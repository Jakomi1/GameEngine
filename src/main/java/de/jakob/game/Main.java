package de.jakob.game;

import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.graphics.basic.GraphicButton;
import de.jakob.game.gui.graphics.input.GraphicLabeledSlider;
import de.jakob.game.gui.graphics.polygon.GraphicRectangle;
import de.jakob.game.gui.util.Alignment;
import de.jakob.game.gui.util.Position;
import de.jakob.game.input.ActionType;
import de.jakob.game.input.Key;
import de.jakob.game.input.KeyBind;
import de.jakob.game.input.KeyBinds;
import de.jakob.game.scheduler.GameScheduler;

import java.awt.*;

public class Main extends GameEngine {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void onStart(GraphicWindow window, GameScheduler scheduler) {



        final int[] score = {0};
        final int[] highscore = {0};
        final double[] speed = {1.0};
        final int[] timeLeft = {30};

        window.main(m -> m
                .title("🎮 Catch the Block Deluxe")
                .backgroundColor(NamedColor.DARK_GRAY)
                .create()
                .show());
        /*window.addKeyBindListener(KeyBinds.registerOrGet("zoom","Zoom",Key.Z), ActionType.PRESS, () -> {
            window.main().zoomInAnimated(20,Position.of(400,400), 2000);
        });*/
        GraphicUserInterface game = window.main();
        GraphicRectangle target = game.addItemAndGet(
                GraphicRectangle.builder()
                        .color(NamedColor.RED)
                        .size(50, 50)
                        .moveable(),
                200, 150
        );

        GraphicButton scoreText = game.addItemAndGet(
                GraphicButton.builder()
                        .text("Score: 0")
                        .size(140, 30)
                        .backgroundColor(NamedColor.GRAY),
                10, 10
        );

        GraphicButton highscoreText = game.addItemAndGet(
                GraphicButton.builder()
                        .text("Highscore: 0")
                        .size(160, 30)
                        .backgroundColor(NamedColor.GRAY),
                160, 10
        );

        GraphicButton timerText = game.addItemAndGet(
                GraphicButton.builder()
                        .text("Time: 30")
                        .size(120, 30)

                        .backgroundColor(NamedColor.GRAY),
                330, 10
        );

        game.addItem(
                GraphicLabeledSlider.builder()
                        .range(1, 10)
                        .text("Schwierigkeit:")
                        .textColor(NamedColor.WHITE)
                        .value(1D)
                        .onChange(val -> speed[0] = val),
                10, 60
        );

        target.onClick(() -> {
            score[0]++;
            scoreText.text("Score: " + score[0]);

            target.color(NamedColor.values()[(int)(Math.random() * NamedColor.values().length)]);

            moveTarget(target, window);
        });

        Runnable mover = new Runnable() {
            @Override
            public void run() {
                moveTarget(target, window);
                scheduler.runLater(this, (long) Math.max(5, 50 / speed[0]));
            }
        };
        scheduler.runLater(mover, 0);

        GraphicUserInterface testGUI =  window.createGUI().moveable().position(Position.of(Alignment.TOP_RIGHT)).show();
        GraphicRectangle rectangle = testGUI.addItemAndGet(
                GraphicRectangle.builder()
                        .color(NamedColor.RED)

                        .size(200,200),
                Position.of(0, 0)
        );
        scheduler.runRepeating(() -> {
            timeLeft[0]--;
            timerText.text("Time: " + timeLeft[0]);
            if (timeLeft[0] <= 0) {
                if (score[0] > highscore[0]) {
                    highscore[0] = score[0];
                    highscoreText.text("Highscore: " + highscore[0]);
                }

                score[0] = 0;
                timeLeft[0] = 30;

                scoreText.text("Score: 0");
            }

        }, 0, 50);
    }

    private void moveTarget(GraphicRectangle target, GraphicWindow window) {
        double x = Math.random() * (window.main().getWindowWidth() - 50);
        double y = Math.random() * (window.main().getWindowHeight() - 50);
        target.position(Position.of(x, y));
    }

    @Override
    public String getName() {
        return "CatchTheBlockDeluxe";
    }

    @Override
    public void secondLoop() {}

    @Override
    public void tickLoop() {}
}