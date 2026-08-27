package fengliu.cloudmusic.music163;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fengliu.cloudmusic.command.MusicCommand;

import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Selects the lyric line matching the player's current playback position.
 */
public class Lyric {
    private static final Pattern TIMESTAMP = Pattern.compile("\\[(\\d+):(\\d{1,2})(?:[.:](\\d{1,3}))?]");
    private static final Pattern OFFSET = Pattern.compile("\\[offset:([+-]?\\d+)]", Pattern.CASE_INSENSITIVE);
    private static final String[] NO_LYRIC = new String[0];

    private final NavigableMap<Long, String> lyric;
    private final NavigableMap<Long, String> translatedLyric;
    private volatile boolean active;

    /**
     * Converts an LRC timestamp to milliseconds. One, two and three digit
     * fractions represent tenths, hundredths and milliseconds respectively.
     */
    public static long timeStrToTime(String value) {
        Matcher matcher = Pattern.compile("^(\\d+):(\\d{1,2})(?:[.:](\\d{1,3}))?$").matcher(value);
        if (!matcher.matches()) {
            return 0;
        }

        try {
            long milliseconds = (Long.parseLong(matcher.group(1)) * 60L + Long.parseLong(matcher.group(2))) * 1000L;
            String fraction = matcher.group(3);
            if (fraction != null) {
                milliseconds += switch (fraction.length()) {
                    case 1 -> Long.parseLong(fraction) * 100L;
                    case 2 -> Long.parseLong(fraction) * 10L;
                    default -> Long.parseLong(fraction);
                };
            }
            return milliseconds;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    /**
     * Parses LRC in timestamp order. A line may contain multiple timestamps.
     */
    public static Map<Long, String> lyricToMap(String source) {
        if (source == null || source.isBlank()) {
            return Collections.emptyNavigableMap();
        }

        long offset = 0;
        Matcher offsetMatcher = OFFSET.matcher(source);
        if (offsetMatcher.find()) {
            try {
                offset = Long.parseLong(offsetMatcher.group(1));
            } catch (NumberFormatException ignored) {
                // Ignore malformed metadata.
            }
        }

        NavigableMap<Long, String> result = new TreeMap<>();
        for (String row : source.split("\\R")) {
            Matcher matcher = TIMESTAMP.matcher(row);
            int textStart = -1;
            while (matcher.find()) {
                textStart = matcher.end();
            }
            if (textStart < 0) {
                continue;
            }

            String text = row.substring(textStart).trim();
            matcher.reset();
            while (matcher.find()) {
                long timestamp = timeStrToTime(matcher.group().substring(1, matcher.group().length() - 1));
                result.put(Math.max(0, timestamp + offset), text);
            }
        }
        return Collections.unmodifiableNavigableMap(result);
    }

    public Lyric(JsonObject data) {
        this.lyric = parseSection(data, "lrc");
        this.translatedLyric = parseSection(data, "tlyric");
    }

    private static NavigableMap<Long, String> parseSection(JsonObject data, String sectionName) {
        if (data == null || !data.has(sectionName) || !data.get(sectionName).isJsonObject()) {
            return Collections.emptyNavigableMap();
        }

        JsonElement value = data.getAsJsonObject(sectionName).get("lyric");
        if (value == null || value.isJsonNull()) {
            return Collections.emptyNavigableMap();
        }

        Map<Long, String> parsed = lyricToMap(value.getAsString());
        if (parsed.isEmpty()) {
            return Collections.emptyNavigableMap();
        }
        return Collections.unmodifiableNavigableMap(new TreeMap<>(parsed));
    }

    /**
     * Returns the lyric for the current progress without a polling thread.
     */
    public String[] getToLyric() {
        if (!this.active || this.lyric.isEmpty()) {
            return NO_LYRIC;
        }

        Map.Entry<Long, String> current = this.lyric.floorEntry(MusicCommand.getPlayer().getPlayingProgress());
        if (current == null || current.getValue().isBlank()) {
            return NO_LYRIC;
        }

        String translation = this.translatedLyric.get(current.getKey());
        if (translation == null || translation.isBlank() || translation.equals(current.getValue())) {
            return new String[]{current.getValue()};
        }
        return new String[]{current.getValue(), translation};
    }

    public void start() {
        this.active = true;
    }

    public void exit() {
        this.active = false;
    }

    /** Pausing keeps the current line visible. */
    public void stop() {
    }

    public void continues() {
    }
}
