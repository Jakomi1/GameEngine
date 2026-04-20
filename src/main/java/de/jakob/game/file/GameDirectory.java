package de.jakob.game.file;

import java.io.File;

public final class GameDirectory {

    private final File directory;

    GameDirectory(File directory) {
        this.directory = directory;
    }

    public File asFile() {
        return directory;
    }

    public String getName() {
        return directory.getName();
    }

    public String getPath() {
        return directory.getPath();
    }

    public boolean exists() {
        return directory.exists();
    }

    public boolean isDirectory() {
        return directory.isDirectory();
    }

    public GameDirectory mkdirs() {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return this;
    }

    public GameDirectory dir(String child) {
        return new GameDirectory(new File(directory, clean(child)));
    }

    public GameFile file(String path) {
        return new GameFile(new File(directory, clean(path)));
    }

    public GameFile txt(String name) {
        return file(ensureExtension(name, ".txt"));
    }

    public GameFile json(String name) {
        return file(ensureExtension(name, ".json"));
    }

    public GameFile xml(String name) {
        return file(ensureExtension(name, ".xml"));
    }

    public GameFile csv(String name) {
        return file(ensureExtension(name, ".csv"));
    }

    public GameFile yaml(String name) {
        return file(ensureExtension(name, ".yaml"));
    }

    private static String clean(String path) {
        if (path == null || path.isEmpty()) return "";
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private static String ensureExtension(String name, String ext) {
        if (name == null || name.isEmpty()) return ext;
        if (name.endsWith(ext)) return name;
        return name + ext;
    }
}