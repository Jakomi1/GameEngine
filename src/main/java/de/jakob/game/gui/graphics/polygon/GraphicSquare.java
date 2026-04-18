package de.jakob.game.gui.graphics.polygon;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class GraphicSquare extends GraphicPolygon {

    protected GraphicSquare() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public GraphicSquare size(double size) {
        super.size(size, size);
        return this;
    }

    @Override
    protected Shape createShape() {
        return new Rectangle(Math.max(0, width), Math.max(0, height));
    }

    public static class Builder extends GraphicPolygonBuilder<GraphicSquare, Builder> {

        private Color fillColor = NamedColor.RED;
        private Color strokeColor;
        private double strokeWidth = 0;

        public Builder size(double size) {
            super.size(size, size);
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
        protected GraphicSquare create() {
            return new GraphicSquare();
        }

        @Override
        protected void configure(GraphicSquare item) {
            item.color(fillColor);
            if (strokeColor != null && strokeWidth > 0) {
                item.stroke(strokeColor, strokeWidth);
            }
        }
    }
}