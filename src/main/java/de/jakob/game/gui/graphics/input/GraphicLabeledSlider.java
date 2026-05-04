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
    private Font textFont = Font.getDefault();
    private double spacing = 4;

    private boolean borderEnabled = false;
    private Color borderColor = NamedColor.BLACK;
    private double borderWidth = 1;

    public GraphicLabeledSlider text(String text) {
        this.labelText = text != null ? text : "";
        if (label != null) {
            label.setText(this.labelText);
        }
        return this;
    }

    public GraphicLabeledSlider textFont(Font font) {
        if (font != null) {
            this.textFont = font;
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
        label.setFont(textFont);
        label.setPadding(Insets.EMPTY);

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

        if (root == null) return;

        if (label != null) {
            Color effectiveText = enabled
                    ? textColor
                    : textColor.darker(0.4);

            if (effectiveText != null) {
                label.setTextFill(effectiveText.toFX());
            }

            if (textFont != null) {
                label.setFont(textFont);
            }
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

    @SuppressWarnings("unchecked")
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GraphicSlider.Builder<GraphicLabeledSlider, Builder> {

        private String labelText;
        private Font textFont;
        private Double spacing;

        private Boolean borderEnabled;
        private Color borderColor;
        private Double borderWidth;

        public Builder text(String text) {
            this.labelText = text;
            return this;
        }

        public Builder textFont(Font font) {
            this.textFont = font;
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

            if (labelText != null) item.text(labelText);
            if (textFont != null) item.textFont(textFont);
            if (spacing != null) item.spacing(spacing);

            if (borderEnabled != null) {
                item.border(borderEnabled);
            }

            if (Boolean.TRUE.equals(borderEnabled) && borderColor != null && borderWidth != null) {
                item.border(borderColor, borderWidth);
            }
        }

        @Override
        protected GraphicLabeledSlider create() {
            return new GraphicLabeledSlider();
        }
    }
}