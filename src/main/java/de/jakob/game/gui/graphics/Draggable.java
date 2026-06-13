package de.jakob.game.gui.graphics;

@SuppressWarnings("unchecked")
public interface Draggable<T extends Draggable<T>> {

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
            return item.isDraggable();
        }
        return false;
    }
}