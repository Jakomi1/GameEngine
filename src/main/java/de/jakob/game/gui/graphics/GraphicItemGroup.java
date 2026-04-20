package de.jakob.game.gui.graphics;

import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.util.Position;
import javafx.scene.Group;
import javafx.scene.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
public class GraphicItemGroup extends GraphicItem implements Moveable<GraphicItemGroup> {

    private final Group root = new Group();
    private final List<GraphicItem> items = new ArrayList<>();

    private boolean built = false;

    public GraphicItemGroup() {
        setNode(root);
    }

    public GraphicItemGroup addItem(GraphicItem item) {
        if (item == null) return this;

        items.add(item);

        if (built) {
            attachItem(item);
            refreshBounds();
        }

        return this;
    }

    public GraphicItemGroup addItem(GraphicItem.GraphicItemBuilder<?, ?> builder, double x, double y) {
        if (builder == null) return this;
        if (!built) {
            throw new IllegalStateException("GraphicItemGroup muss erst build() haben, bevor Builder-Items hinzugefügt werden.");
        }

        GraphicItem item = builder.build(gui, Position.of(x, y));
        return addItem(item);
    }

    public <T extends GraphicItem> T addItemAndGet(GraphicItem.GraphicItemBuilder<T, ?> builder, double x, double y) {
        if (builder == null) return null;
        if (!built) {
            throw new IllegalStateException("GraphicItemGroup muss erst build() haben, bevor Builder-Items hinzugefügt werden.");
        }

        T item = builder.build(gui, Position.of(x, y));
        addItem(item);
        return item;
    }

    public GraphicItemGroup removeItem(GraphicItem item) {
        if (item == null) return this;

        items.remove(item);

        if (built && item.getNode() != null) {
            root.getChildren().remove(item.getNode());
            refreshBounds();
        }

        return this;
    }

    public GraphicItemGroup clearItems() {
        items.clear();

        if (built) {
            root.getChildren().clear();
            refreshBounds();
        }

        return this;
    }

    public List<GraphicItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int sizeItems() {
        return items.size();
    }

    public GraphicItem getItem(int index) {
        return items.get(index);
    }

    @Override
    public void build() {
        if (built) return;

        root.getChildren().clear();

        for (GraphicItem item : items) {
            attachItem(item);
            Node node = item.getNode();
            if (node == null) {
                throw new IllegalStateException(
                        "Ein GraphicItem in der Group hat kein Node. Erst das Item korrekt builden oder als gebauten Item hinzufügen."
                );
            }
            root.getChildren().add(node);
        }

        built = true;

        refreshBounds();
        applyVisibility();
        recalcPosition();
        applyPosition();
    }

    @Override
    public void init(GraphicUserInterface gui) {
        super.init(gui);

        for (GraphicItem item : items) {
            item.init(gui);
        }
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public void show() {
        super.show();
        for (GraphicItem item : items) {
            item.show();
        }
    }

    @Override
    public void hide() {
        super.hide();
        for (GraphicItem item : items) {
            item.hide();
        }
    }

    @Override
    public GraphicItem position(Position.Builder builder) {
        super.position(builder);
        return this;
    }

    @Override
    public GraphicItem size(double width, double height) {
        super.size(width, height);
        return this;
    }

    @Override
    public double getWidth() {
        if (width > 0) return width;
        refreshBounds();
        return root.getLayoutBounds().getWidth();
    }

    @Override
    public double getHeight() {
        if (height > 0) return height;
        refreshBounds();
        return root.getLayoutBounds().getHeight();
    }

    @Override
    protected double getEffectiveWidth() {
        return getWidth();
    }

    @Override
    protected double getEffectiveHeight() {
        return getHeight() + de.jakob.game.gui.GraphicUserInterface.EXTRA_SIZE;
    }

    private void attachItem(GraphicItem item) {
        if (item == null) return;

        if (gui != null) {
            item.init(gui);
        }

        if (item.getNode() != null) {
            item.getNode().setUserData(item);
        }
    }

    private void refreshBounds() {
        root.applyCss();
        root.layout();
    }
}