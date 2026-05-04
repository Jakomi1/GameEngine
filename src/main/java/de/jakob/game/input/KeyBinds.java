package de.jakob.game.input;

import de.jakob.game.file.Directories;
import de.jakob.game.file.GameFile;

import java.util.*;

public final class KeyBinds {

    private static final Map<String, Bind> CACHE = new LinkedHashMap<>();
    private static final Map<String, KeyBind> REF_CACHE = new LinkedHashMap<>();
    private static final GameFile FILE = Directories.dataFile("keybinds.txt");

    private KeyBinds() {}

    private record Bind(String action, String displayName, Key key, boolean changeable) {}

    public enum DefaultBind {
        EXIT("exit", "Beenden", Key.ESCAPE, false),
        DEBUG_SCREEN("debug", "Informationen", Key.F3, true),
        JUMP("jump", "Springen", Key.SPACE, true),
        MOVE_LEFT("move_left", "Links bewegen", Key.A, true),
        MOVE_RIGHT("move_right", "Rechts bewegen", Key.D, true);

        final String action;
        final String displayName;
        final Key key;
        final boolean changeable;

        DefaultBind(String action, String displayName, Key key, boolean changeable) {
            this.action = action;
            this.displayName = displayName;
            this.key = key;
            this.changeable = changeable;

        }

        public KeyBind getKeyBind() {
            return KeyBinds.getKeyBind(action);
        }
        public Key getKey() {
            return KeyBinds.getResolvedKey(action);
        }
        public void reset() {
            KeyBinds.set(action, key);
        }
    }

    public static void load() {
        CACHE.clear();

        List<String> lines = FILE.exists() ? FILE.readLines() : Collections.emptyList();

        if (lines.isEmpty()) {
            setDefaults();
            save();
            return;
        }

        for (String line : lines) {
            parseLine(line);
        }

        ensureDefaults();
        save();
    }

    private static void setDefaults() {
        for (DefaultBind b : DefaultBind.values()) {
            CACHE.put(b.action, new Bind(
                    b.action,
                    b.displayName,
                    b.key,
                    b.changeable
            ));
        }
    }

    private static void ensureDefaults() {
        for (DefaultBind b : DefaultBind.values()) {
            CACHE.putIfAbsent(b.action, new Bind(
                    b.action,
                    b.displayName,
                    b.key,
                    b.changeable
            ));
        }
    }

    private static void parseLine(String line) {
        if (line == null || line.isBlank() || line.startsWith("#")) return;

        String[] split = line.split("\\|");
        if (split.length != 3) return;

        String action = split[0].trim();
        String display = split[1].trim();
        String keyName = split[2].trim();

        Key key;
        try {
            key = Key.valueOf(keyName);
        } catch (Exception e) {
            key = Key.UNDEFINED;
        }

        Bind existing = CACHE.get(action);
        boolean changeable = existing != null ? existing.changeable : true;

        CACHE.put(action, new Bind(
                action,
                display.isBlank() ? fallback(action) : display,
                key,
                changeable
        ));
    }

    public static void save() {
        StringBuilder sb = new StringBuilder();

        for (Bind b : CACHE.values()) {
            sb.append(b.action).append("|")
                    .append(b.displayName).append("|")
                    .append(b.key.name()).append("\n");
        }

        FILE.writeString(sb.toString());
    }

    public static KeyBind registerOrGet(String action, String displayName, Key key) {
        if (action == null || action.isBlank()) return null;

        if (CACHE.containsKey(action)) {
            return getKeyBind(action);
        }

        CACHE.put(action, new Bind(
                action,
                displayName,
                key,
                true
        ));

        save();
        return getKeyBind(action);
    }

    public static void set(String action, Key key) {
        Bind b = CACHE.get(action);
        if (b == null || !b.changeable) return;

        CACHE.put(action, new Bind(
                action,
                b.displayName,
                key,
                b.changeable
        ));

        save();
    }

    public static KeyBind getKeyBind(String action) {
        if (action == null || action.isBlank()) return null;
        if (!CACHE.containsKey(action)) return null;

        return REF_CACHE.computeIfAbsent(action, KeyBind::new);
    }

    public static Key get(String action) {
        Bind b = CACHE.get(action);
        return b != null ? b.key : Key.UNDEFINED;
    }

    public static Key getDefaultKey(String action) {
        for (DefaultBind b : DefaultBind.values()) {
            if (b.action.equals(action)) {
                return b.key;
            }
        }
        return Key.UNDEFINED;
    }

    public static DefaultBind getDefault(String action) {
        for (DefaultBind b : DefaultBind.values()) {
            if (b.action.equals(action)) {
                return b;
            }
        }
        return null;
    }

    public static Key getResolvedKey(String action) {
        Bind b = CACHE.get(action);
        if (b != null) return b.key;
        return getDefaultKey(action);
    }

    public static String getDisplayName(String action) {
        Bind b = CACHE.get(action);
        return b != null ? b.displayName : fallback(action);
    }

    public static boolean isChangeable(String action) {
        Bind b = CACHE.get(action);
        return b != null && b.changeable;
    }

    public static boolean contains(String action) {
        return CACHE.containsKey(action);
    }

    public static Map<String, Key> getAll() {
        Map<String, Key> out = new LinkedHashMap<>();
        for (Bind b : CACHE.values()) {
            out.put(b.action, b.key);
        }
        return Collections.unmodifiableMap(out);
    }

    public static DefaultBind[] getDefaults() {
        return DefaultBind.values();
    }

    private static String fallback(String action) {
        if (action == null || action.isEmpty()) return "";
        String p = action.replace('_', ' ');
        return Character.toUpperCase(p.charAt(0)) + p.substring(1);
    }
}