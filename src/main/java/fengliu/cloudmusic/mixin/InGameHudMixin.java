package fengliu.cloudmusic.mixin;

import com.google.gson.JsonElement;
import fengliu.cloudmusic.command.MusicCommand;
import fengliu.cloudmusic.config.Configs;
import fengliu.cloudmusic.music163.IMusic;
import fengliu.cloudmusic.music163.data.DjMusic;
import fengliu.cloudmusic.music163.data.Music;
import fengliu.cloudmusic.render.MusicIconTexture;
import fengliu.cloudmusic.util.MusicPlayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class InGameHudMixin {

    @Unique
    private final Minecraft client = Minecraft.getInstance();

    /**
     * Minecraft 26.2 treats text colors as ARGB without filling in a missing
     * alpha channel. Keep old CloudMusic configs such as #00FFFFFF readable.
     */
    @Unique
    private static int cloudmusic$opaqueTextColor(int color) {
        return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
    }

    @Unique
    private void renderLyrics(GuiGraphicsExtractor context) {
        float lyricScale = (float) Configs.GUI.LYRIC_SCALE.getDoubleValue();
        int lyricY = Configs.GUI.LYRIC_Y.getIntegerValue();
        int lyricX = Configs.GUI.LYRIC_X.getIntegerValue();
        int lyricColor = cloudmusic$opaqueTextColor(Configs.GUI.LYRIC_COLOR.getIntegerValue());

        context.pose().pushMatrix();
        context.pose().scale(lyricScale, lyricScale);
        for (String lyric : MusicCommand.getPlayer().getLyric()) {
            context.text(client.font, lyric, Math.round(lyricX / lyricScale), Math.round(lyricY / lyricScale), lyricColor, true);
            lyricY += 10;
        }
        context.pose().popMatrix();
    }

    @Unique
    public void renderLoginQrCode(GuiGraphicsExtractor context) {
        if (!MusicCommand.loadQRCode) {
            return;
        }
        context.blit(RenderPipelines.GUI_TEXTURED, MusicIconTexture.QR_CODE_ID, 5, 10, 1f, 1f, 64, 64, 64, 64);
    }

    @Unique
    public int[] getMusicInfoPos() {
        int y = Configs.GUI.MUSIC_INFO_Y.getIntegerValue();
        int x = Configs.GUI.MUSIC_INFO_X.getIntegerValue();
        if (this.client.player == null || !Configs.GUI.MUSIC_INFO_EFFECT_OFFSET.getBooleanValue()) {
            return new int[]{y, x};
        }

        int offset = 0;
        for (MobEffectInstance statusEffect : this.client.player.getActiveEffects()) {
            if (statusEffect.getEffect().value().isBeneficial()) {
                offset = 1;
            } else {
                offset = 2;
                break;
            }
        }

        if (offset == 0) {
            return new int[]{y, x};
        }
        return new int[]{y + Configs.GUI.MUSIC_INFO_EFFECT_OFFSET_Y.getIntegerValue() * offset, x + Configs.GUI.MUSIC_INFO_EFFECT_OFFSET_X.getIntegerValue()};
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    public void render(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        this.renderLoginQrCode(context);

        MusicPlayer player = MusicCommand.getPlayer();
        IMusic playingMusic = player.getPlayingMusic();

        if (playingMusic == null) {
            return;
        }

        if (Configs.GUI.STOP_PLAY_SHOW_UI.getBooleanValue() && !player.isPlaying()){
            return;
        }

        this.renderLyrics(context);
        if (!Configs.GUI.MUSIC_INFO.getBooleanValue()) {
            return;
        }

        int width = context.guiWidth();

        int[] pos = this.getMusicInfoPos();
        int y = pos[0];
        int x = pos[1];

        context.fill(width - 175 - x, y, width - x, 48 + y, Configs.GUI.MUSIC_INFO_COLOR.getIntegerValue());
        context.fill(width - 145 - x, 40 + y, width - 30 - x, 43 + y, Configs.GUI.MUSIC_PROGRESS_BAR_COLOR.getIntegerValue());
        int progress = Math.round((115 / (float) playingMusic.getDurationSecond()) * player.getPlayingProgressSecond());
        if (progress > 115) {
            progress = 115;
        }
        context.fill(width - 145 - x, 40 + y, width - 145 + progress - x, 43 + y, Configs.GUI.MUSIC_PLAYED_PROGRESS_BAR_COLOR.getIntegerValue());
        context.blit(RenderPipelines.GUI_TEXTURED, MusicIconTexture.MUSIC_ICON_ID, width - 172 - x, (int) (2.5f + y), 32f, 32f, 32, 32, 32, 32);
        context.text(this.client.font, playingMusic.getName().length() > 16 ? playingMusic.getName().substring(0, 16) + "..." : playingMusic.getName(), width - 135 - x, 4 + y, cloudmusic$opaqueTextColor(Configs.GUI.MUSIC_INFO_TITLE_FONT_COLOR.getIntegerValue()), true);

        int progressFontColor = cloudmusic$opaqueTextColor(Configs.GUI.MUSIC_PROGRESS_FONT_COLOR.getIntegerValue());
        context.text(this.client.font, player.getPlayingProgressToString(), width - 172 - x, 38 + y, progressFontColor, true);
        context.text(this.client.font, playingMusic.getDurationToString(), width - 28 - x, 38 + y, progressFontColor, true);
        int musicFontColor = cloudmusic$opaqueTextColor(Configs.GUI.MUSIC_INFO_FONT_COLOR.getIntegerValue());
        if (playingMusic instanceof DjMusic music) {
            context.text(this.client.font, Component.translatable("cloudmusic.info.dj.creator", music.dj.get("nickname").getAsString()), width - 135 - x, 14 + y, musicFontColor, true);
            context.text(this.client.font, Component.translatable("cloudmusic.info.dj.music.count", music.listenerCount, music.likedCount), width - 135 - x, 24 + y, musicFontColor, true);
            return;
        }

        Music music = (Music) playingMusic;
        if (!music.aliasName.isEmpty()) {
            context.text(this.client.font, music.aliasName.length() > 16 ? music.aliasName.substring(0, 16) + "..." : music.aliasName, width - 135 - x, 14 + y, musicFontColor, true);
        } else {
            String album = music.album.get("name").getAsString();
            context.text(this.client.font, album.length() > 16 ? album.substring(0, 16) + "..." : album, width - 135 - x, 14 + y, musicFontColor, true);
        }

        StringBuilder artist = new StringBuilder();
        for (JsonElement artistData : music.artists.asList()) {
            artist.append(artistData.getAsJsonObject().get("name").getAsString()).append("/");
        }
        artist = new StringBuilder(artist.substring(0, artist.length() - 1));
        context.text(this.client.font, artist.length() > 16 ? artist.substring(0, 16) + "..." : artist.toString(), width - 135 - x, 24 + y, musicFontColor, true);

        if (music.freeTrialInfo == null) {
            return;
        }

        int freeTrialEndProgress = Math.round((115 / (float) playingMusic.getDurationSecond()) * music.freeTrialInfo.get("end").getAsInt());
        context.fill(width - 145 + freeTrialEndProgress - 1 - x, 40 + y, width - 145 + freeTrialEndProgress + 1 - x, 43 + y, Configs.GUI.MUSIC_PLAYED_PROGRESS_BAR_COLOR.getIntegerValue());
    }

}
