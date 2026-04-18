package de.jakob.game.gui.graphics.input;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.graphics.GraphicItem;

import java.util.function.Consumer;

public abstract class GraphicInput<T> extends GraphicItem {

    protected T value;
    protected Consumer<T> onChange;

    protected boolean enabled = true;
    protected int maxCharacters = 16;

    protected Color backgroundColor = NamedColor.WHITE;
    protected Color textColor = NamedColor.BLACK;
    protected Color borderColor = NamedColor.GRAY;

    protected double borderRadius = 4;
    protected boolean transparentBackground = false;


    public T getValue() {
        return value;
    }

    public GraphicInput<T> value(T value) {
        this.value = value;
        applyValueToNode();
        return this;
    }

    public GraphicInput<T> onChange(Consumer<T> consumer) {
        this.onChange = consumer;
        return this;
    }

    protected void notifyChange(T newValue) {
        this.value = newValue;
        if (onChange != null) onChange.accept(newValue);
    }

    public GraphicInput<T> enable() {
        this.enabled = true;
        applyEnabledState();
        return this;
    }

    public GraphicInput<T> disable() {
        this.enabled = false;
        applyEnabledState();
        return this;
    }

    protected void applyEnabledState() {
        if (node == null) return;

        node.setDisable(!enabled);
        applyStyle();
    }

    public GraphicInput<T> backgroundColor(Color c) {
        this.backgroundColor = c;
        applyStyle();
        return this;
    }

    public GraphicInput<T> textColor(Color c) {
        this.textColor = c;
        applyStyle();
        return this;
    }

    public GraphicInput<T> borderColor(Color c) {
        this.borderColor = c;
        applyStyle();
        return this;
    }

    public GraphicInput<T> borderRadius(double r) {
        this.borderRadius = r;
        applyStyle();
        return this;
    }

    public GraphicInput<T> transparentBackground() {
        this.transparentBackground = true;
        applyStyle();
        return this;
    }

    protected void applyStyle() {
        if (node == null) return;

        Color bg = enabled ? backgroundColor : backgroundColor.darker(0.3);
        Color text = enabled ? textColor : textColor.darker(0.4);

        String bgCss = transparentBackground ? "transparent" : bg.toCSS();

        node.setStyle(
                "-fx-background-color: " + bgCss + ";" +
                        "-fx-text-fill: " + text.toCSS() + ";" +
                        "-fx-border-color: " + borderColor.toCSS() + ";" +
                        "-fx-border-radius: " + borderRadius + ";" +
                        "-fx-background-radius: " + borderRadius + ";"
        );
    }


    public GraphicInput<T> maxCharacters(int max) {
        this.maxCharacters = Math.max(1, max);
        return this;
    }

    protected boolean exceedsMaxLength(String text) {
        return text != null && text.length() > maxCharacters;
    }

    protected abstract void applyValueToNode();

    @SuppressWarnings("unchecked")
    public abstract static class GraphicInputBuilder<T extends GraphicInput<V>, B extends GraphicInputBuilder<T, B, V>, V>
            extends GraphicItemBuilder<T, B> {

        protected V value;
        protected Consumer<V> onChange;
        protected Integer maxCharacters;
        protected Boolean enabled;

        protected Color backgroundColor;
        protected Color textColor;
        protected Color borderColor;
        protected Double borderRadius;
        protected Boolean transparent;

        public B value(V value) { this.value = value; return (B) this; }

        public B onChange(Consumer<V> onChange) { this.onChange = onChange; return (B) this; }
        public B maxCharacters(int max) { this.maxCharacters = max; return (B) this; }
        public B enable() { this.enabled = true; return (B) this; }
        public B disable() { this.enabled = false; return (B) this; }
        public B backgroundColor(Color c) { this.backgroundColor = c; return (B) this; }
        public B textColor(Color c) { this.textColor = c; return (B) this; }
        public B borderColor(Color c) { this.borderColor = c; return (B) this; }
        public B borderRadius(double r) { this.borderRadius = r; return (B) this; }
        public B transparentBackground() { this.transparent = true; return (B) this; }

        protected void applyBase(T item) {
            if (value != null) item.value(value);
            if (onChange != null) item.onChange(onChange);
            if (maxCharacters != null) item.maxCharacters(maxCharacters);

            if (enabled != null) {
                if (enabled) item.enable();
                else item.disable();
            }

            if (backgroundColor != null) item.backgroundColor(backgroundColor);
            if (textColor != null) item.textColor(textColor);
            if (borderColor != null) item.borderColor(borderColor);
            if (borderRadius != null) item.borderRadius(borderRadius);
            if (transparent != null && transparent) item.transparentBackground();
        }
    }
}