package de.jakob.game.gui.graphics.input;
public class GraphicTextInputField extends GraphicInputField<String> {

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

    public static class Builder extends GraphicInputBuilder<GraphicTextInputField, Builder, String> {

        @Override
        protected void configure(GraphicTextInputField item) {
            applyBase(item);
        }

        @Override
        protected GraphicTextInputField create() {
            return new GraphicTextInputField();
        }
    }
}