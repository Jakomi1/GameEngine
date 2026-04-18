package de.jakob.game.gui.graphics.media;

import de.jakob.game.gui.graphics.GraphicItem;
import de.jakob.game.gui.graphics.Moveable;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.BitSet;

@SuppressWarnings({"SameParameterValue"})
public abstract class GraphicTextureItem extends GraphicItem implements Moveable<GraphicTextureItem> {

    protected final ImageView view = new ImageView();
    protected volatile GraphicMediaCache.CachedImage data;

    protected GraphicTextureItem() {
        setNode(view);
        view.setPreserveRatio(false);
        view.setSmooth(false);
        view.setCache(true);
    }

    protected final void apply(GraphicMediaCache.CachedImage cached) {
        this.data = cached;
        view.setImage(cached != null ? cached.image() : null);
    }

    protected final void syncViewSize(double width, double height) {
        view.setFitWidth(Math.max(0, width));
        view.setFitHeight(Math.max(0, height));
    }

    protected abstract GraphicMediaCache.CachedImage currentData();

    @Override
    public boolean touches(GraphicItem other) {
        return preciseTouches(other);
    }

    public boolean preciseTouches(GraphicItem other) {
        Point2D p = getPosition();
        return preciseTouchesAt(other, p.getX(), p.getY());
    }

    public boolean preciseTouchesAt(GraphicItem other, double thisX, double thisY) {
        if (other == null || other == this) return false;

        final var a = currentData();
        final double aw = effectiveWidth(a);
        final double ah = effectiveHeight(a);

        if (!(other instanceof GraphicTextureItem otherTex)) {
            return aabbIntersectsAt(other, thisX, thisY, aw, ah);
        }

        final var b = otherTex.currentData();

        final double bx = other.getPosition().getX();
        final double by = other.getPosition().getY();
        final double bw = otherTex.effectiveWidth(b);
        final double bh = otherTex.effectiveHeight(b);

        final double left = Math.max(thisX, bx);
        final double top = Math.max(thisY, by);
        final double right = Math.min(thisX + aw, bx + bw);
        final double bottom = Math.min(thisY + ah, by + bh);

        if (left >= right || top >= bottom) return false;

        final long areaA = estimate(a, aw, ah, left, top, right, bottom, thisX, thisY);
        final long areaB = estimate(b, bw, bh, left, top, right, bottom, bx, by);

        return areaA <= areaB
                ? scan(a, thisX, thisY, aw, ah, otherTex, b, bx, by, bw, bh, left, top, right, bottom)
                : scan(b, bx, by, bw, bh, this, a, thisX, thisY, aw, ah, left, top, right, bottom);
    }

    private long estimate(
            GraphicMediaCache.CachedImage data,
            double w, double h,
            double left, double top,
            double right, double bottom,
            double x, double y
    ) {
        if (data == null || data.opaque() == null || data.width() <= 0 || data.height() <= 0 || w <= 0 || h <= 0) {
            return Long.MAX_VALUE;
        }

        double sx = w / data.width();
        double sy = h / data.height();

        if (sx <= 0 || sy <= 0) return Long.MAX_VALUE;

        int x0 = clamp((int) ((left - x) / sx), 0, data.width());
        int x1 = clamp((int) ((right - x) / sx), 0, data.width());
        int y0 = clamp((int) ((top - y) / sy), 0, data.height());
        int y1 = clamp((int) ((bottom - y) / sy), 0, data.height());

        return (long) (x1 - x0) * (y1 - y0);
    }

    private boolean scan(
            GraphicMediaCache.CachedImage src,
            double sxPos, double syPos,
            double sw, double sh,
            GraphicTextureItem other,
            GraphicMediaCache.CachedImage otherData,
            double ox, double oy,
            double ow, double oh,
            double left, double top,
            double right, double bottom
    ) {
        BitSet bits = src != null ? src.opaque() : null;
        if (bits == null || src.width() <= 0 || src.height() <= 0 || sw <= 0 || sh <= 0) {
            return true;
        }

        double sx = sw / src.width();
        double sy = sh / src.height();

        if (sx <= 0 || sy <= 0) return true;

        int x0 = clamp((int) ((left - sxPos) / sx), 0, src.width());
        int x1 = clamp((int) ((right - sxPos) / sx), 0, src.width());
        int y0 = clamp((int) ((top - syPos) / sy), 0, src.height());
        int y1 = clamp((int) ((bottom - syPos) / sy), 0, src.height());

        for (int y = y0; y < y1; y++) {
            int row = y * src.width();
            for (int i = bits.nextSetBit(row + x0); i >= 0 && i < row + x1; i = bits.nextSetBit(i + 1)) {
                int x = i - row;

                double wx = sxPos + (x + 0.5) * sx;
                double wy = syPos + (y + 0.5) * sy;

                if (isOpaque(other, otherData, ox, oy, ow, oh, wx, wy)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isOpaque(
            GraphicTextureItem item,
            GraphicMediaCache.CachedImage data,
            double x, double y,
            double w, double h,
            double wx, double wy
    ) {
        if (wx < x || wy < y || wx >= x + w || wy >= y + h) return false;

        if (data == null || data.opaque() == null || data.width() <= 0 || data.height() <= 0 || w <= 0 || h <= 0) {
            return true;
        }

        int sx = (int) ((wx - x) * data.width() / w);
        int sy = (int) ((wy - y) * data.height() / h);

        if (sx < 0 || sy < 0 || sx >= data.width() || sy >= data.height()) {
            return false;
        }

        BitSet opaque = data.opaque();
        return opaque.get(sy * data.width() + sx);
    }

    private boolean aabbIntersectsAt(GraphicItem other, double x, double y, double w, double h) {
        double ox = other.getPosition().getX();
        double oy = other.getPosition().getY();

        double ow = other.getWidth();
        double oh = other.getHeight();

        if (other instanceof GraphicTextureItem tex) {
            GraphicMediaCache.CachedImage d = tex.currentData();
            ow = tex.effectiveWidth(d);
            oh = tex.effectiveHeight(d);
        }

        if (w <= 0 || h <= 0 || ow <= 0 || oh <= 0) {
            return false;
        }

        return x < ox + ow &&
                x + w > ox &&
                y < oy + oh &&
                y + h > oy;
    }

    protected double effectiveWidth(GraphicMediaCache.CachedImage d) {
        double w = getWidth();
        if (w > 0) return w;
        return d != null ? d.width() : 0;
    }

    protected double effectiveHeight(GraphicMediaCache.CachedImage d) {
        double h = getHeight();
        if (h > 0) return h;
        return d != null ? d.height() : 0;
    }

    @Override
    protected Shape createCollisionShape() {
        return new Rectangle(effectiveWidth(data), effectiveHeight(data));
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : Math.min(v, max);
    }

    public abstract static class GraphicTextureBuilder<T extends GraphicTextureItem, B extends GraphicTextureBuilder<T, B>>
            extends GraphicItemBuilder<T, B>
            implements Moveable<B> {

        @Override
        protected void configure(T item) {
            super.configure(item);
        }
    }
}