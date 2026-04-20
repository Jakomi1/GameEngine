package de.jakob.game.gui.generic;

import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.graphics.basic.GraphicText;
import de.jakob.game.scheduler.GameScheduler;

public class DebugGraphicUserInterface extends GraphicUserInterface {

    private GraphicText debugText;
    private final GameScheduler scheduler;

    private double tps = 0;
    private long lastTime = System.currentTimeMillis();

    private DebugGraphicUserInterface(GraphicWindow window, GameScheduler scheduler) {
        super(window);
        this.scheduler = scheduler;
    }

    public static DebugGraphicUserInterface create(GraphicWindow window, GameScheduler scheduler) {
        DebugGraphicUserInterface gui = new DebugGraphicUserInterface(window, scheduler);

        gui.size(260, 140)
                .position(10, 10)
                .title("Informations - Menü")
                .moveable()
                .interactiveAlways();

        gui.debugText = gui.addItemAndGet(
                GraphicText.builder()
                        .text("Lade Daten...")
                        .color(NamedColor.GREEN)
                        .fontSize(12),
                10, 10
        );

        gui.create();
        scheduler.runRepeating(gui::updateText, 1, 1);

        return gui;
    }

    private void updateText() {
        if(!isShown()) return;
        GraphicWindow window = getWindow();

        double mouseX = window.getMouseX();
        double mouseY = window.getMouseY();

        long now = System.currentTimeMillis();

        if (now - lastTime >= 1000) {
            tps = scheduler.getTPS();
            lastTime = now;
        }

        double seconds = (System.currentTimeMillis() - scheduler.getStartTimeMillis()) / 1000.0;

        String text =
                "Maus: " + (int) mouseX + ", " + (int) mouseY + "\n" +
                        "Zeit: " + String.format("%.2f", seconds) + "s\n" +
                        ((tps != 0)?( "TPS: " + String.format("%.2f", tps)):(""));

        debugText.text(text);
    }
}