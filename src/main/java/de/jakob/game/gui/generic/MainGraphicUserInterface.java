package de.jakob.game.gui.generic;

import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.util.Position;
import javafx.scene.layout.Pane;

public class MainGraphicUserInterface extends GraphicUserInterface {

    public MainGraphicUserInterface(GraphicWindow window) {
        super(window);
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