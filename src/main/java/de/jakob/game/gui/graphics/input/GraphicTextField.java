package de.jakob.game.gui.graphics.input;
public class GraphicTextField extends GraphicInputField<String> {

    @Override
    protected void handleTextChange(String oldVal, String newVal) {

        if (exceedsMaxLength(newVal)) {
            field.setText(newVal.substring(0, maxCharacters));
            return;
        }

        notifyChange(newVal);
    }

    @Override
    protected String formatValue(String value) {
        return value == null ? "" : value;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GraphicInputBuilder<GraphicTextField, Builder, String> {

        @Override
        protected void configure(GraphicTextField item) {
            applyBase(item);
        }

        @Override
        protected GraphicTextField create() {
            return new GraphicTextField();
        }
    }
}