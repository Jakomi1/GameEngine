package de.jakob.game.gui.graphics.media;

import de.jakob.game.logger.Logger;
import de.jakob.game.scheduler.GameScheduler;

import java.util.ArrayList;
import java.util.List;
public class GraphicAnimatedImage extends GraphicTextureItem {

    private final List<GraphicMediaCache.CachedImage> frames = new ArrayList<>();
    private int currentFrame;
    private GameScheduler scheduler;
    private GameScheduler.ScheduledTask task;
    private double frameTimeSeconds = 0.1;

    protected GraphicAnimatedImage() {
        super();
    }

    public static Builder builder() {
        return new Builder();

    }

    public GraphicAnimatedImage frames(String... paths) {
        frames.clear();
        currentFrame = 0;

        if (paths == null) {
            apply(null);
            return this;
        }

        for (String p : paths) {
            if (p != null && !p.isBlank()) {
                frames.add(GraphicMediaCache.texture(p));
            }
        }

        apply(frames.isEmpty() ? null : frames.getFirst());
        return this;
    }

    public GraphicAnimatedImage frameTime(double seconds) {
        this.frameTimeSeconds = seconds > 0 ? seconds : 0.001;
        return this;
    }

    @Override
    protected GraphicMediaCache.CachedImage currentData() {
        return frames.isEmpty() ? null : frames.get(currentFrame);
    }

    @Override
    public void build() {
        if (frames.isEmpty()) return;

        final var first = frames.getFirst();

        final double w = getWidth() > 0 ? getWidth() : first.width();
        final double h = getHeight() > 0 ? getHeight() : first.height();

        size(w, h);

        view.setFitWidth(w);
        view.setFitHeight(h);
        view.setImage(first.image());

        applyPosition();

        startAnimation();
    }

    private void startAnimation() {
        stop();

        if (scheduler == null || frames.isEmpty()) return;

        int size = frames.size();

        long periodTicks = Math.max(1, (long) (frameTimeSeconds * 50));

        task = scheduler.runRepeating(() -> {
            currentFrame = (currentFrame + 1) % size;
            view.setImage(frames.get(currentFrame).image());
        }, periodTicks, periodTicks);
    }

    public void play() {
        startAnimation();
    }

    public void pause() {
        stop();
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public static class Builder extends GraphicTextureBuilder<GraphicAnimatedImage, Builder> {

        private String[] paths;
        private double frameTimeSeconds = 0.1;
        private GameScheduler scheduler;
        public Builder frames(String... paths) {
            this.paths = paths;
            return this;
        }

        public Builder frameTime(double seconds) {
            this.frameTimeSeconds = seconds;
            return this;
        }

        public Builder scheduler(GameScheduler scheduler) {
            this.scheduler = scheduler;
            return this;
        }

        @Override
        protected GraphicAnimatedImage create() {
            GraphicAnimatedImage img = new GraphicAnimatedImage();
            img.frameTime(frameTimeSeconds);
            if (paths != null) img.frames(paths);
            if(scheduler != null) {
                img.scheduler = this.scheduler;
            } else {
                Logger.error("GraphicAnimatedImage braucht einen GameScheduler");
            }
            return img;
        }
    }
}