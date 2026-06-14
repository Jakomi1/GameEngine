package de.jakob.game.gui.graphics;

import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.generic.MainGraphicUserInterface;
import de.jakob.game.gui.graphics.media.GraphicImage;
import de.jakob.game.gui.graphics.media.GraphicTextureItem;
import de.jakob.game.gui.util.Position;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted", "SameParameterValue"})
public abstract class GraphicItem {

    private static final double MAX_STEP = 2.0;

    protected Node node;
    protected GraphicUserInterface gui;
    protected Position.Builder position;
    protected Point2D resolvedPosition = new Point2D(0, 0);

    protected boolean visible = true;
    protected double width;
    protected double height;

    private Runnable onPress = null;
    private Runnable onRelease = null;
    private Runnable onClick = null;

    private boolean clickHandlerInstalled = false;
    private boolean draggable = false;
    private boolean dragHandlersInstalled = false;
    private boolean blockOthers = true;
    private boolean dragging = false;

    private double dragOffsetX;
    private double dragOffsetY;

    private String fxBaseStyle = "";
    private final List<String> fxExtraStyles = new ArrayList<>();
    private String lastAppliedFxStyle = "";

    public void init(GraphicUserInterface gui) {
        if (gui == null) throw new IllegalArgumentException("GUI darf nicht null sein!");
        this.gui = gui;
    }

    public GraphicItem move(double dx, double dy) {
        double newX = getX() + dx;
        double newY = getY() + dy;

        this.position = Position.of(newX, newY);
        this.resolvedPosition = new Point2D(newX, newY);

        applyPosition();
        return this;
    }

    public GraphicItem setX(double x) {
        this.position = Position.of(x, getY());
        this.resolvedPosition = new Point2D(x, getY());
        applyPosition();
        return this;
    }

    public GraphicItem setY(double y) {
        this.position = Position.of(getX(), y);
        this.resolvedPosition = new Point2D(getX(), y);
        applyPosition();
        return this;
    }

    public GraphicItem toFront() {
        if (node != null) {
            node.toFront();
        }
        return this;
    }

    public GraphicItem toBack() {
        if (node != null) {
            node.toBack();
        }
        return this;
    }

    public boolean isCompletelyOutsideGui() {
        if (gui == null) {
            return false;
        }

        double guiWidth = gui.getContentWidth();
        double guiHeight = gui.getContentHeight();

        if (guiWidth <= 0 || guiHeight <= 0) {
            return false;
        }

        double x = getX();
        double y = getY();
        double w = getEffectiveWidth();
        double h = getEffectiveHeight();

        return x + w <= 0
                || y + h <= 0
                || x >= guiWidth
                || y >= guiHeight;
    }

    public abstract void build();

    public double getHorizontalMidlineY(double yOffset) {
        return getY() + (getEffectiveHeight() / 2.0) + yOffset;
    }

    public boolean touchesHorizontalMidline(GraphicItem other, double yOffset, double activePercent) {
        if (other == null || other == this) return false;

        activePercent = Math.max(0.0, Math.min(1.0, activePercent));

        double midY = getHorizontalMidlineY(yOffset);

        boolean touchesVertically =
                other.getY() <= midY &&
                        other.getY() + other.getEffectiveHeight() >= midY;

        double width = this.getEffectiveWidth();
        double centerX = this.getX() + width / 2.0;

        double activeWidth = width * activePercent;
        double halfActive = activeWidth / 2.0;

        double minX = centerX - halfActive;
        double maxX = centerX + halfActive;

        double otherCenterX = other.getX() + other.getEffectiveWidth() / 2.0;

        boolean touchesHorizontally =
                otherCenterX >= minX && otherCenterX <= maxX;

        return touchesVertically && touchesHorizontally;
    }

    public boolean touchesHorizontalMidline(GraphicItem other, double yOffset) {
        return touchesHorizontalMidline(other, yOffset, 1.0);
    }

    public boolean touchesHorizontalMidline(GraphicItem other) {
        return touchesHorizontalMidline(other, 0);
    }
    public List<GraphicItem> getTouchingItems() {
        List<GraphicItem> touching = new ArrayList<>();
        if (node == null || node.getParent() == null) return touching;

        for (Node sibling : node.getParent().getChildrenUnmodifiable()) {
            if (sibling == node || !sibling.isVisible()) continue;
            if (sibling.getUserData() instanceof GraphicItem other && touches(other)) {
                touching.add(other);
            }
        }
        return touching;
    }

