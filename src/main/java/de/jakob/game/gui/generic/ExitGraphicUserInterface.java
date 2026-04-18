package de.jakob.game.gui.generic;

import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.graphics.basic.GraphicButton;
import de.jakob.game.gui.util.Alignment;
import de.jakob.game.gui.util.Position;
import de.jakob.game.scheduler.GameScheduler;

public class ExitGraphicUserInterface extends GraphicUserInterface {

    public ExitGraphicUserInterface(GraphicWindow window) {
        super(window);
    }

    public static ExitGraphicUserInterface create(GraphicWindow window, GameScheduler scheduler) {
        if (window == null) throw new IllegalArgumentException("window must not be null");
        if (scheduler == null) throw new IllegalArgumentException("scheduler must not be null");

        ExitGraphicUserInterface gui = new ExitGraphicUserInterface(window);

        gui.size(320, 220)
                .title("Spielmenü")
                .interactiveAlways()
                .alwaysInFront()
                .align(Alignment.CENTER);

        gui.addItem(
                GraphicButton.builder()
                        .text("Weiter")
                        .size(160, 30)
                        .backgroundColor(NamedColor.GREEN)
                        .textColor(NamedColor.WHITE)
                        .onClick(gui::hide),
                Position.of(Alignment.CENTER).marginTop(0.225)
        );

        gui.addItem(
                GraphicButton.builder()
                        .text("Einstellungen")
                        .size(160, 30)
                        .backgroundColor(NamedColor.BLUE)
                        .textColor(NamedColor.WHITE)
                        .onClick(gui::hide),
                Position.of(Alignment.CENTER)
        );

        gui.addItem(
                GraphicButton.builder()
                        .text("Beenden")
                        .size(160, 30)
                        .backgroundColor(NamedColor.RED)
                        .textColor(NamedColor.WHITE)
                        .onClick(() -> {
                            System.exit(0);
                        }),
                Position.of(Alignment.TOP_CENTER).marginBottom(0.225)
        );

        gui.create();
        return gui;
    }
}