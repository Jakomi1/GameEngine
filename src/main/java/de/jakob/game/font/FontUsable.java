package de.jakob.game.font;

import javafx.scene.text.Font;

public interface FontUsable<T> {

    Font DEFAULT_FONT = Font.font("Arial", 14);

    T font(Font font);

    default T setFont(Font font) {
        return font(font);
    }

    Font getFont();

    default Font effectiveFont() {
        return getFont() != null ? getFont() : DEFAULT_FONT;
    }

    void applyFont();
}