package de.jakob.game.gui.generic;

import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.graphics.basic.GraphicText;
import de.jakob.game.scheduler.GameScheduler;

public class DebugGraphicUserInterface extends GraphicUserInterface {

    private final long startTime = System.nanoTime();
    private GraphicText debugText;
    private long lastTime = System.nanoTime();
    private int tickCounter = 0;
    private double tps = 0;

    public DebugGraphicUserInterface(GraphicWindow window) {
        super(window);
    }

    public static DebugGraphicUserInterface create(GraphicWindow window, GameScheduler scheduler) {
        if (window == null) throw new IllegalArgumentException("window must not be null");
        if (scheduler == null) throw new IllegalArgumentException("scheduler must not be null");

        DebugGraphicUserInterface gui = new DebugGraphicUserInterface(window);

        gui.size(260, 140)
                .position(10, 10)
                .title("DEBUG")
                .moveable()
                .interactiveAlways();

        gui.debugText = gui.addItemAndGet(
                GraphicText.builder()
                        .text("Loading debug...")
                        .color(NamedColor.GREEN)
                        .fontSize(12),
                10, 10
        );

        gui.create();
        gui.startUpdater(scheduler);

        return gui;
    }

    private void startUpdater(GameScheduler scheduler) {
        scheduler.runRepeating(this::updateText, 1, 1);
    }

    private void updateText() {
        GraphicWindow window = getWindow();

        double mouseX = window.getMouseX();
        double mouseY = window.getMouseY();

        tickCounter++;
        long now = System.nanoTime();

        if (now - lastTime >= 1_000_000_000L) {
            tps = tickCounter * (1_000_000_000.0 / (now - lastTime));
            tickCounter = 0;
            lastTime = now;
        }

        long elapsedNanos = now - startTime;
        double seconds = elapsedNanos / 1_000_000_000.0;

        String text = "Mouse: " + (int) mouseX + ", " + (int) mouseY + "\n" +
                "Time: " + String.format("%.2f", seconds) + "s\n" +
                "TPS: " + String.format("%.2f", tps);

        debugText.text(text);
    }
}