package de.jakob.game.gui.generic;

import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.util.Position;
import de.jakob.game.input.ActionType;
import de.jakob.game.input.KeyBinds;
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
        getWindow().addKeyBindListener(KeyBinds.DefaultBind.EXIT.getKeyBind(),ActionType.PRESS, this::showExit);

        return this;
    }

    public MainGraphicUserInterface debugGUI(GraphicUserInterface gui) {
        this.debugGUI = gui;

        if (gui != null) {
            gui.hide();
        }

        getWindow().addKeyBindListener(KeyBinds.DefaultBind.DEBUG_SCREEN.getKeyBind(),ActionType.PRESS, this::toggleDebug);

        return this;
    }

    public void showExit() {
        if (exitGUI == null) return;
        if(exitGUI.isShown()) return;
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

        container.setStyle(
                "-fx-background-color: " + getBackgroundColor().toCSS() + ";"
        );

        container.getChildren().add(getContent());

        setContainer(container);
        setBuilt(true);

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