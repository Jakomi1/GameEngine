package de.jakob.game.gui.graphics.basic;

import de.jakob.game.color.Color;
import de.jakob.game.font.FontUsable;
import de.jakob.game.gui.graphics.GraphicItem;
import de.jakob.game.gui.graphics.Moveable;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.Objects;

public class GraphicText extends GraphicItem implements FontUsable<GraphicText>, Moveable<GraphicText> {

    private final Label label;

    private String text = "";

    private String fontFamily = FontUsable.DEFAULT_FONT.getFamily();
    private double fontSize = FontUsable.DEFAULT_FONT.getSize();

    private boolean bold = false;
    private boolean italic = false;

    private Color color = Color.fromRGB(255, 255, 255);

    private boolean built = false;
    private boolean dirtyContent = true;
    private boolean dirtySize = true;
    private boolean dirtyFont = true;
    private boolean dirtyColor = true;

    private GraphicText() {
        this.label = new Label();
        setNode(label);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public GraphicText size(double width, double height) {
        if (Double.compare(this.width, width) == 0 && Double.compare(this.height, height) == 0) {
            return this;
        }

        super.size(width, height);
        dirtySize = true;

        if (built) {
            refresh();
        }

        return this;
    }

    private void markDirty() {
        dirtyContent = true;
        dirtySize = true;
        dirtyFont = true;
        dirtyColor = true;

        if (built) {
            refresh();
        }
    }

    private void refresh() {
        if (label == null) {
            return;
        }

        if (dirtyContent || dirtyFont || dirtyColor) {
            label.setText(text);
            label.setFont(buildFont());
            label.setTextFill(color.toFX());
        }

        if (dirtySize) {
            refreshSize();
        }

        applyPosition();

        dirtyContent = false;
        dirtyFont = false;
        dirtyColor = false;
        dirtySize = false;
    }

    private void refreshSize() {
        double w = width;
        double h = height;

        if (w <= 0 || h <= 0) {
            double computedW = label.prefWidth(-1);
            double computedH = label.prefHeight(computedW > 0 ? computedW : -1);

            if (w <= 0) {
                w = computedW;
            }

            if (h <= 0) {
                h = computedH;
            }
        }

        if (w < 0) {
            w = 0;
        }
        if (h < 0) {
            h = 0;
        }

        if (Double.compare(this.width, w) != 0 || Double.compare(this.height, h) != 0) {
            this.width = w;
            this.height = h;
            recalcPosition();
        }
    }

    public GraphicText text(String text) {
        String value = text != null ? text : "";
        if (Objects.equals(this.text, value)) {
            return this;
        }

        this.text = value;
        markDirty();
        return this;
    }

    @Override
    public GraphicText font(Font font) {
        if (font != null) {
            String family = font.getFamily();
            double size = font.getSize();

            if (Objects.equals(this.fontFamily, family) && Double.compare(this.fontSize, size) == 0) {
                return this;
            }

            this.fontFamily = family;
            this.fontSize = size;
            markDirty();
        }
        return this;
    }

    @Override
    public Font getFont() {
        return buildFont();
    }

    public GraphicText fontSize(double size) {
        if (size > 0 && Double.compare(this.fontSize, size) != 0) {
            this.fontSize = size;
            markDirty();
        }
        return this;
    }

    public GraphicText bold(boolean value) {
        if (this.bold != value) {
            this.bold = value;
            markDirty();
        }
        return this;
    }

    public GraphicText italic(boolean value) {
        if (this.italic != value) {
            this.italic = value;
            markDirty();
        }
        return this;
    }

    public GraphicText color(Color color) {
        Color used = color != null ? color : Color.fromRGB(255, 255, 255);
        if (Objects.equals(this.color, used)) {
            return this;
        }

        this.color = used;
        dirtyColor = true;

        if (built) {
            refresh();
        }

        return this;
    }

    @Override
    public void applyFont() {
        label.setFont(buildFont());
    }

    @Override
    public void build() {
        built = true;
        refresh();
    }

    private Font buildFont() {
        FontWeight weight = bold ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = italic ? FontPosture.ITALIC : FontPosture.REGULAR;
        return Font.font(fontFamily, weight, posture, fontSize);
    }

    public static class Builder extends GraphicItemBuilder<GraphicText, Builder> implements Moveable<GraphicText> {

        private String text = "";
        private Font font;
        private Double fontSize;
        private Boolean bold;
        private Boolean italic;
        private Color color;

        public Builder text(String text) {
            this.text = text != null ? text : "";
            return this;
        }

        public Builder font(Font font) {
            this.font = font;
            return this;
        }

        public Builder fontSize(double size) {
            this.fontSize = size;
            return this;
        }

        public Builder bold(boolean value) {
            this.bold = value;
            return this;
        }

        public Builder italic(boolean value) {
            this.italic = value;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        @Override
        protected GraphicText create() {
            GraphicText t = new GraphicText();

            t.text(text);

            if (font != null) t.font(font);
            if (fontSize != null) t.fontSize(fontSize);
            if (bold != null) t.bold(bold);
            if (italic != null) t.italic(italic);
            if (color != null) t.color(color);

            return t;
        }
    }
}