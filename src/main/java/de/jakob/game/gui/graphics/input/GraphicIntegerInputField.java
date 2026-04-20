package de.jakob.game.gui.graphics.input;
public class GraphicIntegerInputField extends GraphicInputField<Integer> {

    private Integer maxValue;

    public GraphicIntegerInputField maxValue(int max) {
        this.maxValue = max;
        return this;
    }

    @Override
    protected void handleTextChange(String oldVal, String newVal) {

        if (exceedsMaxLength(newVal)) {
            field.setText(newVal.substring(0, maxCharacters));
            return;
        }

        if (newVal.matches("-?\\d*")) {
            try {
                int val = newVal.isEmpty() || newVal.equals("-") ? 0 : Integer.parseInt(newVal);

                if (maxValue != null) val = Math.min(maxValue, val);

                notifyChange(val);

            } catch (Exception ignored) {}
        } else {
            field.setText(oldVal);
        }
    }

    @Override
    protected String formatValue(Integer value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GraphicInputBuilder<GraphicIntegerInputField, Builder, Integer> {

        private Integer maxValue;

        public Builder maxValue(int max) {
            this.maxValue = max;
            return this;
        }

        @Override
        protected void configure(GraphicIntegerInputField item) {
            applyBase(item);
            if (maxValue != null) item.maxValue(maxValue);
        }

        @Override
        protected GraphicIntegerInputField create() {
            return new GraphicIntegerInputField();
        }
    }
}