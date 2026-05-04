package de.jakob.game.input;

public final class KeyBind {

    private final String action;

    KeyBind(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public Key getKey() {
        return KeyBinds.get(action);
    }

    public String getDisplayName() {
        return KeyBinds.getDisplayName(action);
    }

    public boolean isChangeable() {
        return KeyBinds.isChangeable(action);
    }

    public void setKey(Key key) {
        KeyBinds.set(action, key);
    }

    @Override
    public String toString() {
        return action + " = " + getKey();
    }
}