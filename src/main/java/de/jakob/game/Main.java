package de.jakob.game;

import de.jakob.game.color.Color;
import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.generic.DebugGraphicUserInterface;
import de.jakob.game.gui.generic.ExitGraphicUserInterface;
import de.jakob.game.gui.graphics.basic.GraphicButton;
import de.jakob.game.gui.graphics.input.GraphicIntegerInputField;
import de.jakob.game.gui.graphics.input.GraphicSlider;
import de.jakob.game.gui.graphics.input.GraphicTextInputField;
import de.jakob.game.gui.graphics.media.GraphicAnimatedImage;
import de.jakob.game.gui.graphics.media.GraphicAnimation;
import de.jakob.game.gui.graphics.polygon.GraphicRectangle;
import de.jakob.game.gui.util.Alignment;
import de.jakob.game.gui.util.Position;
import de.jakob.game.input.Key;
import de.jakob.game.logger.Logger;
import de.jakob.game.scheduler.GameScheduler;
import de.jakob.game.sound.SoundPlayer;

public class Main extends GameEngine {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void onStart(GraphicWindow window, GameScheduler scheduler) {
        window.main(m -> m
                        .addItem(
                                GraphicButton.builder()
                                        .text("Hey")
                                        .size(100, 100),
                                100, 500
                        )
                        .addItem(
                                GraphicAnimation.builder()
                                        .path("fire.gif")
                                        .moveable()
                                        .size(250, 250),
                                Position.of(Alignment.CENTER)
                        )/*
                        .addItem(
                                GraphicTextInputField.builder()
                                        .value("Hello")
                                        .transparentBackground()
                                        .textColor(NamedColor.WHITE)
                                        .size(200, 30),
                                        //.onChange(val -> Logger.info("Text: " + val)),
                                Position.of(100, 200)
                        )

                        .addItem(
                                GraphicIntegerInputField.builder()
                                        .value(42),
                                        //.onChange(val -> Logger.info("Number: " + val)),
                                Position.of(100, 250)
                        )

                        .addItem(
                                GraphicSlider.builder()
                                        .range(0, 10)
                                        .value(5D),
                                        //.onChange(val -> Logger.info("Slider: " + val)),
                                Position.of(100, 300)
                        )*/
                        .addListener(Key.A, () -> Logger.info(("AAA")))
                        .addItem(
                                GraphicAnimatedImage.builder()
                                        .moveable()
                                        .frames("stone.png","dirt.png")
                                        .frameTime(1)
                                        .size(80, 80)
                                        .scheduler(scheduler),
                                Position.of(Alignment.BOTTOM_RIGHT)
                        )

                )
                .onExit(() -> System.exit(0));

        GraphicUserInterface inventory = window.gui()
                .size(300, 200)
                .position(100, 800)
                .title("Inventar")
                .addItem(
                        GraphicButton.builder()
                                .text("Item 1")
                                .blockOthers()
                                .backgroundColor(Color.fromHex("#aaffaa"))
                                .onClick(() -> {
                                    SoundPlayer.play("error.mp3");
                                    scheduler.runLater(() -> Logger.info(("3 Seconds Later!")),50*3);

                                })
                                .size(100, 30),
                        Position.of(Alignment.CENTER)
                )
                .addItem(
                        GraphicButton.builder()
                                .text("Item 2"),
                        Position.of(Alignment.TOP_CENTER)
                )
                .addListener(Key.A, () -> Logger.info("AAA INVENTAR"))
                .moveable()
                .create()
                .show();

       //window.main().exitGUI(ExitGraphicUserInterface.create(window, scheduler));

        GraphicUserInterface settings = window.gui()
                .size(400, 400)
                .position(450, 600)
                .title("Test")
                .backgroundColor(NamedColor.WHITE)

                .addItem(
                        GraphicRectangle.builder()
                                .color(NamedColor.RED)
                                .size(100, 40)
                                .moveable(),
                        Position.of(Alignment.TOP_CENTER)
                )
                .addListener(Key.A, () -> Logger.info("AAA"))
                .closeable()
                .moveable()
                .create()
                .show();
        GraphicRectangle block = settings.addItemAndGet(
                GraphicRectangle.builder()
                        .color(NamedColor.RED)
                        .size(100, 40)
                        .moveable(),
                100, 100
        );
        block.color(NamedColor.GREEN);
        GraphicButton button = settings.addItemAndGet(
                GraphicButton.builder().text("HEY").size(10,10),10,10
        );
        button.backgroundColor(NamedColor.RED);
    }

    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public void secondLoop() {
        //Logger.info("HEY1");
    }

    @Override
    public void tickLoop() {
        //Logger.info("HEY2");
    }
}