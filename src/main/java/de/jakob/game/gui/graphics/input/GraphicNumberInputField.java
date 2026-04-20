package de.jakob.game.gui.graphics.input;

public class GraphicNumberInputField extends GraphicInputField<Double> {

    @Override
    protected void handleTextChange(String oldVal, String newVal) {

        if (exceedsMaxLength(newVal)) {
            field.setText(newVal.substring(0, maxCharacters));
            return;
        }

        if (newVal.matches("-?\\d*(\\.\\d*)?")) {
            try {
                double val = newVal.isEmpty() ? 0 : Double.parseDouble(newVal);
                notifyChange(val);
            } catch (Exception ignored) {}
        } else {
            field.setText(oldVal);
        }
    }

    @Override
    protected String formatValue(Double value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GraphicInputBuilder<GraphicNumberInputField, Builder, Double> {

        @Override
        protected void configure(GraphicNumberInputField item) {
            applyBase(item);
        }

        @Override
        protected GraphicNumberInputField create() {
            return new GraphicNumberInputField();
        }
    }
}