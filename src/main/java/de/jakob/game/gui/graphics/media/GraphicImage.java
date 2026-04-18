package de.jakob.game.gui.graphics.media;

import javafx.scene.image.ImageView;
public class GraphicImage extends GraphicTextureItem {

    private String path;

    protected GraphicImage() {
        super();
    }

    public static Builder builder() {
        return new Builder();
    }

    public GraphicImage path(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            this.path = null;
            apply(null);
            return this;
        }

        this.path = relativePath;
        apply(GraphicMediaCache.texture(relativePath));
        return this;
    }

    @Override
    protected GraphicMediaCache.CachedImage currentData() {
        return data;
    }

    @Override
    public void build() {
        final var d = data;
        if (d == null) return;

        final double w = getWidth() > 0 ? getWidth() : d.width();
        final double h = getHeight() > 0 ? getHeight() : d.height();

        size(w, h);

        view.setImage(d.image());
        view.setFitWidth(w);
        view.setFitHeight(h);

        applyPosition(); // 🔥 jetzt korrekt
    }

    public ImageView view() {
        return view;
    }

    public String path() {
        return path;
    }

    public static class Builder extends GraphicTextureBuilder<GraphicImage, Builder> {

        private String path;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        @Override
        protected GraphicImage create() {
            GraphicImage img = new GraphicImage();
            if (path != null) img.path(path);
            return img;
        }
    }
}