    public void show() {
        visible = true;
        applyVisibility();
    }

    public void hide() {
        visible = false;
        applyVisibility();
    }

    public boolean isVisible() {
        return visible;
    }

    void applyVisibility() {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    public GraphicItem onPress(Runnable action) {
        this.onPress = action;
        installClickHandlerIfPossible();
        return this;
    }

    public GraphicItem onRelease(Runnable action) {
        this.onRelease = action;
        installClickHandlerIfPossible();
        return this;
    }

    public GraphicItem onClick(Runnable action) {
        this.onClick = action;
        installClickHandlerIfPossible();
        return this;
    }

    protected void fireOnPress() {
        if (onPress != null) onPress.run();
    }

    protected void fireOnRelease() {
        if (onRelease != null) onRelease.run();
    }

    protected void fireOnClick() {
        if (onClick != null) onClick.run();
    }

    protected boolean usesExternalClickHandling() {
        return false;
    }

    public GraphicItem setFXStyle(String style) {
        fxBaseStyle = normalizeFxStyle(style);
        fxExtraStyles.clear();
        applyFXStyle();
        return this;
    }

    public GraphicItem addFXStyle(String style) {
        String normalized = normalizeFxStyle(style);
        if (!normalized.isBlank() && !fxExtraStyles.contains(normalized)) {
            fxExtraStyles.add(normalized);
            applyFXStyle();
        }
        return this;
    }

    public GraphicItem clearFXStyle() {
        fxBaseStyle = "";
        fxExtraStyles.clear();
        applyFXStyle();
        return this;
    }

    protected void applyFXStyle() {
        if (node == null) return;

        StringBuilder style = new StringBuilder(fxBaseStyle);
        for (String extra : fxExtraStyles) {
            if (extra == null || extra.isBlank()) continue;
            if (!style.isEmpty() && style.charAt(style.length() - 1) != ';') style.append(';');
            style.append(extra);
        }

        String newStyle = style.toString();
        if (!Objects.equals(lastAppliedFxStyle, newStyle)) {
            node.setStyle(newStyle);
            lastAppliedFxStyle = newStyle;
        }
    }

    protected void refreshFXStyle() {
        applyFXStyle();
    }

    private String normalizeFxStyle(String style) {
        if (style == null || style.trim().isEmpty()) return "";
        String trimmed = style.trim();
        return trimmed.endsWith(";") ? trimmed : trimmed + ";";
    }

    public Node getNode() {
        if (node != null && node.getUserData() != this) node.setUserData(this);
        installDraggableHandlersIfPossible();
        installClickHandlerIfPossible();
        return node;
    }

    protected void setNode(Node node) {
        this.node = node;
        if (this.node != null) this.node.setUserData(this);
        clickHandlerInstalled = false;
        installDraggableHandlersIfPossible();
        installClickHandlerIfPossible();
        applyFXStyle();
        recalcPosition();
        applyVisibility();
    }

    public GraphicItem position(Position.Builder builder) {
        if (builder == null) throw new IllegalArgumentException("Position.Builder darf nicht null sein!");
        this.position = builder;
        recalcPosition();
        return this;
    }

    protected Point2D resolvePosition() {
        return position != null ? position.get(gui, this) : resolvedPosition;
    }

    protected void recalcPosition() {
        if (position == null) return;
        Point2D p = resolvePosition();
        this.resolvedPosition = p != null ? p : new Point2D(0, 0);
        applyPosition();
    }

    protected void applyPosition() {
        if (node != null) node.relocate(resolvedPosition.getX(), resolvedPosition.getY());
    }

    protected void setResolvedPosition(Point2D p) {
        this.resolvedPosition = p != null ? p : new Point2D(0, 0);
    }

    public double getX() {
        return resolvedPosition.getX();
    }

    public double getY() {
        return resolvedPosition.getY();
    }

    public Point2D getPointPosition() {
        return resolvedPosition;
    }

    public Position.Builder getPosition() {
        return position;
    }

    public GraphicItem size(double width, double height) {
        this.width = width;
        this.height = height;
        recalcPosition();
        return this;
    }

    public double getWidth() {
        if (width > 0) return width;
        return (node != null && node.getLayoutBounds().getWidth() > 0) ? node.getLayoutBounds().getWidth() : 0;
    }

    public double getHeight() {
        if (height > 0) return height;
        return (node != null && node.getLayoutBounds().getHeight() > 0) ? node.getLayoutBounds().getHeight() : 0;
    }

    private void installClickHandlerIfPossible() {
        if (node == null || clickHandlerInstalled || usesExternalClickHandling()) return;

        node.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) fireOnPress();
        });

        node.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) fireOnRelease();
        });

        node.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) fireOnClick();
        });

        clickHandlerInstalled = true;
    }

    protected void setInternalDraggable(boolean value) {
        this.draggable = value;
        installDraggableHandlersIfPossible();
    }

    public boolean isDraggable() {
        return draggable;
    }

    public GraphicItem blockOthers() {
        this.blockOthers = true;
        return this;
    }

    public GraphicItem allowOverlap() {
        this.blockOthers = false;
        return this;
    }

    public boolean blocksOthers() {
        return blockOthers;
    }

    protected Shape createCollisionShape() {
        return new Rectangle(Math.max(0, getWidth()), Math.max(0, getHeight()));
    }

    private void installDraggableHandlersIfPossible() {
        if (!(this instanceof Draggable) || !draggable || dragHandlersInstalled || node == null) return;

        dragHandlersInstalled = true;

        node.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            dragging = true;
            Point2D current = resolvePosition();
            Point2D parentPoint = toParentPoint(e.getSceneX(), e.getSceneY());
            dragOffsetX = parentPoint.getX() - current.getX();
            dragOffsetY = parentPoint.getY() - current.getY();
            e.consume();
        });

        node.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (!dragging) return;
            Point2D parentPoint = toParentPoint(e.getSceneX(), e.getSceneY());
            moveClamped(parentPoint.getX() - dragOffsetX, parentPoint.getY() - dragOffsetY);
            e.consume();
        });

        node.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> dragging = false);
    }

    private Point2D toParentPoint(double sceneX, double sceneY) {
        Parent parent = node != null ? node.getParent() : null;
        return parent != null ? parent.sceneToLocal(sceneX, sceneY) : new Point2D(sceneX, sceneY);
    }

    private void moveClamped(double newX, double newY) {
        double maxX = Math.max(0, getMovementBoundsWidth() - getEffectiveWidth());
        double maxY = Math.max(0, getMovementBoundsHeight() - getEffectiveHeight());

        double targetX = clamp(newX, 0, maxX);
        double targetY = clamp(newY, 0, maxY);

        double dx = targetX - resolvedPosition.getX();
        double dy = targetY - resolvedPosition.getY();

        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / MAX_STEP));
        double stepX = dx / steps;
        double stepY = dy / steps;

        double x = resolvedPosition.getX();
        double y = resolvedPosition.getY();

        for (int i = 0; i < steps; i++) {
            if (!hasBlockingCollisionAt(x + stepX, y)) x += stepX;
            if (!hasBlockingCollisionAt(x, y + stepY)) y += stepY;
        }
        commitPosition(x, y);
    }

    private void commitPosition(double x, double y) {
        this.position = Position.of(x, y);
        this.resolvedPosition = new Point2D(x, y);
        applyPosition();
    }

    private double getMovementBoundsWidth() {
        Parent parent = node != null ? node.getParent() : null;
        if (parent != null && parent.getLayoutBounds().getWidth() > 0) return parent.getLayoutBounds().getWidth();
        if (gui != null && gui.getContentWidth() > 0) return gui.getContentWidth();
        return (node != null && node.getScene() != null) ? node.getScene().getWidth() : 0;
    }

    private double getMovementBoundsHeight() {
        Parent parent = node != null ? node.getParent() : null;
        if (parent != null && parent.getLayoutBounds().getHeight() > 0) return parent.getLayoutBounds().getHeight();
        if (gui != null) {
            if (gui.getContentHeight() > 0) return gui.getContentHeight();
            if (gui.getWindowHeight() > 0) {
                return gui instanceof MainGraphicUserInterface
                        ? gui.getWindowHeight()
                        : Math.max(0, gui.getWindowHeight() - GraphicUserInterface.TOP_BAR_HEIGHT);
            }
        }
        return (node != null && node.getScene() != null) ? node.getScene().getHeight() : 0;
    }

    protected double getEffectiveWidth() {
        return getWidth();
    }

    protected double getEffectiveHeight() {
        return getHeight();
    }

    private boolean hasBlockingCollisionAt(double testX, double testY) {
        if (node == null || node.getParent() == null) return false;

        for (Node sibling : node.getParent().getChildrenUnmodifiable()) {
            if (sibling == node || !sibling.isVisible()) continue;
            if (!(sibling.getUserData() instanceof GraphicItem other)) continue;
            if (!this.blocksOthers() && !other.blocksOthers()) continue;

            if (other instanceof GraphicImage otherImage) {
                if (this instanceof GraphicImage thisImage) {
                    if (collidesMovedImageAt(thisImage, otherImage, testX, testY)) return true;
                } else {
                    if (collidesItemAgainstImageAt(this, otherImage, testX, testY)) return true;
                }
                continue;
            }

            if (intersects(testX, testY, getEffectiveWidth(), getEffectiveHeight(),
                    other.getX(), other.getY(), other.getEffectiveWidth(), other.getEffectiveHeight())) {
                return true;
            }
        }
        return false;
    }

    private boolean collidesMovedImageAt(GraphicTextureItem movedImage, GraphicImage other, double testX, double testY) {
        Point2D old = movedImage.getPointPosition();
        movedImage.setResolvedPosition(new Point2D(testX, testY));
        movedImage.applyPosition();
        try {
            return movedImage.preciseTouches(other);
        } finally {
            movedImage.setResolvedPosition(old);
            movedImage.applyPosition();
        }
    }

    private boolean collidesItemAgainstImageAt(GraphicItem movedItem, GraphicImage image, double testX, double testY) {
        Point2D old = movedItem.getPointPosition();
        movedItem.setResolvedPosition(new Point2D(testX, testY));
        movedItem.applyPosition();
        try {
            return image.preciseTouches(movedItem);
        } finally {
            movedItem.setResolvedPosition(old);
            movedItem.applyPosition();
        }
    }

    private boolean intersects(double x1, double y1, double w1, double h1,
                               double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean touches(GraphicItem other) {
        if (other == null || other == this) return false;
        return intersects(getX(), getY(), getEffectiveWidth(), getEffectiveHeight(),
                other.getX(), other.getY(), other.getEffectiveWidth(), other.getEffectiveHeight());
    }

    public abstract static class GraphicItemBuilder<T extends GraphicItem, B extends GraphicItemBuilder<T, B>> {

        protected double width = 100;
        protected double height = 36;
        protected boolean moveable = false;
        protected boolean blockOthers = true;
        protected boolean visible = true;
        protected Position.Builder position;
        protected Runnable onClick;
        protected Runnable onPress;
        protected Runnable onRelease;
        protected final List<String> fxStyles = new ArrayList<>();

        protected void setInternalMoveable(boolean value) {
            this.moveable = value;
        }

        @SuppressWarnings("unchecked")
        public B onPress(Runnable action) {
            this.onPress = action;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B onRelease(Runnable action) {
            this.onRelease = action;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B size(double width, double height) {
            this.width = width;
            this.height = height;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B blockOthers() {
            this.blockOthers = true;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B allowOverlap() {
            this.blockOthers = false;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B onClick(Runnable action) {
            this.onClick = action;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B visible(boolean visible) {
            this.visible = visible;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B hidden() {
            this.visible = false;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addFXStyle(String style) {
            if (style != null && !style.isBlank()) this.fxStyles.add(style);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B position(Position.Builder position) {
            this.position = position;
            return (B) this;
        }

        public final T build(GraphicUserInterface gui) {
            if (position == null) throw new IllegalStateException("Position muss gesetzt sein!");
            return build(gui, position);
        }

        public final T build(GraphicUserInterface gui, Position.Builder positionBuilder) {
            T item = create();
            item.init(gui);

            if (width >= 0 && height >= 0) item.size(width, height);

            configure(item);

            item.setInternalDraggable(this.moveable);
            if (blockOthers) item.blockOthers(); else item.allowOverlap();

            item.visible = this.visible;
            if (this.onClick != null) item.onClick(this.onClick);
            if (this.onPress != null) item.onPress(this.onPress);
            if (this.onRelease != null) item.onRelease(this.onRelease);

            for (String style : fxStyles) item.addFXStyle(style);

            item.position = positionBuilder;
            item.build();
            item.recalcPosition();
            item.applyVisibility();

            return item;
        }

        protected void configure(T item) {}

        protected abstract T create();
    }
}