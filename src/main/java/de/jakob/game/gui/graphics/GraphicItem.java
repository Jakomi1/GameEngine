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

@SuppressWarnings({"UnusedReturnValue", "BooleanMethodIsAlwaysInverted", "SameParameterValue"})
public abstract class GraphicItem {

    private static final double MAX_STEP = 1.0;

    protected Node node;
    protected GraphicUserInterface gui;

    protected Position.Builder position;
    protected Point2D resolvedPosition = new Point2D(0, 0);

    protected boolean visible = true; 

    protected double width;
    protected double height;

    private boolean moveable = false;
    private boolean moveHandlersInstalled = false;
    private boolean blockOthers = true;

    private boolean dragging = false;
    private double dragOffsetX;
    private double dragOffsetY;

    public void init(GraphicUserInterface gui) {
        if (gui == null) {
            throw new IllegalArgumentException("GUI darf nicht null sein!");
        }
        this.gui = gui;
    }

    public abstract void build();

    
    
    

    public void show() {
        visible = true;
        if (node == null) return;

        node.setVisible(true);
        node.setManaged(true);
    }

    public void hide() {
        visible = false;
        if (node == null) return;

        node.setVisible(false);
        node.setManaged(false);
    }

    public boolean isVisible() {
        return visible;
    }

    void applyVisibility() {
        if (node == null) return;

        node.setVisible(visible);
        node.setManaged(visible);
    }

    

    public Node getNode() {
        if (node != null && node.getUserData() != this) {
            node.setUserData(this);
        }
        installMoveHandlersIfPossible();
        return node;
    }

    protected void setNode(Node node) {
        this.node = node;

        if (this.node != null) {
            this.node.setUserData(this);
        }

        installMoveHandlersIfPossible();
        recalcPosition();
        applyVisibility(); 
    }

    public GraphicItem position(Position.Builder builder) {
        if (builder == null) {
            throw new IllegalArgumentException("Position.Builder darf nicht null sein!");
        }

        this.position = builder;
        recalcPosition();
        return this;
    }

    protected Point2D resolvePosition() {
        if (position == null) {
            return resolvedPosition;
        }
        return position.get(gui, this);
    }

    protected void recalcPosition() {
        if (position == null) return;

        Point2D p = resolvePosition();
        this.resolvedPosition = p != null ? p : new Point2D(0, 0);

        if (node != null) {
            node.relocate(resolvedPosition.getX(), resolvedPosition.getY());
        }
    }

    protected void applyPosition() {
        if (node == null) return;
        node.relocate(resolvedPosition.getX(), resolvedPosition.getY());
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

    public Point2D getPosition() {
        return resolvedPosition;
    }

    public GraphicItem size(double width, double height) {
        this.width = width;
        this.height = height;
        recalcPosition();
        return this;
    }

    public double getWidth() {
        if (width > 0) return width;
        if (node != null) {
            double w = node.getLayoutBounds().getWidth();
            if (w > 0) return w;
        }
        return 0;
    }

    public double getHeight() {
        if (height > 0) return height;
        if (node != null) {
            double h = node.getLayoutBounds().getHeight();
            if (h > 0) return h;
        }
        return 0;
    }

    protected void setInternalMoveable(boolean value) {
        this.moveable = value;
        installMoveHandlersIfPossible();
    }

    public boolean isMoveable() {
        return moveable;
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

    private void installMoveHandlersIfPossible() {
        if (!(this instanceof Moveable) || !moveable || moveHandlersInstalled || node == null) return;

        moveHandlersInstalled = true;

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
            double newX = parentPoint.getX() - dragOffsetX;
            double newY = parentPoint.getY() - dragOffsetY;
            moveClamped(newX, newY);
            e.consume();
        });

        node.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> dragging = false);
    }

    private Point2D toParentPoint(double sceneX, double sceneY) {
        Parent parent = node != null ? node.getParent() : null;
        return (parent != null) ? parent.sceneToLocal(sceneX, sceneY) : new Point2D(sceneX, sceneY);
    }

    private void moveClamped(double newX, double newY) {
        double boundsWidth = getMovementBoundsWidth();
        double boundsHeight = getMovementBoundsHeight();

        double maxX = Math.max(0, boundsWidth - getEffectiveWidth());
        double maxY = Math.max(0, boundsHeight - getEffectiveHeight());

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
            double nextX = x + stepX;
            if (!hasBlockingCollisionAt(nextX, y)) x = nextX;
            double nextY = y + stepY;
            if (!hasBlockingCollisionAt(x, nextY)) y = nextY;
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
                    other.getX(), other.getY(),
                    other.getEffectiveWidth(), other.getEffectiveHeight())) {
                return true;
            }
        }
        return false;
    }

    private boolean collidesMovedImageAt(GraphicTextureItem movedImage, GraphicImage other, double testX, double testY) {
        Point2D old = movedImage.getPosition();
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
        Point2D old = movedItem.getPosition();
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
                other.getX(), other.getY(),
                other.getEffectiveWidth(), other.getEffectiveHeight());
    }

    
    
    

    public abstract static class GraphicItemBuilder<T extends GraphicItem, B extends GraphicItemBuilder<T, B>> {

        protected double width = 100;
        protected double height = 36;
        protected boolean moveable = false;
        protected boolean blockOthers = true;
        protected boolean visible = true; 
        protected Position.Builder position;

        protected void setInternalMoveable(boolean value) {
            this.moveable = value;
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

            item.setInternalMoveable(this.moveable);

            if (blockOthers) item.blockOthers();
            else item.allowOverlap();

            item.visible = this.visible; 

            item.position = positionBuilder;
            item.build();
            item.recalcPosition();
            item.applyVisibility(); 

            return item;
        }

        protected void configure(T item) {
        }

        protected abstract T create();
    }
}