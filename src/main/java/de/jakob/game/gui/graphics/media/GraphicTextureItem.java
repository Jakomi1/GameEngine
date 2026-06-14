package de.jakob.game.gui.graphics.media;

import de.jakob.game.gui.graphics.Draggable;
import de.jakob.game.gui.graphics.GraphicItem;
import de.jakob.game.scheduler.GameScheduler;
import javafx.geometry.Point2D;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.BitSet;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class GraphicTextureItem extends GraphicItem implements Draggable<GraphicTextureItem> {

    protected final ImageView view = new ImageView();
    protected volatile GraphicMediaCache.CachedImage data;
    private double scaleFactor = 1.0;
    private GraphicMediaCache.CachedImage overrideTexture;
    private GameScheduler.ScheduledTask overrideTask;
    private static final Map<GraphicMediaCache.CachedImage, OpaqueBounds> OPAQUE_BOUNDS_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    protected GraphicTextureItem() {
        setNode(view);
        view.setPreserveRatio(false);
        view.setSmooth(false);
        view.setCache(true);
    }

    protected final void apply(GraphicMediaCache.CachedImage cached) {
        this.data = cached;
        view.setImage(cached != null ? cached.image() : null);
        updateScale();
    }

    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        updateScale();
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    private void updateScale() {
        syncViewSize(getWidth(), getHeight());
    }

    protected final void syncViewSize(double width, double height) {
        view.setFitWidth(Math.max(0, width));
        view.setFitHeight(Math.max(0, height));
    }

    protected GraphicMediaCache.CachedImage currentData() {
        return overrideTexture != null ? overrideTexture : data;
    }

    @Override
    public double getWidth() {
        if (width > 0) return width;
        GraphicMediaCache.CachedImage d = currentData();
        if (d != null && d.width() > 0) return d.width() * scaleFactor;
        return super.getWidth();
    }

    @Override
    public double getHeight() {
        if (height > 0) return height;
        GraphicMediaCache.CachedImage d = currentData();
        if (d != null && d.height() > 0) return d.height() * scaleFactor;
        return super.getHeight();
    }

    @Override
    public boolean touches(GraphicItem other) {
        return preciseTouches(other);
    }

    public boolean preciseTouches(GraphicItem other) {
        Point2D p = getPointPosition();
        return preciseTouchesAt(other, p.getX(), p.getY());
    }

    public boolean preciseTouchesAt(GraphicItem other, double thisX, double thisY) {
        if (other == null || other == this) return false;

        final GraphicMediaCache.CachedImage a = currentData();
        final double aw = getWidth();
        final double ah = getHeight();

        if (a == null || aw <= 0 || ah <= 0) {
            return false;
        }

        if (!(other instanceof GraphicTextureItem otherTex)) {
            return coarseOpaqueIntersectsAt(other, thisX, thisY, a, aw, ah);
        }

        final GraphicMediaCache.CachedImage b = otherTex.currentData();
        final double bx = other.getPointPosition().getX();
        final double by = other.getPointPosition().getY();
        final double bw = otherTex.getWidth();
        final double bh = otherTex.getHeight();

        if (b == null || bw <= 0 || bh <= 0) {
            return false;
        }

        final Rect wa = opaqueWorldBounds(a, thisX, thisY, aw, ah);
        final Rect wb = opaqueWorldBounds(b, bx, by, bw, bh);

        if (wa == null || wb == null) {
            return false;
        }

        final double left = Math.max(wa.left, wb.left);
        final double top = Math.max(wa.top, wb.top);
        final double right = Math.min(wa.right, wb.right);
        final double bottom = Math.min(wa.bottom, wb.bottom);

        if (left >= right || top >= bottom) return false;

        final long areaA = estimate(a, aw, ah, left, top, right, bottom, thisX, thisY);
        final long areaB = estimate(b, bw, bh, left, top, right, bottom, bx, by);

        return areaA <= areaB
                ? scan(a, thisX, thisY, aw, ah, otherTex, b, bx, by, bw, bh, left, top, right, bottom)
                : scan(b, bx, by, bw, bh, this, a, thisX, thisY, aw, ah, left, top, right, bottom);
    }

    private boolean coarseOpaqueIntersectsAt(
            GraphicItem other,
            double thisX, double thisY,
            GraphicMediaCache.CachedImage a,
            double aw, double ah
    ) {
        Rect wa = opaqueWorldBounds(a, thisX, thisY, aw, ah);
        if (wa == null) return false;

        double ox = other.getPointPosition().getX();
        double oy = other.getPointPosition().getY();
        double ow = other.getWidth();
        double oh = other.getHeight();

        if (ow <= 0 || oh <= 0) return false;

        return intersects(
                wa.left, wa.top, wa.right - wa.left, wa.bottom - wa.top,
                ox, oy, ow, oh
        );
    }

    private long estimate(
            GraphicMediaCache.CachedImage data,
            double w, double h,
            double left, double top,
            double right, double bottom,
            double x, double y
    ) {
        OpaqueBounds bounds = opaqueBoundsOf(data);
        if (data == null || bounds == null || data.width() <= 0 || data.height() <= 0 || w <= 0 || h <= 0) {
            return Long.MAX_VALUE;
        }

        double sx = w / data.width();
        double sy = h / data.height();
        if (sx <= 0 || sy <= 0) return Long.MAX_VALUE;

        int x0 = clamp((int) ((left - x) / sx), 0, data.width());
        int x1 = clamp((int) ((right - x) / sx), 0, data.width());
        int y0 = clamp((int) ((top - y) / sy), 0, data.height());
        int y1 = clamp((int) ((bottom - y) / sy), 0, data.height());

        return (long) Math.max(0, x1 - x0) * Math.max(0, y1 - y0);
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
            return false;
        }

        double sx = sw / src.width();
        double sy = sh / src.height();
        if (sx <= 0 || sy <= 0) return false;

        int x0 = clamp((int) ((left - sxPos) / sx), 0, src.width());
        int x1 = clamp((int) ((right - sxPos) / sx), 0, src.width());
        int y0 = clamp((int) ((top - syPos) / sy), 0, src.height());
        int y1 = clamp((int) ((bottom - syPos) / sy), 0, src.height());

        if (x0 >= x1 || y0 >= y1) {
            return false;
        }

        for (int y = y0; y < y1; y++) {
            int row = y * src.width();
            for (int i = bits.nextSetBit(row + x0); i >= 0 && i < row + x1; i = bits.nextSetBit(i + 1)) {
                int x = i - row;

                double wx = sxPos + (x + 0.5) * sx;
                double wy = syPos + (y + 0.5) * sy;

                if (isOpaque(otherData, ox, oy, ow, oh, wx, wy)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isOpaque(
            GraphicMediaCache.CachedImage data,
            double x, double y,
            double w, double h,
            double wx, double wy
    ) {
        if (wx < x || wy < y || wx >= x + w || wy >= y + h) return false;

        if (data == null || data.width() <= 0 || data.height() <= 0 || w <= 0 || h <= 0) {
            return false;
        }

        BitSet opaque = data.opaque();
        if (opaque == null) {
            return false;
        }

        int sx = (int) ((wx - x) * data.width() / w);
        int sy = (int) ((wy - y) * data.height() / h);

        if (sx < 0 || sy < 0 || sx >= data.width() || sy >= data.height()) {
            return false;
        }

        return opaque.get(sy * data.width() + sx);
    }

    private Rect opaqueWorldBounds(
            GraphicMediaCache.CachedImage data,
            double x, double y,
            double w, double h
    ) {
        OpaqueBounds bounds = opaqueBoundsOf(data);
        if (data == null || bounds == null || data.width() <= 0 || data.height() <= 0 || w <= 0 || h <= 0) {
            return null;
        }

        double sx = w / data.width();
        double sy = h / data.height();

        double left = x + bounds.minX * sx;
        double top = y + bounds.minY * sy;
        double right = x + (bounds.maxX + 1) * sx;
        double bottom = y + (bounds.maxY + 1) * sy;

        return new Rect(left, top, right, bottom);
    }

    private OpaqueBounds opaqueBoundsOf(GraphicMediaCache.CachedImage data) {
        if (data == null || data.width() <= 0 || data.height() <= 0 || data.opaque() == null) {
            return null;
        }

        OpaqueBounds cached = OPAQUE_BOUNDS_CACHE.get(data);
        if (cached != null) {
            return cached.isEmpty() ? null : cached;
        }

        BitSet bits = data.opaque();
        int w = data.width();
        int h = data.height();

        int first = bits.nextSetBit(0);
        if (first < 0) {
            OpaqueBounds empty = OpaqueBounds.empty();
            OPAQUE_BOUNDS_CACHE.put(data, empty);
            return null;
        }

        int minX = w;
        int minY = h;
        int maxX = -1;
        int maxY = -1;

        for (int i = first; i >= 0; i = bits.nextSetBit(i + 1)) {
            int x = i % w;
            int y = i / w;

            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        OpaqueBounds bounds = new OpaqueBounds(minX, minY, maxX, maxY);
        OPAQUE_BOUNDS_CACHE.put(data, bounds);
        return bounds;
    }

    private boolean aabbIntersectsAt(GraphicItem other, double x, double y, double w, double h) {
        double ox = other.getPointPosition().getX();
        double oy = other.getPointPosition().getY();
        double ow = other.getWidth();
        double oh = other.getHeight();

        if (w <= 0 || h <= 0 || ow <= 0 || oh <= 0) {
            return false;
        }

        return intersects(x, y, w, h, ox, oy, ow, oh);
    }

    @Override
    protected Shape createCollisionShape() {
        GraphicMediaCache.CachedImage d = currentData();
        OpaqueBounds bounds = opaqueBoundsOf(d);

        if (d == null || bounds == null || d.width() <= 0 || d.height() <= 0) {
            return new Rectangle(Math.max(0, getWidth()), Math.max(0, getHeight()));
        }

        double w = getWidth();
        double h = getHeight();

        if (w <= 0 || h <= 0) {
            return new Rectangle();
        }

        double sx = w / d.width();
        double sy = h / d.height();

        return new Rectangle(
                Math.max(0, bounds.minX * sx),
                Math.max(0, bounds.minY * sy),
                Math.max(0, (bounds.maxX - bounds.minX + 1) * sx),
                Math.max(0, (bounds.maxY - bounds.minY + 1) * sy)
        );
    }

    private static boolean intersects(
            double x1, double y1, double w1, double h1,
            double x2, double y2, double w2, double h2
    ) {
        return x1 < x2 + w2 &&
                x1 + w1 > x2 &&
                y1 < y2 + h2 &&
                y1 + h1 > y2;
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : Math.min(v, max);
    }

    private static final class Rect {
        final double left;
        final double top;
        final double right;
        final double bottom;

        Rect(double left, double top, double right, double bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    private static final class OpaqueBounds {
        final int minX;
        final int minY;
        final int maxX;
        final int maxY;

        private final boolean empty;

        OpaqueBounds(int minX, int minY, int maxX, int maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
            this.empty = false;
        }

        private OpaqueBounds() {
            this.minX = 0;
            this.minY = 0;
            this.maxX = -1;
            this.maxY = -1;
            this.empty = true;
        }

        static OpaqueBounds empty() {
            return new OpaqueBounds();
        }

        boolean isEmpty() {
            return empty;
        }
    }

    public abstract static class GraphicTextureBuilder<T extends GraphicTextureItem, B extends GraphicTextureBuilder<T, B>>
            extends GraphicItemBuilder<T, B>
            implements Draggable<B> {

        private double scaleFactor = 1.0;

        protected GraphicTextureBuilder() {
            this.width = -1;
            this.height = -1;
        }

        @SuppressWarnings("unchecked")
        public B scale(double scaleFactor) {
            this.scaleFactor = scaleFactor;
            return (B) this;
        }

        @Override
        protected void configure(T item) {
            super.configure(item);
            item.setScaleFactor(scaleFactor);
        }
    }

    public void overwriteTextureFor(String path, GameScheduler scheduler, long ticks) {
        if (path == null || path.isBlank()) return;

        if (overrideTask != null) {
            overrideTask.cancel();
        }

        GraphicMediaCache.CachedImage newTex = GraphicMediaCache.texture(path);
        this.overrideTexture = newTex;

        if (newTex != null) {
            view.setImage(newTex.image());
        } else {
            view.setImage(null);
        }

        if (scheduler != null && ticks > 0) {
            overrideTask = scheduler.runLater(() -> {
                overrideTexture = null;

                GraphicMediaCache.CachedImage current = currentData();
                if (current != null) {
                    view.setImage(current.image());
                } else {
                    view.setImage(null);
                }

                overrideTask = null;
            }, ticks);
        }
    }
}