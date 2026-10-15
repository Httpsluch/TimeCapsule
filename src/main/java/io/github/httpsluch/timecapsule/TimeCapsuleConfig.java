package io.github.httpsluch.timecapsule;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * A plain properties file, so the mod needs no config library. Read once at startup and
 * rewritten with every key present, which also creates it on first launch.
 */
public final class TimeCapsuleConfig {
    public static final String FILE_NAME = "timecapsule.properties";

    /** Mode pre-selected in the setup screen: false = real-world days, true = in-game days. */
    public static boolean defaultInGame = false;
    public static int defaultDays = 7;
    public static int maxDays = 365;
    public static boolean announceOpen = true;
    public static boolean animateLid = true;

    private TimeCapsuleConfig() {
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                TimeCapsule.LOGGER.warn("Could not read {}, using defaults", file, e);
            }
        }

        defaultInGame = "in_game".equalsIgnoreCase(props.getProperty("default_time_mode", "real_world").trim());
        // Menu data syncs as 16-bit values, so day counts must stay well below 32767.
        maxDays = clamp(parseInt(props, "max_days", 365), 1, 3650);
        defaultDays = clamp(parseInt(props, "default_days", 7), 1, maxDays);
        announceOpen = Boolean.parseBoolean(props.getProperty("announce_open", "true").trim());
        animateLid = Boolean.parseBoolean(props.getProperty("animate_lid", "true").trim());

        write(file);
        TimeCapsule.LOGGER.info("Time Capsule loaded: default timer {} for {} days (max {}), open announcements {}",
            defaultInGame ? "in-game" : "real-world", defaultDays, maxDays, announceOpen ? "on" : "off");
    }

    private static void write(Path file) {
        String text = String.join("\n",
            "# Time Capsule settings. Restart the game or server after editing.",
            "",
            "# Timer pre-selected when a capsule is placed: real_world or in_game.",
            "# Players can still switch it in the capsule's setup screen.",
            "default_time_mode=" + (defaultInGame ? "in_game" : "real_world"),
            "",
            "# Days pre-selected in the setup screen.",
            "default_days=" + defaultDays,
            "",
            "# Longest timer a player can choose (1-3650).",
            "max_days=" + maxDays,
            "",
            "# Tell everyone in chat when a capsule is opened.",
            "announce_open=" + announceOpen,
            "",
            "# Open the lid while someone is looking inside (a block-state change, no per-tick work).",
            "animate_lid=" + animateLid,
            "");
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write(text);
            }
        } catch (IOException e) {
            TimeCapsule.LOGGER.warn("Could not write {}", file, e);
        }
    }

    private static int parseInt(Properties props, String key, int fallback) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(fallback)).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
