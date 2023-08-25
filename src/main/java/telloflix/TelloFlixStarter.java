package telloflix;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import telloflix.model.TelloFlix;
import telloflix.views.TelloFlixUI;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * In diesem Beispiel wird zusätzlich zur TelloApp der VideoStream der Drohne angezeigt.
 * <p>
 * BITTE BEACHTEN:
 * <p>
 * Hier ist es offensichtlich wie sich ein UI-Freeze auswirkt: Es wird erst wieder ein neues Videobild angezeigt,
 * sobald die Kommandoverarbeitung beendet ist.
 * <p>
 * Sobald man die Kommandos asynchron absetzt, werden alle gesendeten Video-Frames angezeigt.
 * <p>
 * Zur Verarbeitung des Video-Streams wird die Library 'JavaCV' verwendet. Diese Library bietet weitere Funktionen
 * wie z.B. eine Gesichtserkennung oder eine Hinderniserkennung.
 * Es ist sehr empfehlenswert ausschliesslich die "offiziellen" Beispiele von JavaCV als Startpunkt zu verwenden
 * <a href="https://github.com/bytedeco/javacv/tree/master/samples">JavaCV Examples</a>
 */
public class TelloFlixStarter extends Application {

    private TelloFlix tello;

    @Override
    public void init() throws Exception {
        super.init();

        //das PresentationModel, in diesem Fall die Klasse zur Verbindung mit der Drohne
        tello = new TelloFlix();
        tello.connect();
        tello.startStateListener();
        tello.startVideoListener();
    }

    @Override
    public void start(Stage primaryStage) {
        Parent rootPanel = new TelloFlixUI(tello);

        Scene scene = new Scene(rootPanel);

        primaryStage.setTitle("Let it fly");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);


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
