package de.jakob.game.input;

import javafx.scene.input.KeyCode;

public enum Key {
   //ANY("Beliebig", null),
    //LETTER("Buchstabe", null),
    //DIGIT("Ziffer", null),

    MOUSE_LEFT("Linksklick", null),
    MOUSE_RIGHT("Rechtsklick", null),

    ENTER("Enter", KeyCode.ENTER),
    BACK_SPACE("Backspace", KeyCode.BACK_SPACE),
    TAB("Tab", KeyCode.TAB),
    SHIFT("Shift", KeyCode.SHIFT),
    CONTROL("Strg", KeyCode.CONTROL),
    ALT("Alt", KeyCode.ALT),
    PAUSE("Pause", KeyCode.PAUSE),
    CAPS("Feststelltaste", KeyCode.CAPS),
    ESCAPE("Escape", KeyCode.ESCAPE),
    SPACE("Leertaste", KeyCode.SPACE),
    PAGE_UP("Bild auf", KeyCode.PAGE_UP),
    PAGE_DOWN("Bild ab", KeyCode.PAGE_DOWN),
    END("Ende", KeyCode.END),
    HOME("Pos1", KeyCode.HOME),



    LEFT("Pfeil links", KeyCode.LEFT),
    UP("Pfeil hoch", KeyCode.UP),
    RIGHT("Pfeil rechts", KeyCode.RIGHT),
    DOWN("Pfeil runter", KeyCode.DOWN),

    COMMA("Komma", KeyCode.COMMA),
    MINUS("Minus", KeyCode.MINUS),
    PERIOD("Punkt", KeyCode.PERIOD),
    SLASH("Slash", KeyCode.SLASH),

    DIGIT0("0", KeyCode.DIGIT0),
    DIGIT1("1", KeyCode.DIGIT1),
    DIGIT2("2", KeyCode.DIGIT2),
    DIGIT3("3", KeyCode.DIGIT3),
    DIGIT4("4", KeyCode.DIGIT4),
    DIGIT5("5", KeyCode.DIGIT5),
    DIGIT6("6", KeyCode.DIGIT6),
    DIGIT7("7", KeyCode.DIGIT7),
    DIGIT8("8", KeyCode.DIGIT8),
    DIGIT9("9", KeyCode.DIGIT9),

    SEMICOLON("Semikolon", KeyCode.SEMICOLON),
    EQUALS("Gleich", KeyCode.EQUALS),

    A("A", KeyCode.A),
    B("B", KeyCode.B),
    C("C", KeyCode.C),
    D("D", KeyCode.D),
    E("E", KeyCode.E),
    F("F", KeyCode.F),
    G("G", KeyCode.G),
    H("H", KeyCode.H),
    I("I", KeyCode.I),
    J("J", KeyCode.J),
    K("K", KeyCode.K),
    L("L", KeyCode.L),
    M("M", KeyCode.M),
    N("N", KeyCode.N),
    O("O", KeyCode.O),
    P("P", KeyCode.P),
    Q("Q", KeyCode.Q),
    R("R", KeyCode.R),
    S("S", KeyCode.S),
    T("T", KeyCode.T),
    U("U", KeyCode.U),
    V("V", KeyCode.V),
    W("W", KeyCode.W),
    X("X", KeyCode.X),
    Y("Y", KeyCode.Y),
    Z("Z", KeyCode.Z),

    OPEN_BRACKET("Klammer auf", KeyCode.OPEN_BRACKET),
    BACK_SLASH("Backslash", KeyCode.BACK_SLASH),
    CLOSE_BRACKET("Klammer zu", KeyCode.CLOSE_BRACKET),

    NUMPAD0("NumPad 0", KeyCode.NUMPAD0),
    NUMPAD1("NumPad 1", KeyCode.NUMPAD1),
    NUMPAD2("NumPad 2", KeyCode.NUMPAD2),
    NUMPAD3("NumPad 3", KeyCode.NUMPAD3),
    NUMPAD4("NumPad 4", KeyCode.NUMPAD4),
    NUMPAD5("NumPad 5", KeyCode.NUMPAD5),
    NUMPAD6("NumPad 6", KeyCode.NUMPAD6),
    NUMPAD7("NumPad 7", KeyCode.NUMPAD7),
    NUMPAD8("NumPad 8", KeyCode.NUMPAD8),
    NUMPAD9("NumPad 9", KeyCode.NUMPAD9),

    MULTIPLY("NumPad *", KeyCode.MULTIPLY),
    ADD("NumPad +", KeyCode.ADD),
    SEPARATOR("NumPad Trennzeichen", KeyCode.SEPARATOR),
    SUBTRACT("NumPad -", KeyCode.SUBTRACT),
    DECIMAL("NumPad .", KeyCode.DECIMAL),
    DIVIDE("NumPad /", KeyCode.DIVIDE),

    DELETE("Entf", KeyCode.DELETE),

    NUM_LOCK("Num Lock", KeyCode.NUM_LOCK),
    SCROLL_LOCK("Scroll Lock", KeyCode.SCROLL_LOCK),

    F1("F1", KeyCode.F1),
    F2("F2", KeyCode.F2),
    F3("F3", KeyCode.F3),
    F4("F4", KeyCode.F4),
    F5("F5", KeyCode.F5),
    F6("F6", KeyCode.F6),
    F7("F7", KeyCode.F7),
    F8("F8", KeyCode.F8),
    F9("F9", KeyCode.F9),
    F10("F10", KeyCode.F10),
    F11("F11", KeyCode.F11),
    F12("F12", KeyCode.F12),

    PRINTSCREEN("Druck", KeyCode.PRINTSCREEN),
    INSERT("Einfg", KeyCode.INSERT),
    HELP("Hilfe", KeyCode.HELP),
    META("Meta", KeyCode.META),

    BACK_QUOTE("Backquote", KeyCode.BACK_QUOTE),
    QUOTE("Anführungszeichen", KeyCode.QUOTE),

    PLUS("Plus", KeyCode.PLUS),
    MINUS_SYMBOL("Minus", KeyCode.MINUS),

    UNDEFINED("Unbekannt", KeyCode.UNDEFINED);

    private final String displayName;
    private final KeyCode code;

    Key(String displayName, KeyCode code) {
        this.displayName = displayName;
        this.code = code;
    }

    public KeyCode getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
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
          /*      if (this == LETTER && other.isLetter()) {
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
        }*/

        return this == other;
    }

    public boolean isLetter() {
        return code != null && code.isLetterKey();
    }

    public boolean isDigit() {
        return code != null && code.isDigitKey();
    }

    @Override
    public String toString() {
        return displayName;
    }




}