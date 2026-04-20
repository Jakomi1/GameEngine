package de.jakob.game.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class GameFile {

    private final File file;

    GameFile(File file) {
        this.file = file;
    }

    public File asFile() {
        return file;
    }

    public String getName() {
        return file.getName();
    }

    public String getPath() {
        return file.getPath();
    }

    public boolean exists() {
        return file.exists();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isFile() {
        return file.isFile();
    }

    public GameFile ensureParentExists() {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return this;
    }

    public String readString() {
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + file, e);
        }
    }

    public List<String> readLines() {
        try {
            return Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + file, e);
        }
    }

    public byte[] readBytes() {
        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + file, e);
        }
    }

    public GameFile writeString(String content) {
        try {
            ensureParentExists();
            Files.writeString(
                    file.toPath(),
                    content != null ? content : "",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            return this;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + file, e);
        }
    }

    public GameFile writeLines(List<String> lines) {
        try {
            ensureParentExists();
            Files.write(
                    file.toPath(),
                    lines != null ? lines : List.of(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            return this;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + file, e);
        }
    }

    public GameFile writeBytes(byte[] data) {
        try {
            ensureParentExists();
            Files.write(
                    file.toPath(),
                    data != null ? data : new byte[0],
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            return this;
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + file, e);
        }
    }

    public GameFile append(String content) {
        try {
            ensureParentExists();
            Files.writeString(
                    file.toPath(),
                    content != null ? content : "",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            return this;
        } catch (IOException e) {
            throw new RuntimeException("Failed to append file: " + file, e);
        }
    }

    public boolean delete() {
        return file.delete();
    }

    @Override
    public String toString() {
        return file.toString();
    }
}