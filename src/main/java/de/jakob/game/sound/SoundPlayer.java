package de.jakob.game.sound;

import javafx.application.Platform;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public final class SoundPlayer {

    private SoundPlayer() {
    }

    // ---------- PLAY ----------

    public static void play(String name) {
        play(name, -1, -1);
    }

    public static void play(String name, double startSecond) {
        play(name, startSecond, -1);
    }

    public static void play(String name, double startSecond, double endSecond) {
        Sound.get(name).thenAccept(sound -> runFx(() -> {
            MediaPlayer player = new MediaPlayer(sound.media());
            applyRange(player, startSecond, endSecond);

            player.setOnEndOfMedia(() -> {
                try {
                    player.stop();
                } finally {
                    player.dispose();
                }
            });

            player.setOnError(() -> {
                try {
                    player.stop();
                } finally {
                    player.dispose();
                }
            });

            player.play();
        })).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    // ---------- LOOP ----------

    public static SoundLoop loop(String name) {
        return loop(name, -1, -1);
    }

    public static SoundLoop loop(String name, double startSecond) {
        return loop(name, startSecond, -1);
    }

    public static SoundLoop loop(String name, double startSecond, double endSecond) {
        SoundLoop loop = new SoundLoop();

        Sound.get(name).thenAccept(sound -> runFx(() -> {
            try {
                MediaPlayer player = new MediaPlayer(sound.media());
                applyRange(player, startSecond, endSecond);

                player.setCycleCount(MediaPlayer.INDEFINITE);

                player.setOnEndOfMedia(() -> {
                    Duration start = player.getStartTime();
                    player.seek(start != null ? start : Duration.ZERO);
                });

                player.setOnError(() -> {
                    try {
                        player.stop();
                    } finally {
                        player.dispose();
                    }
                });

                loop.attach(player);
                player.play();
            } catch (Throwable t) {
                loop.fail(t);
                t.printStackTrace();
            }
        })).exceptionally(ex -> {
            loop.fail(ex);
            ex.printStackTrace();
            return null;
        });

        return loop;
    }

    // ---------- WARMUP ----------

    public static CompletableFuture<Void> warmUp(String name) {
        return Sound.get(name).thenCompose(SoundPlayer::warmUp);
    }

    public static CompletableFuture<Void> warmUp(Sound sound) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        runFx(() -> {
            try {
                MediaPlayer player = new MediaPlayer(sound.media());

                player.setOnReady(() -> {
                    try {
                        player.setVolume(0.0);
                        player.play();
                        player.pause();
                        player.seek(Duration.ZERO);
                    } catch (Throwable ignored) {
                    } finally {
                        try {
                            player.stop();
                        } finally {
                            player.dispose();
                            future.complete(null);
                        }
                    }
                });

                player.setOnError(() -> {
                    Throwable err = player.getError();
                    try {
                        player.stop();
                    } finally {
                        player.dispose();
                    }
                    future.completeExceptionally(
                            err != null ? err : new RuntimeException("Sound warmup failed for " + sound.name())
                    );
                });
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    public static CompletableFuture<Void> warmUpAll(String... names) {
        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        if (names == null) {
            return chain;
        }

        for (String name : names) {
            chain = chain.thenCompose(ignored -> warmUp(name));
        }

        return chain;
    }

    // ---------- HELPER ----------

    private static void applyRange(MediaPlayer player, double start, double end) {
        if (start >= 0) {
            player.setStartTime(Duration.seconds(start));
        }

        if (end > 0 && end > start) {
            player.setStopTime(Duration.seconds(end));
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