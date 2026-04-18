package de.jakob.game.gui.graphics.polygon;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class GraphicRectangle extends GraphicPolygon {

    protected double arcWidth = 0;
    protected double arcHeight = 0;

    protected GraphicRectangle() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public GraphicRectangle rounded(double arcWidth, double arcHeight) {
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        applyStyle();
        return this;
    }

    @Override
    protected Shape createShape() {
        Rectangle rect = new Rectangle(Math.max(0, width), Math.max(0, height));

        if (arcWidth > 0 || arcHeight > 0) {
            rect.setArcWidth(arcWidth);
            rect.setArcHeight(arcHeight);
        }

        return rect;
    }

    public static class Builder extends GraphicPolygonBuilder<GraphicRectangle, Builder> {

        private double arcWidth = 0;
        private double arcHeight = 0;
        private Color fillColor = NamedColor.RED;
        private Color strokeColor;
        private double strokeWidth = 0;

        public Builder rounded(double arcWidth, double arcHeight) {
            this.arcWidth = arcWidth;
            this.arcHeight = arcHeight;
            return this;
        }

        public Builder color(Color color) {
            this.fillColor = color != null ? color : NamedColor.RED;
            return this;
        }

        public Builder stroke(Color color, double width) {
            this.strokeColor = color;
            this.strokeWidth = Math.max(0, width);
            return this;
        }

        @Override
        protected GraphicRectangle create() {
            return new GraphicRectangle();
        }

        @Override
        protected void configure(GraphicRectangle item) {
            item.rounded(arcWidth, arcHeight);
            item.color(fillColor);
            if (strokeColor != null && strokeWidth > 0) {
                item.stroke(strokeColor, strokeWidth);
            }
        }
    }
}