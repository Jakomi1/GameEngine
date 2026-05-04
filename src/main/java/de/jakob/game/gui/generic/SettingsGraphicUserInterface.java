package de.jakob.game.gui.generic;

import de.jakob.game.color.NamedColor;
import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.graphics.basic.GraphicButton;
import de.jakob.game.gui.graphics.input.GraphicLabeledSlider;
import de.jakob.game.gui.util.Alignment;
import de.jakob.game.gui.util.Position;
import de.jakob.game.scheduler.GameScheduler;
import de.jakob.game.sound.AudioConfig;
import de.jakob.game.sound.SoundPlayer;

public class SettingsGraphicUserInterface extends GraphicUserInterface{

    private SettingsGraphicUserInterface(GraphicWindow window) {
        super(window);
    }

    public static SettingsGraphicUserInterface create(GraphicWindow window, GameScheduler scheduler) {
        if (window == null) throw new IllegalArgumentException("window must not be null");
        if (scheduler == null) throw new IllegalArgumentException("scheduler must not be null");

        SettingsGraphicUserInterface gui = new SettingsGraphicUserInterface(window);
        gui.size(400, 300)
                .title("Einstellungen")
                .interactiveAlways()
                .moveable()
                .alwaysInFront()
                .align(Alignment.CENTER);
        gui.addItem(GraphicLabeledSlider.builder()
                        .size(150, 40)
                        .text("Lautstärke: "+(int)(SoundPlayer.getDefaultVolume()*100)+"%")
                        .range(0, 1)
                        .value(SoundPlayer.getDefaultVolume())
                        .onChange((s, volume) -> {
                            AudioConfig.setDefaultVolume(volume);
                            s.text("Lautstärke: "+(int)(volume*100)+"%");
                        })
                        .noBorder()
                        .transparentBackground()
                        .border(false)
                        .textColor(NamedColor.WHITE),
                Position.marginLeft(0.1).marginTop(0.15)
        );

        gui.addItem(GraphicButton.builder()
                        .size(150, 40)
                        .text("Tastenbelegung")
                        .onClick(() -> {
                            KeybindsGraphicUserInterface.create(window, scheduler).show();
                        }),
                Position.marginRight(0.1).marginTop(0.15)
        );

        gui.addItem(GraphicButton.builder()
                        .size(150, 40)
                        .text("???")

                        .onClick(() -> {
                            //KeybindsGraphicUserInterface.create(window, scheduler).show();
                        }),
                Position.marginRight(0.1).marginTop(0.35)
        );

        gui.addItem(GraphicButton.builder()
                        .size(150, 40)
                        .text("???")

                        .onClick(() -> {
                            //KeybindsGraphicUserInterface.create(window, scheduler).show();
                        }),
                Position.marginRight(0.1).marginTop(0.55)
        );

        gui.addItem(GraphicLabeledSlider.builder()
                        .size(150, 40)
                        .text("Ziel-TPS: "+(int)(GameScheduler.targetedTPS))
                        .range(1, 200)
                        .value((double) GameScheduler.targetedTPS)
                        .onChange((s, v) -> {
                            //SoundPlayer.targetedTPS = v;
                            s.text("Ziel-TPS: "+(int)(GameScheduler.targetedTPS));
                        })
                        .transparentBackground()
                        .border(false)
                        .textColor(NamedColor.WHITE)
                        .disable(),
                Position.marginLeft(0.1).marginTop(0.35)
        );

        gui.addItem(GraphicLabeledSlider.builder()
                        .size(150, 40)
                        .text("???")

                        .range(0, 1)
                        .value(SoundPlayer.getDefaultVolume())
                        .onChange((s, volume) -> {

                        })
                        .transparentBackground()
                        .border(false)
                        .textColor(NamedColor.WHITE)
                        .disable(),
                Position.marginLeft(0.1).marginTop(0.55)
        );


        gui.addItem(
                GraphicButton.builder()
                        .size(150, 40)
                        .text("Zurück")
                        .backgroundColor(NamedColor.RED)
                        .textColor(NamedColor.WHITE)
                        .onClick(gui::hide),
                Position.of(Alignment.CENTER).marginBottom(0.08)
        );

        gui.create();
        return gui;
    }

}
