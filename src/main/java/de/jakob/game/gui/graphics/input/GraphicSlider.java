package de.jakob.game.gui.graphics.input;

import javafx.scene.control.Slider;
public class GraphicSlider extends GraphicInput<Double> {

    private Slider slider;

    private double min = 0;
    private double max = 100;

    public GraphicSlider range(double min, double max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public void build() {
        slider = new Slider(min, max, value != null ? value : min);

        if (width > 0) slider.setPrefWidth(width);
        if (height > 0) slider.setPrefHeight(height);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            notifyChange(newVal.doubleValue());
        });

        setNode(slider);

        applyValueToNode();
        applyEnabledState();
        applyStyle();
    }

    @Override
    protected void applyValueToNode() {
        if (slider != null && value != null) {
            slider.setValue(value);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GraphicInputBuilder<GraphicSlider, Builder, Double> {

        private Double min;
        private Double max;

        public Builder range(double min, double max) {
            this.min = min;
            this.max = max;
            return this;
        }

        @Override
        protected void configure(GraphicSlider item) {
            applyBase(item);

            if (min != null && max != null) {
                item.range(min, max);
            }
        }

        @Override
        protected GraphicSlider create() {
            return new GraphicSlider();
        }
    }
}