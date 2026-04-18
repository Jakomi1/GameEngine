package de.jakob.game.path;

import java.io.File;

public final class Directories {

    private static boolean initialized = false;

    private static File baseDir;
    private static File assetsDir;
    private static File texturesDir;
    private static File soundsDir;
    private static File mediaDir;
    private static File debugDir;

    private Directories() {
    }

    public static void init(String rootDir) {
        if (initialized) {
            throw new IllegalStateException("Directories already initialized!");
        }

        baseDir = new File(rootDir, "resources");
        assetsDir = new File(baseDir, "assets");
        texturesDir = new File(assetsDir, "textures");
        soundsDir = new File(assetsDir, "sounds");
        mediaDir = new File(assetsDir, "media");
        debugDir = new File(baseDir, "debug");

        create(baseDir);
        create(assetsDir);
        create(texturesDir);
        create(soundsDir);
        create(mediaDir);
        create(debugDir);

        initialized = true;
    }

    private static void ensureInit() {
        if (!initialized) {
            throw new IllegalStateException("Directories not initialized! Call Directories.init(...) first.");
        }
    }

    private static void create(File dir) {
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static File base() {
        ensureInit();
        return baseDir;
    }

    public static File assets() {
        ensureInit();
        return assetsDir;
    }

    public static File textures() {
        ensureInit();
        return texturesDir;
    }

    public static File sounds() {
        ensureInit();
        return soundsDir;
    }

    public static File media() {
        ensureInit();
        return mediaDir;
    }

    public static File texture(String path) {
        ensureInit();
        return new File(texturesDir, clean(path));
    }

    public static File sound(String path) {
        ensureInit();
        return new File(soundsDir, clean(path));
    }

    public static File media(String path) {
        ensureInit();
        return new File(mediaDir, clean(path));
    }

    public static File debug() {
        ensureInit();
        return debugDir;
    }

    public static File debugFile(String path) {
        ensureInit();
        return new File(debugDir, clean(path));
    }

    private static String clean(String path) {
        if (path == null) return "";
        return path.startsWith("/") ? path.substring(1) : path;
    }
}