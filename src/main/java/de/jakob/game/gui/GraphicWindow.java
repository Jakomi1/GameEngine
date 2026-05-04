package de.jakob.game.gui;

import de.jakob.game.gui.generic.MainGraphicUserInterface;
import de.jakob.game.input.ActionType;
import de.jakob.game.input.Key;
import de.jakob.game.input.KeyBind;
import de.jakob.game.input.KeyBinds;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@SuppressWarnings({"FieldCanBeLocal", "ConfusingMainMethod"})
public class GraphicWindow {

    private final Stage stage;
    private final MainGraphicUserInterface root;
    private final String windowName;
    private final Size size;
    private final List<GraphicUserInterface> interfaces = new ArrayList<>();
    private GraphicUserInterface exitInterface;
    private boolean sceneInitialized = false;
    private GraphicUserInterface activeInterface;
    private GraphicUserInterface lastFocusedInterface;
    private GraphicUserInterface hoveredInterface;
    private double lastMouseSceneX = Double.NaN;
    private double lastMouseSceneY = Double.NaN;
    private Runnable onExit;
    private final List<WindowKeyBinding> bindedKeyBindings = new ArrayList<>();
    public GraphicWindow(Stage stage) {
        this(stage, "Main Window", Size.of(800, 600));
    }

    public GraphicWindow(Stage stage, String windowName, Size size) {
        this.stage = stage;
        this.windowName = windowName != null ? windowName : "Main Window";
        this.size = size != null ? size : Size.of(800, 600);

        this.root = new MainGraphicUserInterface(this);
        stage.setTitle(this.windowName);

        if (this.size.isFullscreen()) {
            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            stage.setFullScreen(true);
        }

        stage.setOnCloseRequest(e -> {
            if (onExit != null) {
                onExit.run();
            }
        });
    }


    public GraphicWindow main(Consumer<MainGraphicUserInterface> consumer) {
        Objects.requireNonNull(consumer);

        root.size((int) getViewportWidth(), (int) getViewportHeight());

        consumer.accept(root);

        if (!root.isBuilt()) {
            root.create();
        }

        if (!sceneInitialized) {
            Scene scene = new Scene(root.getRoot(), getViewportWidth(), getViewportHeight());

            scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> updateMouseContext(e.getSceneX(), e.getSceneY()));
            scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> updateMouseContext(e.getSceneX(), e.getSceneY()));

            scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                updateMouseContext(e.getSceneX(), e.getSceneY());

                Key key = mouseKey(e);
                GraphicUserInterface target = resolveMouseTarget(e.getSceneX(), e.getSceneY(), key, ActionType.PRESS);

                if (target != null) {
                    setActiveInterface(target);
                    if (target != root) {
                        lastFocusedInterface = target;
                    }
                    target.fire(key, ActionType.PRESS);
                    //e.consume();
                    return;
                }

