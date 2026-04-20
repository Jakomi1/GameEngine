package de.jakob.game.input;

import de.jakob.game.file.Directories;
import de.jakob.game.file.GameFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class KeyBinds {

    private static final Map<String, Bind> CACHE = new LinkedHashMap<>();
    private static final GameFile FILE = Directories.dataFile("keybinds.txt");

    private KeyBinds() {}

    private record Bind(String action, String displayName, Key key, boolean changeable) {
        private Bind {
            action = action != null ? action.trim() : "";
            displayName = displayName != null ? displayName.trim() : "";
        }

        Bind(String action, String displayName, Key key) {
            this(action, displayName, key, true);
        }
    }

    private enum DefaultBind {
        EXIT("exit", "Beenden", Key.ESCAPE, false),
        DEBUG_SCREEN("debug", "Informationen", Key.F3, true);

        private final String action;
        private final String displayName;
        private final Key key;
        private final boolean changeable;

        DefaultBind(String action, String displayName, Key key, boolean changeable) {
            this.action = action;
            this.displayName = displayName;
            this.key = key;
            this.changeable = changeable;
        }

        private String action() {
            return action;
        }

        private String displayName() {
            return displayName;
        }

        private Key key() {
            return key;
        }

        private boolean changeable() {
            return changeable;
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

        ensureAllDefaultsExist();
        save();
    }

    private static void parseLine(String line) {
        if (line == null || line.isBlank()) return;
        String trimmed = line.trim();
        if (trimmed.startsWith("#")) return;

        if (trimmed.contains("|")) {
            String[] split = trimmed.split("\\|", 3);
            if (split.length != 3) return;

            String action = split[0].trim();
            String displayName = split[1].trim();
            String keyName = split[2].trim();

            Key key = parseKey(keyName);
            if (action.isEmpty()) return;

            CACHE.put(action, new Bind(
                    action,
                    displayName.isEmpty() ? fallbackDisplayName(action) : displayName,
                    key
            ));
        } else if (trimmed.contains("=")) {
            String[] split = trimmed.split("=", 2);
            if (split.length != 2) return;

            String action = split[0].trim();
            String keyName = split[1].trim();

            Key key = parseKey(keyName);
            if (action.isEmpty()) return;

            String displayName = defaultDisplayNameFor(action);
            if (displayName == null || displayName.isBlank()) {
                displayName = fallbackDisplayName(action);
            }

            CACHE.put(action, new Bind(action, displayName, key));
        }
    }

    private static Key parseKey(String keyName) {
        if (keyName == null || keyName.isBlank()) return Key.UNDEFINED;
        try {
            return Key.valueOf(keyName.trim());
        } catch (IllegalArgumentException ignored) {
            return Key.UNDEFINED;
        }
    }

    private static void setDefaults() {
        CACHE.clear();
        for (DefaultBind bind : DefaultBind.values()) {
            CACHE.put(bind.action(),
                    new Bind(bind.action(), bind.displayName(), bind.key(), bind.changeable()));
        }
    }

    private static void ensureAllDefaultsExist() {
        for (DefaultBind bind : DefaultBind.values()) {
            Bind existing = CACHE.get(bind.action());
            if (existing == null) {
                CACHE.put(bind.action(),
                        new Bind(bind.action(), bind.displayName(), bind.key(), bind.changeable()));
            } else {
                CACHE.put(bind.action(),
                        new Bind(bind.action(), bind.displayName(), existing.key(), bind.changeable()));
            }
        }
    }

    public static void save() {
        StringBuilder sb = new StringBuilder();
        for (Bind bind : getOrderedBinds()) {
            sb.append(bind.action()).append("|")
                    .append(bind.displayName()).append("|")
                    .append(bind.key().name()).append("\n");
        }
        FILE.writeString(sb.toString());
    }

    private static List<Bind> getOrderedBinds() {
        List<Bind> ordered = new ArrayList<>();
        for (DefaultBind bind : DefaultBind.values()) {
            Bind cached = CACHE.get(bind.action());
            if (cached != null) ordered.add(cached);
        }

        List<Bind> others = new ArrayList<>();
        for (Bind bind : CACHE.values()) {
            if (!isDefaultAction(bind.action())) others.add(bind);
        }
        others.sort(Comparator.comparing(Bind::action, String.CASE_INSENSITIVE_ORDER));
        ordered.addAll(others);
        return ordered;
    }

    private static boolean isDefaultAction(String action) {
        for (DefaultBind bind : DefaultBind.values()) {
            if (Objects.equals(bind.action(), action)) return true;
        }
        return false;
    }

    private static String defaultDisplayNameFor(String action) {
        for (DefaultBind bind : DefaultBind.values()) {
            if (Objects.equals(bind.action(), action)) return bind.displayName();
        }
        return null;
    }

    private static String fallbackDisplayName(String action) {
        if (action == null || action.isBlank()) return "";
        String pretty = action.replace('_', ' ').trim();
        if (pretty.isEmpty()) return action;
        return Character.toUpperCase(pretty.charAt(0)) + pretty.substring(1);
    }

    public static void register(String action, Key key) {
        register(action, fallbackDisplayName(action), key);
    }

    public static boolean isChangeable(String action) {
        Bind bind = CACHE.get(action);
        return bind != null && bind.changeable();
    }

    public static void register(String action, String displayName, Key key) {
        if (action == null || action.isBlank()) return;

        Bind existing = CACHE.get(action);

        String usedDisplayName = (displayName == null || displayName.isBlank())
                ? (existing != null ? existing.displayName() : fallbackDisplayName(action))
                : displayName;

        boolean changeable = existing == null || existing.changeable();

        CACHE.put(action, new Bind(action, usedDisplayName, key, changeable));
        save();
    }

    public static void set(String action, Key key) {
        if (action == null || action.isBlank()) return;

        Bind existing = CACHE.get(action);
        if (existing == null) return;

        if (!existing.changeable()) return;

        CACHE.put(action, new Bind(action, existing.displayName(), key, true));
        save();
    }

    public static Key get(String action) {
        Bind bind = CACHE.get(action);
        return bind != null ? bind.key() : Key.UNDEFINED;
    }

    public static String getDisplayName(String action) {
        Bind bind = CACHE.get(action);
        if (bind != null && !bind.displayName().isBlank()) return bind.displayName();
        String defaultDisplay = defaultDisplayNameFor(action);
        return defaultDisplay != null ? defaultDisplay : fallbackDisplayName(action);
    }

    public static Map<String, Key> getAll() {
        Map<String, Key> result = new LinkedHashMap<>();
        for (Bind bind : getOrderedBinds()) {
            result.put(bind.action(), bind.key());
        }
        return Collections.unmodifiableMap(result);
    }

    public static Map<String, String> getAllDisplayNames() {
        Map<String, String> result = new LinkedHashMap<>();
        for (Bind bind : getOrderedBinds()) {
            result.put(bind.action(), bind.displayName());
        }
        return Collections.unmodifiableMap(result);
    }

    public static boolean contains(String action) {
        return action != null && CACHE.containsKey(action);
    }
}