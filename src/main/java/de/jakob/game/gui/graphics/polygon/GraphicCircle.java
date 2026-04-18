package de.jakob.game.gui.graphics.polygon;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
public class GraphicCircle extends GraphicPolygon {

    private double radius;

    protected GraphicCircle() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public GraphicCircle radius(double radius) {
        this.radius = Math.max(0, radius);
        return this;
    }

    @Override
    protected Shape createShape() {
        Circle circle = new Circle(radius);
        circle.setCenterX(radius);
        circle.setCenterY(radius);
        return circle;
    }

    @Override
    public void build() {
        super.size(radius * 2, radius * 2);
        super.build();
    }

    public static class Builder extends GraphicPolygonBuilder<GraphicCircle, Builder> {

        private double radius;
        private Color fillColor = NamedColor.RED;
        private Color strokeColor;
        private double strokeWidth = 0;

        public Builder radius(double radius) {
            this.radius = Math.max(0, radius);
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
        protected GraphicCircle create() {
            return new GraphicCircle();
        }

        @Override
        protected void configure(GraphicCircle item) {
            item.radius(radius);
            item.color(fillColor);
            if (strokeColor != null && strokeWidth > 0) {
                item.stroke(strokeColor, strokeWidth);
            }
        }
    }
}