                if (root.isBuilt() && root.getRoot() != null && root.getRoot().isVisible() && root.hasListener(key, ActionType.PRESS)) {
                    setActiveInterface(root);
                    root.fire(key, ActionType.PRESS);
                    e.consume();
                }
            });

            scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
                updateMouseContext(e.getSceneX(), e.getSceneY());

                Key key = mouseKey(e);
                GraphicUserInterface target = resolveMouseTarget(e.getSceneX(), e.getSceneY(), key, ActionType.RELEASE);

                if (target != null) {
                    target.fire(key, ActionType.RELEASE);
                    //e.consume();
                    return;
                }

                if (root.isBuilt() && root.getRoot() != null && root.getRoot().isVisible() && root.hasListener(key, ActionType.RELEASE)) {
                    root.fire(key, ActionType.RELEASE);
                    e.consume();
                }
            });

            scene.setOnKeyPressed(e -> {
                Key key = Key.from(e.getCode());
                if (dispatchInput(key, ActionType.PRESS)) {
                    e.consume();
                    return;
                }
            });

            scene.setOnKeyReleased(e -> {
                Key key = Key.from(e.getCode());
                if (dispatchInput(key, ActionType.RELEASE)) {
                    e.consume();
                }
            });

            stage.setScene(scene);
            sceneInitialized = true;
        }

        setActiveInterface(root);

        stage.show();
        forceFocus();

        return this;
    }
    public GraphicWindow addBindedKeyListener(KeyBind bind, ActionType type, Runnable runnable) {
        if (bind != null && type != null && runnable != null) {
            bindedKeyBindings.add(new WindowKeyBinding(bind, type, runnable));
        }
        return this;
    }


    private void fireWindowKeyListeners(Key key, ActionType type) {
        for (WindowKeyBinding binding : bindedKeyBindings) {

            KeyBind bind = binding.bind();

            if (bind == null) return;

            Key expected = bind.getKey(); // oder bind.key()

            if (expected == key && binding.type() == type) {
                binding.actionRunnable().run();
            }
        }
    }
    public boolean dispatchInput(Key key, ActionType type) {
        if (key == null || type == null) return false;


        fireWindowKeyListeners(key, type);

        boolean handled = false;

        if (isMouseKey(key)) {
            if (Double.isNaN(lastMouseSceneX) || Double.isNaN(lastMouseSceneY)) {
                handled = root != null && root.hasListener(key, type) && root.fire(key, type);
            } else {
                GraphicUserInterface target =
                        resolveMouseTarget(lastMouseSceneX, lastMouseSceneY, key, type);

                if (target != null) {
                    handled = target.fire(key, type);
                } else if (root != null && root.hasListener(key, type)) {
                    handled = root.fire(key, type);
                }
            }
        } else {
            GraphicUserInterface target = resolveKeyboardTarget(key, type);

            if (target != null) {
                handled = target.fire(key, type);
            } else {
                handled = root != null && root.hasListener(key, type) && root.fire(key, type);
            }
        }

        return handled;
    }


    public GraphicWindow onExit(Runnable action) {
        this.onExit = action;
        return this;
    }

    public GraphicWindow exitGUI(GraphicUserInterface gui) {
        this.exitInterface = gui;

        if (gui != null) {
            register(gui);
            gui.hide();
        }

        return this;
    }

    public MainGraphicUserInterface main() {
        return root;
    }

    public GraphicUserInterface gui() {
        return new GraphicUserInterface(this);
    }


    public boolean dispatchInput(Key key, ActionType type, double sceneX, double sceneY) {
        if (key == null || type == null) return false;

        updateMouseContext(sceneX, sceneY);

        if (!isMouseKey(key)) {
            return dispatchInput(key, type);
        }

        GraphicUserInterface target = resolveMouseTarget(sceneX, sceneY, key, type);
        if (target != null) {
            return target.fire(key, type);
        }

        if (root != null && root.hasListener(key, type)) {
            return root.fire(key, type);
        }

        return false;
    }

    private void updateMouseContext(double sceneX, double sceneY) {
        lastMouseSceneX = sceneX;
        lastMouseSceneY = sceneY;
        hoveredInterface = resolveHoveredInterface(sceneX, sceneY);
    }

    private GraphicUserInterface resolveKeyboardTarget(Key key, ActionType type) {
        if (Double.isNaN(lastMouseSceneX) || Double.isNaN(lastMouseSceneY)) {
            return null;
        }

        for (Node node : getSceneChildrenTopToBottom()) {
            GraphicUserInterface gui = guiForNode(node);
            if (gui == null || gui == root) continue;
            if (!isVisibleAndBuilt(gui)) continue;
            if (!gui.containsScenePoint(lastMouseSceneX, lastMouseSceneY)) continue;

            if (gui.isOnlyInteractiveWhenActive() && gui != activeInterface) {
                continue;
            }

            if (gui.hasListener(key, type)) {
                return gui;
            }
        }

        if (root != null) {
            if (root.isOnlyInteractiveWhenActive() && root != activeInterface) {
                return null;
            }
            if (root.hasListener(key, type)) {
                return root;
            }
        }

        return null;
    }

    private GraphicUserInterface resolveMouseTarget(double sceneX, double sceneY, Key key, ActionType type) {
        for (Node node : getSceneChildrenTopToBottom()) {
            GraphicUserInterface gui = guiForNode(node);
            if (gui == null || gui == root) continue;
            if (!isVisibleAndBuilt(gui)) continue;
            if (!gui.containsScenePoint(sceneX, sceneY)) continue;

            if (gui.isOnlyInteractiveWhenActive() && gui != activeInterface) {
                continue;
            }

            if (gui.hasListener(key, type)) {
                return gui;
            }
        }

        if (root != null
                && root.isBuilt()
                && root.getRoot() != null
                && root.getRoot().isVisible()
                && root.containsScenePoint(sceneX, sceneY)) {

            if (root.isOnlyInteractiveWhenActive() && root != activeInterface) {
                return null;
            }

            if (root.hasListener(key, type)) {
                return root;
            }
        }

        return null;
    }

    private GraphicUserInterface resolveHoveredInterface(double sceneX, double sceneY) {
        for (Node node : getSceneChildrenTopToBottom()) {
            GraphicUserInterface gui = guiForNode(node);
            if (gui == null) continue;
            if (!isVisibleAndBuilt(gui)) continue;
            if (gui.containsScenePoint(sceneX, sceneY)) {
                return gui;
            }
        }
        return null;
    }

    private List<Node> getSceneChildrenTopToBottom() {
        Pane parent = root.getRoot();
        List<Node> children = parent != null ? parent.getChildren() : new ArrayList<>();
        List<Node> reversed = new ArrayList<>(children.size());

        for (int i = children.size() - 1; i >= 0; i--) {
            reversed.add(children.get(i));
        }

        return reversed;
    }

    private GraphicUserInterface guiForNode(Node node) {
        if (node == null) return null;

        Object userData = node.getUserData();
        if (userData instanceof GraphicUserInterface gui) {
            return gui;
        }

        if (root != null && (root.getRoot() == node || root.getContent() == node)) {
            return root;
        }

        for (GraphicUserInterface gui : interfaces) {
            if (gui != null && (gui.getRoot() == node || gui.getContent() == node)) {
                return gui;
            }
        }

        return null;
    }

    protected void register(GraphicUserInterface gui) {
        if (gui != null && !interfaces.contains(gui)) {
            interfaces.add(gui);
            if (gui.getRoot() != null) {
                gui.getRoot().setUserData(gui);
            }
            if (gui.getContent() != null) {
                gui.getContent().setUserData(gui);
            }
        }
    }

    public void focus(GraphicUserInterface gui) {
        if (gui == null) return;

        register(gui);

        activeInterface = gui;
        lastFocusedInterface = gui;

        if (gui != root && !gui.isAlwaysInBack()) {
            interfaces.remove(gui);
            interfaces.add(gui);
        }

        updateZOrder();
    }

    protected void onInterfaceHidden(GraphicUserInterface gui) {
        if (gui == null) return;

        if (activeInterface == gui || !isVisibleAndBuilt(activeInterface)) {
            activeInterface = resolveFallbackInterface(gui);
        }

        if (hoveredInterface == gui) {
            hoveredInterface = null;
        }

        updateZOrder();
    }

    private GraphicUserInterface resolveFallbackInterface(GraphicUserInterface hidden) {
        for (int i = interfaces.size() - 1; i >= 0; i--) {
            GraphicUserInterface gui = interfaces.get(i);
            if (gui == null || gui == hidden) continue;
            if (isVisibleAndBuilt(gui)) {
                return gui;
            }
        }

        if (isVisibleAndBuilt(root)) {
            return root;
        }

        return null;
    }

    private boolean isVisibleAndBuilt(GraphicUserInterface gui) {
        return gui != null
                && gui.isBuilt()
                && gui.getRoot() != null
                && gui.getRoot().isVisible();
    }

    public void updateZOrder() {
        Pane parent = root.getRoot();
        if (parent == null) return;

        List<Node> order = new ArrayList<>();
        java.util.Set<Node> added = new java.util.HashSet<>();

        java.util.function.Consumer<Node> addSafe = node -> {
            if (node == null) return;

            if (node == parent) return;

            if (added.contains(node)) return;

            added.add(node);
            order.add(node);
        };

        addSafe.accept(root.getContent());

        for (GraphicUserInterface gui : interfaces) {
            if (gui == null || gui == root || !gui.isBuilt()) continue;
            if (!gui.isAlwaysInBack()) continue;

            Pane n = gui.getRoot();
            if (n != null && n.isVisible()) {
                addSafe.accept(n);
            }
        }
        for (GraphicUserInterface gui : interfaces) {
            if (gui == null || gui == root || !gui.isBuilt()) continue;
            if (gui.isAlwaysInBack() || gui.isAlwaysInFront()) continue;

            Pane n = gui.getRoot();
            if (n != null && n.isVisible()) {
                addSafe.accept(n);
            }
        }

        for (GraphicUserInterface gui : interfaces) {
            if (gui == null || gui == root || !gui.isBuilt()) continue;
            if (!gui.isAlwaysInFront()) continue;

            Pane n = gui.getRoot();
            if (n != null && n.isVisible()) {
                addSafe.accept(n);
            }
        }
        parent.getChildren().setAll(order);
    }

    public boolean canPlace(GraphicUserInterface moving, double x, double y) {
        if (moving == null) return false;

        double w = moving.getWindowWidth();
        double h = moving.getWindowHeight();

        if (w <= 0 || h <= 0) return true;

        double viewportWidth = getViewportWidth();
        double viewportHeight = getViewportHeight();

        if (x < 0 || y < 0 || x + w > viewportWidth || y + h > viewportHeight) {
            return false;
        }

        for (GraphicUserInterface other : interfaces) {
            if (other == null || other == moving || !other.isBuilt()
                    || other.getRoot() == null || !other.getRoot().isVisible()) continue;

            boolean mustBlock = moving.blocksOthers() || other.blocksOthers();
            if (!mustBlock) continue;

            double ox = other.getRoot().getLayoutX();
            double oy = other.getRoot().getLayoutY();
            double ow = other.getWindowWidth();
            double oh = other.getWindowHeight();

            if (intersects(x, y, w, h, ox, oy, ow, oh)) return false;
        }

        return true;
    }

    private boolean intersects(double x1, double y1, double w1, double h1,
                               double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 &&
                x1 + w1 > x2 &&
                y1 < y2 + h2 &&
                y1 + h1 > y2;
    }

    protected void addOverlay(Node node) {
        root.getRoot().getChildren().add(node);
    }

    protected void removeOverlay(Node node) {
        root.getRoot().getChildren().remove(node);
    }

    public Pane getRoot() {
        return root.getRoot();
    }

    public Stage getStage() {
        return stage;
    }

    public void setFullScreen(boolean value) {
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(value);
    }

    public GraphicUserInterface getActiveInterface() {
        return activeInterface;
    }

    public void setActiveInterface(GraphicUserInterface gui) {
        this.activeInterface = gui;
    }

    public GraphicUserInterface getLastFocusedInterface() {
        return lastFocusedInterface;
    }

    public GraphicUserInterface getHoveredInterface() {
        return hoveredInterface;
    }

    private void forceFocus() {
        Platform.runLater(() -> {
            stage.setAlwaysOnTop(true);
            stage.toFront();
            stage.requestFocus();
            stage.setAlwaysOnTop(false);
        });
    }

    private boolean isMouseKey(Key key) {
        return key == Key.MOUSE_LEFT || key == Key.MOUSE_RIGHT;
    }

    private Key mouseKey(MouseEvent e) {
        return e.getButton() == MouseButton.PRIMARY ? Key.MOUSE_LEFT : Key.MOUSE_RIGHT;
    }

    public double getViewportWidth() {
        if (stage.getScene() != null) {
            return stage.getScene().getWidth();
        }

        if (size.isWindowed()) {
            return size.width();
        }

        Rectangle2D bounds = Screen.getPrimary().getBounds();
        return bounds.getWidth();
    }

    public double getViewportHeight() {
        if (stage.getScene() != null) {
            return stage.getScene().getHeight();
        }

        if (size.isWindowed()) {
            return size.height();
        }

        Rectangle2D bounds = Screen.getPrimary().getBounds();
        return bounds.getHeight();
    }

    public double getMouseX() {
        return lastMouseSceneX;
    }

    public double getMouseY() {
        return lastMouseSceneY;
    }

    public GraphicUserInterface getExitInterface() {
        return exitInterface;
    }

    public record Size(int width, int height, Mode mode) {
        public static final Size FULLSCREEN = new Size(-1, -1, Mode.FULLSCREEN);

        public static Size of(int width, int height) {
            return new Size(width, height, Mode.WINDOWED);
        }

        public boolean isFullscreen() {
            return mode == Mode.FULLSCREEN;
        }

        public boolean isWindowed() {
            return mode == Mode.WINDOWED;
        }

        private enum Mode {
            WINDOWED,
            FULLSCREEN
        }
    }
    public record WindowKeyBinding(KeyBind bind, ActionType type, Runnable actionRunnable) {}
}