package de.jakob.game.sound;

import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class SoundLoop {

    private final AtomicReference<AudioClip> clipRef = new AtomicReference<>();
    private final AtomicReference<MediaPlayer> playerRef = new AtomicReference<>();
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final CompletableFuture<Void> ready = new CompletableFuture<>();

    SoundLoop() {
    }

    void attach(AudioClip clip) {
        if (clip == null) {
            fail(new IllegalArgumentException("clip must not be null"));
            return;
        }

        if (stopped.get()) {
            clip.stop();
            completeReady();
            return;
        }

        clipRef.set(clip);
        completeReady();
    }

    void attach(MediaPlayer player) {
        if (player == null) {
            fail(new IllegalArgumentException("player must not be null"));
            return;
        }

        if (stopped.get()) {
            runFx(() -> {
                try {
                    player.stop();
                } finally {
                    safeDispose(player);
                    completeReady();
                }
            });
            return;
        }

        playerRef.set(player);
        completeReady();
    }

    void fail(Throwable t) {
        ready.completeExceptionally(t);
    }

    public CompletableFuture<Void> whenLoaded() {
        return ready;
    }

    public void stop() {
        stopped.set(true);

        AudioClip clip = clipRef.getAndSet(null);
        if (clip != null) {
            clip.stop();
        }

        MediaPlayer player = playerRef.getAndSet(null);
        if (player != null) {
            runFx(() -> {
                try {
                    player.stop();
                } finally {
                    safeDispose(player);
                }
            });
        }
    }

    public boolean isPlaying() {
        AudioClip clip = clipRef.get();
        if (clip != null) {
            return clip.isPlaying();
        }

        MediaPlayer player = playerRef.get();
        return player != null && player.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public boolean isLoaded() {
        return clipRef.get() != null || playerRef.get() != null;
    }

    private void completeReady() {
        ready.complete(null);
    }

    private static void safeDispose(MediaPlayer player) {
        try {
            player.dispose();
        } catch (Throwable ignored) {
        }
    }

    private static void runFx(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }
}