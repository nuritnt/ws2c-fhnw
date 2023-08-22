package tello;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


import tello.models.Tello;


public class TelloFlightPlan {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) {
        LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.INFO);

        Tello tello = new Tello();
        tello.connect();                        // Verbindung zur Drohne aufbauen

        if (tello.getBatteryLevel() < 0) {     // TelloCamp liefert im Moment immer 0. todo: Bei der echten Drohne hier einen realistischen Wert eingetragen
            LOGGER.info("Tello can't start. Battery level too low.");
        } else {
            tello.startStateListener();        // aktuellen Status von der Drohne schicken lassen
            tello.takeOff();
            tello.delay(Duration.ofSeconds(2)); // zwei Sekunde schweben lassen
            tello.setSpeed(50);                 // 50 cm/sec
            tello.up(50);                       // das sollte 1 Sekunde dauern
            tello.delay(Duration.ofSeconds(2));
            tello.land();                       // wieder landen
        }

        tello.disconnect();
    }
}
