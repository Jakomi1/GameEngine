package de.jakob.game.gui;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.generic.MainGraphicUserInterface;
import de.jakob.game.gui.graphics.GraphicItem;
import de.jakob.game.gui.graphics.basic.GraphicButton;
import de.jakob.game.gui.graphics.basic.GraphicText;
import de.jakob.game.gui.util.Alignment;
import de.jakob.game.gui.util.Position;
import de.jakob.game.input.ActionType;
import de.jakob.game.input.Key;
import javafx.geometry.Point2D;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphicUserInterface {

    public static final double TOP_BAR_HEIGHT = 30.0;
    public static final double EXTRA_SIZE = 2;
    private final GraphicWindow window;
    private final Pane content;

    private Pane container;
    private Pane topBar;
    private BorderPane frame;

    private boolean alwaysInBack = false;
    private boolean alwaysInFront = false;
    private boolean blockOthers = false;
    private boolean onlyInteractiveWhenActive = false;

    private int width = 400;
    private int height = 300;
    private boolean moveable = false;
    private boolean closeButton = false;
    private String title = "";

    private Color mainColor = NamedColor.BACKGROUND;
    private Color topBarColor = null;
    private Color titleColor = null;

    private boolean built = false;

    private Position.Builder position = Position.of(0, 0);

    private final Map<Key, Map<ActionType, List<Runnable>>> listeners = new HashMap<>();

    private double dragOffsetX = 0;
    private double dragOffsetY = 0;
    private boolean dragging = false;

    public GraphicUserInterface(GraphicWindow window) {
        this.window = window;
        this.content = new Pane();
        this.content.setPickOnBounds(true);
    }

    public GraphicUserInterface addListener(Key key, Runnable action) {
        return addListener(key, ActionType.PRESS, action);
    }

    public GraphicUserInterface addListener(Key key, ActionType type, Runnable action) {
        if (key == null || type == null || action == null) return this;

        listeners
                .computeIfAbsent(key, k -> new HashMap<>())
                .computeIfAbsent(type, t -> new ArrayList<>())
                .add(action);

        return this;
    }

    private double getOuterWidth() {
        return width + EXTRA_SIZE;
    }

    private double getOuterHeight() {
        return height + EXTRA_SIZE;
    }

    protected boolean fire(Key incoming, ActionType type) {
        boolean executed = false;

        for (Map.Entry<Key, Map<ActionType, List<Runnable>>> entry : listeners.entrySet()) {
            if (!entry.getKey().matches(incoming)) continue;

            List<Runnable> actions = entry.getValue().get(type);
            if (actions == null || actions.isEmpty()) continue;

            executed = true;
            for (Runnable action : actions) {
                action.run();
            }
        }

        return executed;
    }

    protected boolean hasListener(Key incoming, ActionType type) {
        for (Map.Entry<Key, Map<ActionType, List<Runnable>>> entry : listeners.entrySet()) {
            if (!entry.getKey().matches(incoming)) continue;

            List<Runnable> actions = entry.getValue().get(type);
            if (actions != null && !actions.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public GraphicUserInterface size(int width, int height) {
        this.width = width;
        this.height = height;
        applyLayoutChanges();
        return this;
    }

    public GraphicUserInterface position(Position.Builder builder) {
        this.position = builder != null ? builder : Position.of(0, 0);
        applyPositionToContainer();
        return this;
    }

    public GraphicUserInterface position(double x, double y) {
        return position(Position.of(x, y));
    }

    public GraphicUserInterface titleColor(Color color) {
        this.titleColor = color;
        refreshTopBarContent();
        return this;
    }

    public GraphicUserInterface align(Alignment alignment) {
        return position(Position.of(alignment));
    }

    public GraphicUserInterface moveable() {
        this.moveable = true;
        installMoveHandlers();
        return this;
    }

    public GraphicUserInterface closeable() {
        this.closeButton = true;
        refreshTopBarContent();
        return this;
    }

    public GraphicUserInterface title(String title) {
        this.title = title != null ? title : "";
        refreshTopBarContent();
        return this;
    }

    public GraphicUserInterface backgroundColor(Color color) {
        this.mainColor = color != null ? color : Color.fromHex("#2b2b2b");
        refreshStyles();
        return this;
    }

    public GraphicUserInterface topBar(Color color) {
        this.topBarColor = color;
        refreshStyles();
        return this;
    }

    public GraphicUserInterface alwaysInBack() {
        this.alwaysInBack = true;
        this.alwaysInFront = false;
        if (built) {
            window.updateZOrder();
        }
        return this;
    }

    public GraphicUserInterface alwaysInFront() {
        this.alwaysInFront = true;
        this.alwaysInBack = false;
        if (built) {
            window.updateZOrder();
        }
        return this;
    }

    public GraphicUserInterface normalZOrder() {
        this.alwaysInBack = false;
        this.alwaysInFront = false;
        if (built) {
            window.updateZOrder();
        }
        return this;
    }

    public GraphicUserInterface blockOthers() {
        this.blockOthers = true;
        return this;
    }

    public GraphicUserInterface onlyInteractiveWhenActive() {
        this.onlyInteractiveWhenActive = true;
        return this;
    }

    public GraphicUserInterface interactiveAlways() {
        this.onlyInteractiveWhenActive = false;
        return this;
    }

    public GraphicUserInterface interactiveWhenActive(boolean value) {
        this.onlyInteractiveWhenActive = value;
        return this;
    }

    public boolean blocksOthers() {
        return blockOthers;
    }

    public boolean isAlwaysInBack() {
        return alwaysInBack;
    }

    public boolean isAlwaysInFront() {
        return alwaysInFront;
    }

    public boolean isOnlyInteractiveWhenActive() {
        return onlyInteractiveWhenActive;
    }

    public boolean isMoveable() {
        return moveable;
    }

    public double getContentHeight() {
        return content.getHeight();
    }

    public double getContentWidth() {
        return content.getWidth();
    }

    public GraphicUserInterface addItem(GraphicItem.GraphicItemBuilder<?, ?> builder, double x, double y) {
        return addItem(builder, Position.of(x, y));
    }

    public GraphicUserInterface addItem(GraphicItem.GraphicItemBuilder<?, ?> builder, Position.Builder positionBuilder) {
        GraphicItem item = builder.build(this, positionBuilder);
        content.getChildren().add(item.getNode());
        return this;
    }

    public <T extends GraphicItem> T addItemAndGet(GraphicItem.GraphicItemBuilder<T, ?> builder, double x, double y) {
        return addItemAndGet(builder, Position.of(x, y));
    }

    public <T extends GraphicItem> T addItemAndGet(GraphicItem.GraphicItemBuilder<T, ?> builder, Position.Builder positionBuilder) {
        T item = builder.build(this, positionBuilder);
        content.getChildren().add(item.getNode());
        return item;
    }

    public GraphicUserInterface create() {
        if (built) {
            return this;
        }

        if (width <= 0 || height <= 0) {
            throw new IllegalStateException("Overlay GUI braucht size()");
        }

        container = new Pane();
        container.setPrefSize(getOuterWidth(), getOuterHeight());
        container.setManaged(false);
        container.setPickOnBounds(false);
        container.setUserData(this);

        frame = new BorderPane();
        frame.setPrefSize(width, height);
        frame.setEffect(new DropShadow(20, javafx.scene.paint.Color.BLACK));

        topBar = new Pane();
        topBar.setPrefSize(width, TOP_BAR_HEIGHT);

        content.setPrefSize(width, Math.max(0, height - TOP_BAR_HEIGHT));

        frame.setTop(topBar);
        frame.setCenter(content);
        container.getChildren().add(frame);

        content.setUserData(this);

        applyLayoutChanges();
        refreshStyles();
        refreshTopBarContent();
        applyPositionToContainer();
        installMoveHandlers();

        container.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> window.focus(this));

        built = true;
        applyPositionToContainer();

        return this;
    }

    public boolean isShown() {
        return built
                && container != null
                && container.isVisible()
                && window.getRoot().getChildren().contains(container);
    }

    public GraphicUserInterface show() {
        if (!built) {
            create();
        }

        applyPositionToContainer();

        if (!window.getRoot().getChildren().contains(container)) {
            window.addOverlay(container);
            window.register(this);
        }

        container.setVisible(true);
        window.focus(this);
        window.updateZOrder();
        return this;
    }

    public void hide() {
        if (!built) return;

        container.setVisible(false);
        window.onInterfaceHidden(this);
    }

    private void applyLayoutChanges() {
        if (container != null) {
            container.setPrefSize(getOuterWidth(), getOuterHeight());
        }

        if (frame != null) {
            frame.setPrefSize(width, height);
        }

        if (topBar != null) {
            topBar.setPrefSize(width, TOP_BAR_HEIGHT);
        }

        content.setPrefSize(width, Math.max(0, height - TOP_BAR_HEIGHT));
        refreshTopBarContent();
        refreshStyles();
        applyPositionToContainer();
    }

    private void refreshStyles() {
        if (frame != null) {
            frame.setStyle(
                    "-fx-background-color: " + mainColor.toCSS() + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: " + mainColor.darker(0.28).toCSS() + ";" +
                            "-fx-border-radius: 10;"
            );
        }

        if (topBar != null) {
            Color resolvedTop = resolveTopBarColor();
            topBar.setStyle(
                    "-fx-background-color: " + resolvedTop.toCSS() + ";" +
                            "-fx-background-radius: 10 10 0 0;"
            );
        }

        refreshTopBarContent();
    }

    private void refreshTopBarContent() {
        if (topBar == null) return;

        topBar.getChildren().clear();

        Color resolvedTop = resolveTopBarColor();

        if (!title.isEmpty()) {
            Color textColor = titleColor != null
                    ? titleColor
                    : resolveTextColor(resolvedTop);

            GraphicText text = GraphicText.builder()
                    .text(title)
                    .fontSize(14)
                    .color(textColor)
                    .build(this, Position.of(10, 7));

            topBar.getChildren().add(text.getNode());
        }

        if (closeButton) {
            GraphicText xText = GraphicText.builder()
                    .text("X")
                    .bold(true)
                    .fontSize(14)
                    .color(NamedColor.WHITE)
                    .build(this, Position.of(0, 0));

            GraphicButton close = GraphicButton.builder()
                    .graphicText(xText)
                    .backgroundColor(NamedColor.RED)
                    .textColor(NamedColor.WHITE)
                    .onClick(this::hide)
                    .size(30, 20)
                    .build(this, Position.of(width - 40, 5));

            topBar.getChildren().add(close.getNode());
        }
    }

    private Color resolveTopBarColor() {
        return topBarColor != null
                ? topBarColor
                : mainColor.darker(0.12);
    }

    private void installMoveHandlers() {
        if (topBar == null) return;
        if (!moveable) return;

        topBar.setOnMousePressed(e -> {
            window.focus(this);
            dragging = true;
            dragOffsetX = e.getSceneX() - container.getLayoutX();
            dragOffsetY = e.getSceneY() - container.getLayoutY();
            e.consume();
        });

        topBar.setOnMouseDragged(e -> {
            if (!dragging) return;

            double newX = e.getSceneX() - dragOffsetX;
            double newY = e.getSceneY() - dragOffsetY;

            moveContainer(newX, newY);
            e.consume();
        });

        topBar.setOnMouseReleased(e -> {
            dragging = false;
            e.consume();
        });
    }

    private void applyPositionToContainer() {
        if (container == null) return;

        Point2D point = position.get(window, this);

        double viewportWidth = window.getViewportWidth();
        double viewportHeight = window.getViewportHeight();

        double clampedX = clamp(point.getX(), 0, Math.max(0, viewportWidth - getOuterWidth()));
        double clampedY = clamp(point.getY(), 0, Math.max(0, viewportHeight - getOuterHeight()));

        container.relocate(clampedX, clampedY);
    }

    private void moveContainer(double newX, double newY) {
        if (container == null) return;

        double maxX = Math.max(0, window.getViewportWidth() - getOuterWidth());
        double maxY = Math.max(0, window.getViewportHeight() - getOuterHeight());
        double clampedTargetX = clamp(newX, 0, maxX);
        double clampedTargetY = clamp(newY, 0, maxY);

        double currentX = container.getLayoutX();
        double currentY = container.getLayoutY();

        double maxStep = 25.0;

        double dx = clampedTargetX - currentX;
        double dy = clampedTargetY - currentY;

        dx = clamp(dx, -maxStep, maxStep);
        dy = clamp(dy, -maxStep, maxStep);

        double nextX = currentX + dx;
        double nextY = currentY + dy;

        if (!window.canPlace(this, nextX, currentY)) {
            nextX = currentX;
        }

        if (!window.canPlace(this, nextX, nextY)) {
            nextY = currentY;
        }

        container.relocate(nextX, nextY);
        this.position = Position.of(nextX, nextY);
    }

    protected boolean containsScenePoint(double sceneX, double sceneY) {
        if (!built || container == null || container.getScene() == null) return false;
        Point2D local = container.sceneToLocal(sceneX, sceneY);
        return container.getBoundsInLocal().contains(local);
    }

    protected boolean isPointOnTopBar(double sceneX, double sceneY) {
        if (!built || topBar == null || topBar.getScene() == null) return false;
        Point2D local = topBar.sceneToLocal(sceneX, sceneY);
        return topBar.getBoundsInLocal().contains(local);
    }

    protected GraphicWindow getWindow() {
        return window;
    }

    protected Pane getContent() {
        return content;
    }

    protected void setContainer(Pane container) {
        this.container = container;
    }

    protected void setBuilt(boolean built) {
        this.built = built;
    }

    protected boolean isBuilt() {
        return built;
    }

    public Pane getRoot() {
        return container != null ? container : content;
    }

    public double getWindowWidth() {
        if (width > 0) return width;

        if (this instanceof MainGraphicUserInterface) {
            return window.getViewportWidth();
        }

        return width;
    }

    public double getWindowHeight() {
        if (height > 0) return height;

        if (this instanceof MainGraphicUserInterface) {
            return window.getViewportHeight();
        }

        return height;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    protected Color getBackgroundColor() {
        return mainColor;
    }

    private Color resolveTextColor(Color background) {
        if (background == null) return NamedColor.WHITE;
        return background.isLight() ? NamedColor.BLACK : NamedColor.WHITE;
    }
}