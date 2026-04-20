package de.jakob.game.gui.graphics.input;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class GraphicLabeledSlider extends GraphicSlider {

    private Label label;
    private VBox root;

    private String labelText = "";
    private Color labelColor;
    private Font labelFont = Font.getDefault();
    private double spacing = 4;

    private boolean borderEnabled = false;
    private Color borderColor = NamedColor.BLACK;
    private double borderWidth = 1;

    public GraphicLabeledSlider label(String text) {
        this.labelText = text != null ? text : "";
        if (label != null) {
            label.setText(this.labelText);
        }
        return this;
    }

    public GraphicLabeledSlider labelColor(Color color) {
        this.labelColor = color;
        if (label != null && color != null) {
            label.setTextFill(color.toFX());
        }
        return this;
    }

    public GraphicLabeledSlider labelFont(Font font) {
        if (font != null) {
            this.labelFont = font;
            if (label != null) {
                label.setFont(font);
            }
        }
        return this;
    }

    public GraphicLabeledSlider spacing(double spacing) {
        this.spacing = spacing;
        if (root != null) {
            root.setSpacing(spacing);
        }
        return this;
    }

    public GraphicLabeledSlider border(boolean enabled) {
        this.borderEnabled = enabled;
        if (root != null) {
            applyStyle();
        }
        return this;
    }

    public GraphicLabeledSlider border(Color color, double width) {
        this.borderEnabled = true;
        this.borderColor = color != null ? color : NamedColor.BLACK;
        this.borderWidth = width;
        if (root != null) {
            applyStyle();
        }
        return this;
    }

    public GraphicLabeledSlider noBorder() {
        return border(false);
    }

    @Override
    public void build() {
        super.build();

        label = new Label(labelText);
        label.setFont(labelFont);
        label.setPadding(Insets.EMPTY);

        if (labelColor != null) {
            label.setTextFill(labelColor.toFX());
        }

        root = new VBox(label, getSlider());
        root.setSpacing(spacing);
        root.setPadding(Insets.EMPTY);

        if (width > 0) root.setPrefWidth(width);
        if (height > 0) root.setPrefHeight(height);

        setNode(root);
        applyStyle();
    }

    @Override
    protected void applyStyle() {
        super.applyStyle();

        if (root == null) {
            return;
        }

        if (borderEnabled && borderWidth > 0) {
            root.setStyle(
                    "-fx-border-color: " + toRgb(borderColor) + ";" +
                            "-fx-border-width: " + borderWidth + ";"
            );
        } else {
            root.setStyle("-fx-border-color: transparent; -fx-border-width: 0;");
        }
    }

    private String toRgb(Color color) {
        javafx.scene.paint.Color fx = color.toFX();
        return String.format("rgba(%d,%d,%d,%f)",
                (int) (fx.getRed() * 255),
                (int) (fx.getGreen() * 255),
                (int) (fx.getBlue() * 255),
                fx.getOpacity()
        );
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GraphicSlider.Builder<GraphicLabeledSlider, Builder> {

        private String labelText;
        private Color labelColor;
        private Font labelFont;
        private Double spacing;

        private Boolean borderEnabled;
        private Color borderColor;
        private Double borderWidth;

        public Builder label(String text) {
            this.labelText = text;
            return this;
        }

        public Builder labelColor(Color color) {
            this.labelColor = color;
            return this;
        }

        public Builder labelFont(Font font) {
            this.labelFont = font;
            return this;
        }

        public Builder spacing(double spacing) {
            this.spacing = spacing;
            return this;
        }

        public Builder border(boolean enabled) {
            this.borderEnabled = enabled;
            return this;
        }

        public Builder border(Color color, double width) {
            this.borderEnabled = true;
            this.borderColor = color;
            this.borderWidth = width;
            return this;
        }

        public Builder noBorder() {
            this.borderEnabled = false;
            this.borderColor = null;
            this.borderWidth = null;
            return this;
        }

        @Override
        protected void configure(GraphicLabeledSlider item) {
            super.configure(item);

            if (labelText != null) item.label(labelText);
            if (labelColor != null) item.labelColor(labelColor);
            if (labelFont != null) item.labelFont(labelFont);
            if (spacing != null) item.spacing(spacing);

            if (borderEnabled != null) {
                item.border(borderEnabled);
            }

            if (Boolean.TRUE.equals(borderEnabled) && borderColor != null && borderWidth != null) {
                item.border(borderColor, borderWidth);
            }

            if (borderColor != null && borderWidth != null) {
                item.border(borderColor, borderWidth);
            }
        }

        @Override
        protected GraphicLabeledSlider create() {
            return new GraphicLabeledSlider();
        }
    }
}