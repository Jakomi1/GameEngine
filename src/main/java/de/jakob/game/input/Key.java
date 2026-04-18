package de.jakob.game.input;

import javafx.scene.input.KeyCode;

public enum Key {

    ANY(null),
    LETTER(null),
    DIGIT(null),

    MOUSE_LEFT(null),
    MOUSE_RIGHT(null),

    ENTER(KeyCode.ENTER),
    BACK_SPACE(KeyCode.BACK_SPACE),
    TAB(KeyCode.TAB),
    SHIFT(KeyCode.SHIFT),
    CONTROL(KeyCode.CONTROL),
    ALT(KeyCode.ALT),
    PAUSE(KeyCode.PAUSE),
    CAPS(KeyCode.CAPS),
    ESCAPE(KeyCode.ESCAPE),
    SPACE(KeyCode.SPACE),
    PAGE_UP(KeyCode.PAGE_UP),
    PAGE_DOWN(KeyCode.PAGE_DOWN),
    END(KeyCode.END),
    HOME(KeyCode.HOME),

    LEFT(KeyCode.LEFT),
    UP(KeyCode.UP),
    RIGHT(KeyCode.RIGHT),
    DOWN(KeyCode.DOWN),

    COMMA(KeyCode.COMMA),
    MINUS(KeyCode.MINUS),
    PERIOD(KeyCode.PERIOD),
    SLASH(KeyCode.SLASH),

    DIGIT0(KeyCode.DIGIT0),
    DIGIT1(KeyCode.DIGIT1),
    DIGIT2(KeyCode.DIGIT2),
    DIGIT3(KeyCode.DIGIT3),
    DIGIT4(KeyCode.DIGIT4),
    DIGIT5(KeyCode.DIGIT5),
    DIGIT6(KeyCode.DIGIT6),
    DIGIT7(KeyCode.DIGIT7),
    DIGIT8(KeyCode.DIGIT8),
    DIGIT9(KeyCode.DIGIT9),

    SEMICOLON(KeyCode.SEMICOLON),
    EQUALS(KeyCode.EQUALS),

    A(KeyCode.A),
    B(KeyCode.B),
    C(KeyCode.C),
    D(KeyCode.D),
    E(KeyCode.E),
    F(KeyCode.F),
    G(KeyCode.G),
    H(KeyCode.H),
    I(KeyCode.I),
    J(KeyCode.J),
    K(KeyCode.K),
    L(KeyCode.L),
    M(KeyCode.M),
    N(KeyCode.N),
    O(KeyCode.O),
    P(KeyCode.P),
    Q(KeyCode.Q),
    R(KeyCode.R),
    S(KeyCode.S),
    T(KeyCode.T),
    U(KeyCode.U),
    V(KeyCode.V),
    W(KeyCode.W),
    X(KeyCode.X),
    Y(KeyCode.Y),
    Z(KeyCode.Z),

    OPEN_BRACKET(KeyCode.OPEN_BRACKET),
    BACK_SLASH(KeyCode.BACK_SLASH),
    CLOSE_BRACKET(KeyCode.CLOSE_BRACKET),

    NUMPAD0(KeyCode.NUMPAD0),
    NUMPAD1(KeyCode.NUMPAD1),
    NUMPAD2(KeyCode.NUMPAD2),
    NUMPAD3(KeyCode.NUMPAD3),
    NUMPAD4(KeyCode.NUMPAD4),
    NUMPAD5(KeyCode.NUMPAD5),
    NUMPAD6(KeyCode.NUMPAD6),
    NUMPAD7(KeyCode.NUMPAD7),
    NUMPAD8(KeyCode.NUMPAD8),
    NUMPAD9(KeyCode.NUMPAD9),

    MULTIPLY(KeyCode.MULTIPLY),
    ADD(KeyCode.ADD),
    SEPARATOR(KeyCode.SEPARATOR),
    SUBTRACT(KeyCode.SUBTRACT),
    DECIMAL(KeyCode.DECIMAL),
    DIVIDE(KeyCode.DIVIDE),

    DELETE(KeyCode.DELETE),

    NUM_LOCK(KeyCode.NUM_LOCK),
    SCROLL_LOCK(KeyCode.SCROLL_LOCK),

    F1(KeyCode.F1),
    F2(KeyCode.F2),
    F3(KeyCode.F3),
    F4(KeyCode.F4),
    F5(KeyCode.F5),
    F6(KeyCode.F6),
    F7(KeyCode.F7),
    F8(KeyCode.F8),
    F9(KeyCode.F9),
    F10(KeyCode.F10),
    F11(KeyCode.F11),
    F12(KeyCode.F12),

    PRINTSCREEN(KeyCode.PRINTSCREEN),
    INSERT(KeyCode.INSERT),
    HELP(KeyCode.HELP),
    META(KeyCode.META),

    BACK_QUOTE(KeyCode.BACK_QUOTE),
    QUOTE(KeyCode.QUOTE),

    PLUS(KeyCode.PLUS),
    MINUS_SYMBOL(KeyCode.MINUS),

    UNDEFINED(KeyCode.UNDEFINED);

    private final KeyCode code;

    Key(KeyCode code) {
        this.code = code;
    }

    public KeyCode getCode() {
        return code;
    }

    public static Key from(KeyCode code) {
        for (Key key : values()) {
            if (key.code == code) {
                return key;
            }
        }
        return UNDEFINED;
    }

    public boolean matches(Key other) {
        if (this == ANY || other == ANY) {
            return true;
        }

        if (this == LETTER && other.isLetter()) {
            return true;
        }

        if (other == LETTER && this.isLetter()) {
            return true;
        }

        if (this == DIGIT && other.isDigit()) {
            return true;
        }

        if (other == DIGIT && this.isDigit()) {
            return true;
        }

        return this == other;
    }

    public boolean isLetter() {
        return code != null && code.isLetterKey();
    }

    public boolean isDigit() {
        return code != null && code.isDigitKey();
    }
}