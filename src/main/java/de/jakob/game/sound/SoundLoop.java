package de.jakob.game.sound;

import javafx.scene.media.MediaPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class SoundLoop {

    private final AtomicReference<MediaPlayer> playerRef = new AtomicReference<>();
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final CompletableFuture<Void> ready = new CompletableFuture<>();

    SoundLoop() {
    }

    void attach(MediaPlayer player) {
        if (player == null) {
            ready.completeExceptionally(new IllegalArgumentException("player must not be null"));
            return;
        }

        if (stopped.get()) {
            safeDispose(player);
            ready.complete(null);
            return;
        }

        playerRef.set(player);
        ready.complete(null);
    }

    void fail(Throwable t) {
        ready.completeExceptionally(t);
    }

    public CompletableFuture<Void> whenLoaded() {
        return ready;
    }

    public void stop() {
        stopped.set(true);

        MediaPlayer player = playerRef.getAndSet(null);
        if (player != null) {
            try {
                player.stop();
            } finally {
                safeDispose(player);
            }
        }
    }

    public boolean isPlaying() {
        MediaPlayer player = playerRef.get();
        return player != null && player.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public boolean isLoaded() {
        return playerRef.get() != null;
    }

    private static void safeDispose(MediaPlayer player) {
        try {
            player.dispose();
        } catch (Throwable ignored) {
        }
    }
}