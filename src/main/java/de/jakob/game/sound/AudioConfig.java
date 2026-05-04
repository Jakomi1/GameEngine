package de.jakob.game.sound;

import de.jakob.game.file.Directories;
import de.jakob.game.file.GameFile;

import java.util.List;

public final class AudioConfig {

    private static final GameFile FILE = Directories.dataFile("audio-config.txt");

    private static double defaultVolume = 0.8;

    private AudioConfig() {}

    public static void load() {
        if (!FILE.exists()) {
            save();
            return;
        }

        List<String> lines = FILE.readLines();

        for (String line : lines) {
            parse(line);
        }

    }

    public static void save() {
        StringBuilder sb = new StringBuilder();

        sb.append("# Audio configuration\n");
        sb.append("defaultVolume=").append(defaultVolume).append("\n");

        FILE.writeString(sb.toString());
    }

    private static void parse(String line) {
        if (line == null) return;
        line = line.trim();

        if (line.isEmpty() || line.startsWith("#")) return;

        String[] split = line.split("=");
        if (split.length != 2) return;

        String key = split[0].trim();
        String value = split[1].trim();

        switch (key) {
            case "defaultVolume" -> {
                try {
                    defaultVolume = clamp(Double.parseDouble(value));
                } catch (NumberFormatException ignored) {}
            }
        }
    }

    public static void setDefaultVolume(double volume) {
        defaultVolume = clamp(volume);
        save();
    }

    public static double getDefaultVolume() {
        return defaultVolume;
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(1, v));
    }
}