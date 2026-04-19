package de.jakob.game.gui.graphics;

@SuppressWarnings("unchecked")
public interface Moveable<T extends Moveable<T>> {

    default T moveable() {
        if (this instanceof GraphicItem item) {
            item.setInternalMoveable(true);
        } else if (this instanceof GraphicItem.GraphicItemBuilder<?, ?> builder) {
            builder.setInternalMoveable(true);
        }
        return (T) this;
    }


    default T notMoveable() {
        if (this instanceof GraphicItem item) {
            item.setInternalMoveable(false);
        } else if (this instanceof GraphicItem.GraphicItemBuilder<?, ?> builder) {
            builder.setInternalMoveable(false);
        }
        return (T) this;
    }

    default boolean isMoveable() {
        if (this instanceof GraphicItem item) {
            return item.isMoveable();
        }
        return false;
    }
}