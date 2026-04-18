package de.jakob.game.sound;

import de.jakob.game.logger.Logger;
import de.jakob.game.path.Directories;
import javafx.scene.media.Media;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class Sound {

    private static final String FALLBACK = "error.mp3";

    private static final ExecutorService LOADER =
            Executors.newCachedThreadPool(daemonFactory("Sound-Loader"));

    private static final ConcurrentHashMap<String, CompletableFuture<Sound>> CACHE = new ConcurrentHashMap<>();

    private static final CompletableFuture<Sound> FALLBACK_FUTURE = loadFallbackAsync();

    private final String name;
    private final Media media;

    private Sound(String name, Media media) {
        this.name = name;
        this.media = media;
    }

    public String name() {
        return name;
    }

    public Media media() {
        return media;
    }

    public static CompletableFuture<Sound> get(String name) {
        if (name == null || name.isBlank()) {
            return FALLBACK_FUTURE;
        }

        if (FALLBACK.equalsIgnoreCase(name)) {
            return FALLBACK_FUTURE;
        }

        return CACHE.computeIfAbsent(name, Sound::loadAsync);
    }

    public static CompletableFuture<Void> preload(String... names) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        if (names == null) {
            return chain;
        }

        for (String name : names) {
            chain = chain.thenCompose(ignored -> get(name).thenApply(sound -> null));
        }

        return chain;
    }

    private static CompletableFuture<Sound> loadAsync(String name) {
        return CompletableFuture.supplyAsync(() -> loadOrNull(name), LOADER)
                .thenCompose(sound -> {
                    if (sound != null) {
                        return CompletableFuture.completedFuture(sound);
                    }
                    return FALLBACK_FUTURE;
                });
    }

    private static CompletableFuture<Sound> loadFallbackAsync() {
        return CompletableFuture.supplyAsync(() -> loadStrict(FALLBACK), LOADER);
    }

    private static Sound loadOrNull(String name) {
        File file = Directories.sound(name);

        if (!file.exists()) {
            Logger.warn("[Sound] Missing: " + name);
            return null;
        }

        return new Sound(name, new Media(file.toURI().toString()));
    }

    private static Sound loadStrict(String name) {
        File file = Directories.sound(name);

        if (!file.exists()) {
            throw new RuntimeException("Fallback sound missing: " + FALLBACK);
        }

        return new Sound(name, new Media(file.toURI().toString()));
    }

    private static ThreadFactory daemonFactory(String name) {
        return r -> {
            Thread t = new Thread(r, name);
            t.setDaemon(true);
            return t;
        };
    }
}