package de.jakob.game.gui.generic;

import de.jakob.game.color.Color;
import de.jakob.game.gui.GraphicUserInterface;
import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.graphics.basic.GraphicButton;
import de.jakob.game.gui.graphics.basic.GraphicScrollPane;
import de.jakob.game.gui.graphics.basic.GraphicText;
import de.jakob.game.gui.util.Alignment;
import de.jakob.game.gui.util.Position;
import de.jakob.game.input.ActionType;
import de.jakob.game.input.Key;
import de.jakob.game.input.KeyBinds;
import de.jakob.game.scheduler.GameScheduler;
import javafx.scene.layout.VBox;

import java.util.*;

public class KeybindsGraphicUserInterface extends GraphicUserInterface {

    private static final double ROW_HEIGHT = 34.0;
    private static final double ROW_GAP = 6.0;
    private final GameScheduler scheduler;
    public static String defaultStatus = "Klicke einen Eintrag und drücke dann eine Taste zum Ändern.";
    private VBox scrollContent;
    private GraphicText statusText;

    private String awaitingAction = null;
    private Runnable refreshAction = () -> {};

    private KeybindsGraphicUserInterface(GraphicWindow window, GameScheduler scheduler) {
        super(window);
        this.scheduler = scheduler;
    }

    @SuppressWarnings("unused")
    public static KeybindsGraphicUserInterface create(GraphicWindow window, GameScheduler scheduler) {
        if (window == null) throw new IllegalArgumentException("window must not be null");
        KeyBinds.load();

        KeybindsGraphicUserInterface gui = new KeybindsGraphicUserInterface(window, scheduler);
        gui.size(460, 420)
                .title("Tastaturbelegung")
                .alwaysInFront()
                .position(Position.of(Alignment.CENTER));

        gui.scrollContent = new VBox(ROW_GAP);
        gui.scrollContent.setFillWidth(true);

        gui.statusText = gui.addItemAndGet(
                GraphicText.builder()
                        .text(defaultStatus)
                        .fontSize(13)
                        .color(Color.fromRGB(255, 255, 255)),
                Position.of(Alignment.TOP_LEFT).margin(0.05,0,0,0.05)
        );

        GraphicScrollPane scrollPane = gui.addItemAndGet(
                GraphicScrollPane.builder()
                        .content(gui.scrollContent)
                        .fitToWidth(true)
                        .fitToHeight(false)
                        .pannable(true)
                        .transparentBackground(true)
                        .size(gui.getWindowWidth() - 24, gui.getWindowHeight() - 120),
                Position.of(Alignment.TOP_LEFT).margin(0.125,0,0,0.05)
        );

        gui.addItemAndGet(
                GraphicButton.builder()
                        .text("Zurück")
                        .size(gui.getWindowWidth() - 24, 36)
                        .backgroundColor(Color.fromHex("#b33a3a"))
                        .hover(Color.fromHex("#d94a4a"))
                        .clickColor(Color.fromHex("#ff6666"))
                        .textColor(Color.fromRGB(255, 255, 255))
                        .onClick(gui::hide),
                Position.of(Alignment.BOTTOM_CENTER).offsetY(-10)
        );

        gui.refreshAction = () -> {
            gui.scrollContent.getChildren().clear();

            List<Map.Entry<String, Key>> entries =
                    new ArrayList<>(KeyBinds.getAll().entrySet());

            entries.sort((a, b) -> {
                boolean aC = KeyBinds.isChangeable(a.getKey());
                boolean bC = KeyBinds.isChangeable(b.getKey());

                if (aC != bC) return aC ? -1 : 1;
                return a.getKey().compareToIgnoreCase(b.getKey());
            });

            for (Map.Entry<String, Key> e : entries) {
                String action = e.getKey();
                Key key = e.getValue();

                boolean selected = action.equals(gui.awaitingAction);
                boolean changeable = KeyBinds.isChangeable(action);

                Color base = changeable ? Color.fromHex("#2f2f2f") : Color.fromHex("#1a1a1a");
                Color textColor = changeable ? Color.fromRGB(255,255,255) : Color.fromRGB(130,130,130);

                GraphicButton row = GraphicButton.builder()
                        .text(KeyBinds.getDisplayName(action) + " [" + key.getDisplayName() + "]")
                        .size(gui.getWindowWidth() - 70, ROW_HEIGHT)
                        .backgroundColor(selected ? Color.fromHex("#4a5f8a") : base)
                        .textColor(textColor)
                        .onClick(() -> {
                            if (!changeable) return;
                            gui.awaitingAction = action;
                            gui.updateStatus("Taste für \"" + KeyBinds.getDisplayName(action) + "\" drücken...");
                            gui.refresh();
                        })
                        .build(gui, Position.of(0, 0)); // Position egal in VBox

                gui.scrollContent.getChildren().add(row.getNode());
            }
        };

        gui.addListener(Key.ESCAPE, ActionType.PRESS, () -> {
            gui.awaitingAction = null;
            gui.updateStatus("Tasten Änderung Abgebrochen");
            gui.refresh();
        });

        gui.registerKeyListeners();
        gui.create();
        gui.refresh();

        return gui;
    }

    private void registerKeyListeners() {
        for (Key key : Key.values()) {
            if (key == null) continue;

            this.addListener(key, ActionType.PRESS, () -> {

                if (awaitingAction == null) return;

                String action = awaitingAction;
                awaitingAction = null;

                if (key == Key.ESCAPE) return;

                KeyBinds.set(action, key);

                updateStatus("\"" + KeyBinds.getDisplayName(action)
                        + "\" -> " + key.getDisplayName());
                scheduler.runLater(() -> updateStatus(defaultStatus),GameScheduler.debugIntervalTicks);
                refresh();
            });
        }
    }

    private void updateStatus(String text) {
        if (statusText != null) {
            statusText.text(text);
        }
    }

    public void refresh() {
        refreshAction.run();
    }

    @Override
    public GraphicUserInterface show() {
        refresh();
        return super.show();
    }
}