package de.jakob.game;

import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.generic.DebugGraphicUserInterface;
import de.jakob.game.gui.generic.ExitGraphicUserInterface;
import de.jakob.game.file.Directories;
import de.jakob.game.input.KeyBinds;
import de.jakob.game.logger.Logger;
import de.jakob.game.scheduler.GameScheduler;
import javafx.application.Application;
import javafx.stage.Stage;

public abstract class GameEngine extends Application {

    private GameScheduler scheduler;

    @Override
    public void start(Stage stage) {
        Logger.info("Engine is starting...");
        scheduler = new GameScheduler();
        scheduler.start();

        Directories.init(getResourceRootPath());
        KeyBinds.load();
        GraphicWindow window = new GraphicWindow(stage, getName(), GraphicWindow.Size.FULLSCREEN);
        window.main().exitGUI(ExitGraphicUserInterface.create(window, scheduler).show());
        window.main().debugGUI(DebugGraphicUserInterface.create(window, scheduler).show());

        onStart(window, scheduler);
        scheduler.runRepeating(this::tickLoop, 0, 1);
        scheduler.runRepeating(this::secondLoop, 0, GameScheduler.targetedTPS);
        Logger.info("Engine started!");
    }

    public abstract void onStart(GraphicWindow window, GameScheduler scheduler);

    public void tickLoop() {

    }

    public void secondLoop() {

    }

    public String getResourceRootPath() {
        return System.getProperty("user.dir");
    }

    public String getName() {
        return "Spiel";
    }
}