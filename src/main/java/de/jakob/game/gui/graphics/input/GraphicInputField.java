package de.jakob.game.gui.graphics.input;

import javafx.scene.control.TextField;

public abstract class GraphicInputField<T> extends GraphicInput<T> {

    protected TextField field;

    @Override
    public void build() {
        field = new TextField();

        if (width > 0) field.setPrefWidth(width);
        if (height > 0) field.setPrefHeight(height);

        field.textProperty().addListener((obs, oldVal, newVal) -> {
            handleTextChange(oldVal, newVal);
        });

        setNode(field);

        applyValueToNode();
        applyEnabledState();
        applyStyle();
    }

    protected abstract void handleTextChange(String oldVal, String newVal);

    @Override
    protected void applyValueToNode() {
        if (field != null && value != null) {
            field.setText(formatValue(value));
        }
    }

    protected String formatValue(T value) {
        return String.valueOf(value);
    }
}