package de.jakob.game.gui.graphics;

@SuppressWarnings("unchecked")
public interface Draggable<T extends Draggable<T>> {

    default T draggable() {
        if (this instanceof GraphicItem item) {
            item.setInternalDraggable(true);
        } else if (this instanceof GraphicItem.GraphicItemBuilder<?, ?> builder) {
            builder.setInternalMoveable(true);
        }
        return (T) this;
    }


    default T notDraggable() {
        if (this instanceof GraphicItem item) {
            item.setInternalDraggable(false);
        } else if (this instanceof GraphicItem.GraphicItemBuilder<?, ?> builder) {
            builder.setInternalMoveable(false);
        }
        return (T) this;
    }

    default boolean isDraggable() {
        if (this instanceof GraphicItem item) {
            return item.isDraggable();
        }
        return false;
    }
}