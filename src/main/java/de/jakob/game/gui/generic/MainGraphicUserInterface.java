package de.jakob.game.gui.generic;

import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.graphics.basic.GraphicText;
import de.jakob.game.gui.util.Position;
import javafx.scene.layout.Pane;

public class MainGraphicUserInterface extends GraphicUserInterface {

    private GraphicUserInterface exitGUI;
    private GraphicUserInterface debugGUI;

    private boolean debugVisible = false;

    public MainGraphicUserInterface(GraphicWindow window) {
        super(window);
    }

    public MainGraphicUserInterface exitGUI(GraphicUserInterface gui) {
        this.exitGUI = gui;

        if (gui != null) {
            gui.hide();
        }

        getWindow().addKeyBindListener(de.jakob.game.input.KeyBinds.DefaultBind.EXIT.getKeyBind(), de.jakob.game.input.ActionType.PRESS, this::showExit);

        return this;
    }

    public MainGraphicUserInterface debugGUI(GraphicUserInterface gui) {
        this.debugGUI = gui;

        if (gui != null) {
            gui.hide();
        }

        getWindow().addKeyBindListener(de.jakob.game.input.KeyBinds.DefaultBind.DEBUG_SCREEN.getKeyBind(), de.jakob.game.input.ActionType.PRESS, this::toggleDebug);

        return this;
    }

    public void showExit() {
        if (exitGUI == null) return;
        if (exitGUI.isShown()) return;
        exitGUI.show();
        getWindow().focus(exitGUI);
    }

    public void toggleDebug() {
        if (debugGUI == null) return;

        debugVisible = !debugVisible;

        if (debugVisible) {
            debugGUI.show();
            getWindow().focus(debugGUI);
        } else {
            debugGUI.hide();
        }
    }

    public boolean isDebugVisible() {
        return debugVisible;
    }

    @Override
    public GraphicUserInterface backgroundColor(de.jakob.game.color.Color color) {
        super.backgroundColor(color);
        return this;
    }

    @Override
    protected void refreshStyles() {
        Pane container = getRoot();
        if (container != null) {
            container.setStyle(
                    "-fx-background-color: " + getBackgroundColor().toCSS() + ";"
            );
        }
    }

    @Override
    public GraphicUserInterface create() {
        if (isBuilt()) return this;

        Pane container = new Pane();

        double width = getWindow().getViewportWidth();
        double height = getWindow().getViewportHeight();

        size((int) width, (int) height);
        position(Position.of(0, 0));

        container.setPrefSize(width, height);
        container.setManaged(false);

        getContent().setPrefSize(width, height);

        container.getChildren().add(getContent());

        setContainer(container);
        setBuilt();
        refreshStyles();

        return this;
    }

    @Override
    public MainGraphicUserInterface show() {
        getWindow().setActiveInterface(this);
        getWindow().updateZOrder();
        return this;
    }

    @Override
    public void hide() {
    }
}