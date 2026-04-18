package de.jakob.game.gui.graphics.polygon;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class GraphicTriangle extends GraphicPolygon {

    protected GraphicTriangle() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Shape createShape() {
        return new Polygon(
                0.0, height,
                width / 2.0, 0.0,
                width, height
        );
    }

    public static class Builder extends GraphicPolygonBuilder<GraphicTriangle, Builder> {

        private Color fillColor = NamedColor.RED;
        private Color strokeColor;
        private double strokeWidth = 0;

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
        protected GraphicTriangle create() {
            return new GraphicTriangle();
        }

        @Override
        protected void configure(GraphicTriangle item) {
            item.color(fillColor);
            if (strokeColor != null && strokeWidth > 0) {
                item.stroke(strokeColor, strokeWidth);
            }
        }
    }
}