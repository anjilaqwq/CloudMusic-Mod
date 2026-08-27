package fengliu.cloudmusic.mixin;

import fengliu.cloudmusic.music163.Shares;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

@Mixin(ChatComponent.class)
public class ChatHudMixin {
    @Unique
    private static final Pattern SHAR_PATTERN = Pattern.compile("CloudMusic#.+\\sid:\\s[^\\sid:a-zA-Z]\\w[^\\sa-zA-Z]+$", Pattern.CASE_INSENSITIVE);

    /**
     * 判断是否为分享消息
     * @param sharMatcher matcher
     * @return bool
     */
    @Unique
    private boolean isSharMessage(Matcher sharMatcher){
        if (!sharMatcher.find()) {
            return false;
        }

        return sharMatcher.groupCount() <= 1;
    }

    /**
     * 设置分享消息样式
     * @param message 消息
     */
    @Unique
    private void setShar(Component message){
        Matcher sharMatcher = SHAR_PATTERN.matcher(message.getString());
        if (!isSharMessage(sharMatcher)) {
            return;
        }

        String[] keyValuePair = sharMatcher.group(0).replace("CloudMusic# ", "").split(" id: ");
        if (keyValuePair.length > 2){
            return;
        }

        for (Shares shar: Shares.values()) {
            if(!shar.isShar(keyValuePair[0])){
                continue;
            }

            try {
                ((MutableComponent) message).setStyle(
                    Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand(
                            shar.getCommand(Long.parseLong(keyValuePair[1]))
                        ))
                        .withColor(0x87CEEB)
                        .withUnderlined(true)
                );
            } catch (NumberFormatException err) {
                return;
            }

            return;
        }
    }

    @Inject(method = "addPlayerMessage", at = @At("HEAD"))
    public void addMessage(Component message, @Nullable MessageSignature signature, @Nullable GuiMessageTag indicator, CallbackInfo info){
        setShar(message);
    }
}
