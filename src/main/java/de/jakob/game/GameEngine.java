package de.jakob.game;

import de.jakob.game.gui.GraphicWindow;
import de.jakob.game.gui.generic.ExitGraphicUserInterface;
import de.jakob.game.path.Directories;
import de.jakob.game.scheduler.GameScheduler;
import javafx.application.Application;
import javafx.stage.Stage;

public abstract class GameEngine extends Application {

    private GameScheduler scheduler;

    @Override
    public void start(Stage stage) {
        scheduler = new GameScheduler();
        scheduler.start();

        Directories.init(getResourceRootPath());

        GraphicWindow window = new GraphicWindow(stage, getName(), GraphicWindow.Size.FULLSCREEN);
        onStart(window, scheduler);
        window.exitGUI(ExitGraphicUserInterface.create(window, scheduler));
        scheduler.runRepeating(this::tickLoop, 0, 1);
        scheduler.runRepeating(this::secondLoop, 0, GameScheduler.targetedTPS);
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