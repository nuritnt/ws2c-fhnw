package tello;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import tello.models.Tello;
import tello.views.TelloUI;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Der Aufbau dieser JavaFX-Applikation entspricht der in OOP2 vorgestellten Grundstruktur.
 * <p>
 * Es ist eine Anwendung des PresentationModel-Konzepts.
 */
public class TelloAppStarter extends Application {

    private Tello tello;

    @Override
    public void init() throws Exception {
        super.init();

        //das PresentationModel, in diesem Fall die Klasse zur Verbindung mit der Drohne
        tello = new Tello();
        tello.connect();
    }

    @Override
    public void start(Stage primaryStage) {
        Parent rootPanel = new TelloUI(tello);

        Scene scene = new Scene(rootPanel);

        primaryStage.setTitle("Let it fly");
        primaryStage.setScene(scene);

        primaryStage.setResizable(false);

        primaryStage.show();
    }

    @Override
    public void stop() {
        tello.disconnect();
    }

    public static void main(String[] args) {
        LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.INFO);

        launch(args);
    }
}
