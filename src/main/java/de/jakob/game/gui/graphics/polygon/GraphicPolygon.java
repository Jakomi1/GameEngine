package de.jakob.game.gui.graphics.polygon;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.graphics.GraphicItem;
import de.jakob.game.gui.graphics.Moveable;
import javafx.scene.Node;
import javafx.scene.shape.Shape;

import java.util.Objects;

public abstract class GraphicPolygon extends GraphicItem implements Moveable<GraphicPolygon> {

    protected Shape shape;

    protected Color fillColor = NamedColor.RED;
    protected Color strokeColor;
    protected double strokeWidth = 0;

    private boolean built = false;
    private boolean dirtyShape = true;
    private boolean dirtyStyle = true;

    @Override
    public GraphicPolygon size(double width, double height) {
        if (Double.compare(this.width, width) == 0 && Double.compare(this.height, height) == 0) {
            return this;
        }

        super.size(width, height);
        dirtyShape = true;

        if (built) {
            refresh();
        }

        return this;
    }

    public GraphicPolygon color(Color color) {
        Color used = color != null ? color : NamedColor.RED;
        if (Objects.equals(this.fillColor, used)) {
            return this;
        }

        this.fillColor = used;
        dirtyStyle = true;

        if (built) {
            applyStyle();
        }

        return this;
    }

    public GraphicPolygon stroke(Color color, double width) {
        double usedWidth = Math.max(0, width);

        if (Objects.equals(this.strokeColor, color) && Double.compare(this.strokeWidth, usedWidth) == 0) {
            return this;
        }

        this.strokeColor = color;
        this.strokeWidth = usedWidth;
        dirtyStyle = true;

        if (built) {
            applyStyle();
        }

        return this;
    }

    protected void applyStyle() {
        if (shape == null) return;

        shape.setFill(fillColor.toFX());

        if (strokeColor != null && strokeWidth > 0) {
            shape.setStroke(strokeColor.toFX());
            shape.setStrokeWidth(strokeWidth);
        } else {
            shape.setStroke(null);
        }
    }

    @Override
    protected void setNode(Node node) {
        super.setNode(node);

        if (node instanceof Shape s) {
            this.shape = s;
            applyStyle();
        }
    }

    protected abstract Shape createShape();

    private void refresh() {
        if (dirtyShape || shape == null) {
            Shape newShape = createShape();
            this.shape = newShape;
            setNode(newShape);
            dirtyShape = false;
        }

        if (dirtyStyle) {
            applyStyle();
            dirtyStyle = false;
        }

        applyPosition();
    }

    @Override
    public void build() {
        built = true;
        refresh();
    }

    // --- INNERER BUILDER ---

    /**
     * Ein generischer Builder für alle Polygon-Typen.
     * Erbt vom GraphicItemBuilder und implementiert Moveable für die Builder-Kette.
     */
    public abstract static class GraphicPolygonBuilder<T extends GraphicPolygon, B extends GraphicPolygonBuilder<T, B>>
            extends GraphicItemBuilder<T, B>
            implements Moveable<B> {

        protected Color fillColor = NamedColor.RED;
        protected Color strokeColor;
        protected double strokeWidth = 0;

        @SuppressWarnings("unchecked")
        public B color(Color color) {
            this.fillColor = color != null ? color : NamedColor.RED;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B stroke(Color color, double width) {
            this.strokeColor = color;
            this.strokeWidth = Math.max(0, width);
            return (B) this;
        }

        @Override
        protected void configure(T item) {
            super.configure(item);
            // Übertrage die Polygon-spezifischen Werte an das Item
            item.color(fillColor);
            if (strokeColor != null && strokeWidth > 0) {
                item.stroke(strokeColor, strokeWidth);
            }
        }
    }
}