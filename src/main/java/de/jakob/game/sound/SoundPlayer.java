package de.jakob.game.sound;

import javafx.application.Platform;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("CallToPrintStackTrace")
public final class SoundPlayer {
    public static double getDefaultVolume() {
        return AudioConfig.getDefaultVolume();
    }
    private SoundPlayer() {
    }

    public static void play(String name) {
        play(name, getDefaultVolume());
    }

    public static void play(String name, double volume) {
        if (volume <= 0) {
            return;
        }

        Sound.get(name).thenAccept(sound -> {
            AudioClipPlay.play(sound, volume);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public static void play(String name, double startSecond, double volume) {
        play(name, startSecond, -1, volume);
    }

    public static void play(String name, double startSecond, double endSecond, double volume) {
        if (volume <= 0) {
            return;
        }

        Sound.get(name).thenAccept(sound -> runFx(() -> {
            MediaPlayer player = new MediaPlayer(sound.media());

            applyRange(player, startSecond, endSecond);
            player.setVolume(clamp(volume));

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

    public static SoundLoop loop(String name) {
        return loop(name, getDefaultVolume());
    }

    public static SoundLoop loop(String name, double volume) {
        SoundLoop loop = new SoundLoop();

        if (volume <= 0) {
            return loop;
        }

        Sound.get(name).thenAccept(sound -> {
            AudioClip clip = sound.clip();
            clip.setCycleCount(AudioClip.INDEFINITE);
            clip.setVolume(clamp(volume));
            loop.attach(clip);
            clip.play();
        }).exceptionally(ex -> {
            loop.fail(ex);
            ex.printStackTrace();
            return null;
        });

        return loop;
    }

    public static SoundLoop loop(String name, double startSecond, double volume) {
        return loop(name, startSecond, -1, volume);
    }

    public static SoundLoop loop(String name, double startSecond, double endSecond, double volume) {
        SoundLoop loop = new SoundLoop();

        if (volume <= 0) {
            return loop;
        }

        Sound.get(name).thenAccept(sound -> runFx(() -> {
            try {
                MediaPlayer player = new MediaPlayer(sound.media());

                applyRange(player, startSecond, endSecond);
                player.setVolume(clamp(volume));
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
        return Sound.get(name).thenApply(sound -> null);
    }

    public static CompletableFuture<Void> warmUp(Sound sound) {
        return CompletableFuture.completedFuture(null);
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

    private static double clamp(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

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

    private static final class AudioClipPlay {
        private static void play(Sound sound, double volume) {
            if (volume <= 0) {
                return;
            }
            sound.clip().play(clamp(volume));
        }

        private static double clamp(double v) {
            if (v < 0) return 0;
            if (v > 1) return 1;
            return v;
        }
    }
}