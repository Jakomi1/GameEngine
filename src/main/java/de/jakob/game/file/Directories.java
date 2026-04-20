package de.jakob.game.file;

import java.io.File;

public final class Directories {

    private static boolean initialized = false;

    private static GameDirectory baseDir;
    private static GameDirectory assetsDir;
    private static GameDirectory texturesDir;
    private static GameDirectory soundsDir;
    private static GameDirectory mediaDir;
    private static GameDirectory debugDir;
    private static GameDirectory dataDir;

    private Directories() {
    }

    public static void init(String rootDir) {
        if (initialized) {
            throw new IllegalStateException("Directories already initialized!");
        }

        baseDir = new GameDirectory(new File(rootDir, "resources"));
        assetsDir = baseDir.dir("assets");
        dataDir = baseDir.dir("data");

        texturesDir = assetsDir.dir("textures");
        soundsDir = assetsDir.dir("sounds");
        mediaDir = assetsDir.dir("media");

        debugDir = baseDir.dir("debug");

        baseDir.mkdirs();
        assetsDir.mkdirs();
        dataDir.mkdirs();
        texturesDir.mkdirs();
        soundsDir.mkdirs();
        mediaDir.mkdirs();
        debugDir.mkdirs();

        initialized = true;
    }

    private static void ensureInit() {
        if (!initialized) {
            throw new IllegalStateException("Directories not initialized! Call Directories.init(...) first.");
        }
    }

    public static GameDirectory base() {
        ensureInit();
        return baseDir;
    }

    public static GameDirectory assets() {
        ensureInit();
        return assetsDir;
    }

    public static GameDirectory data() {
        ensureInit();
        return dataDir;
    }

    public static GameDirectory debug() {
        ensureInit();
        return debugDir;
    }

    public static GameDirectory textures() {
        ensureInit();
        return texturesDir;
    }

    public static GameDirectory sounds() {
        ensureInit();
        return soundsDir;
    }

    public static GameDirectory media() {
        ensureInit();
        return mediaDir;
    }

    public static GameFile texture(String path) {
        ensureInit();
        return texturesDir.file(path);
    }

    public static GameFile sound(String path) {
        ensureInit();
        return soundsDir.file(path);
    }

    public static GameFile media(String path) {
        ensureInit();
        return mediaDir.file(path);
    }

    public static GameFile dataFile(String path) {
        ensureInit();
        return dataDir.file(path);
    }

    public static GameFile debugFile(String path) {
        ensureInit();
        return debugDir.file(path);
    }
}