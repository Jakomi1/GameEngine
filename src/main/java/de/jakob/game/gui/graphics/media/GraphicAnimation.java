package de.jakob.game.gui.graphics.media;
@SuppressWarnings({"FieldCanBeLocal","unused"})
public class GraphicAnimation extends GraphicTextureItem {


    private String path;

    protected GraphicAnimation() {
        super();
    }

    public static Builder builder() {
        return new Builder();
    }

    public GraphicAnimation path(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            this.path = null;
            apply(null);
            return this;
        }

        this.path = relativePath;
        apply(GraphicMediaCache.mediaImage(relativePath));
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

        applyPosition();
    }

    public static class Builder extends GraphicTextureBuilder<GraphicAnimation, Builder> {

        private String path;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        @Override
        protected GraphicAnimation create() {
            GraphicAnimation anim = new GraphicAnimation();
            if (path != null) anim.path(path);
            return anim;
        }
    }
}