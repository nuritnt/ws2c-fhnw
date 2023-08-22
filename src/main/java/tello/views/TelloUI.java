package tello.views;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import javafx.scene.text.Text;
import tello.models.Tello;
import tello.views.util.ViewMixin;


public class TelloUI extends GridPane implements ViewMixin {
    private final Tello tello;

    //Deklaration der benötigten UI-Elemente
    private Button startButton;
    private Button landButton;
    private Button flyUpButton;
    private Button emergencyButton;
    private Text batteryLevel;

    /**
     * Jeder UI-Teil benoetigt die darzustellenden Informationen.
     *
     * @param tello das PresentationModel, das visualisiert und manipuliert werden soll
     */
    public TelloUI(Tello tello) {
        this.tello = tello;

        init();
    }

    @Override
    public void initializeSelf() {
        loadFonts("/fonts/Lato/Lato-Lig.ttf");
        addStylesheetFiles("style.css");
    }

    @Override
    public void initializeParts() {
        startButton = new Button("Takeoff");
        landButton = new Button("Land");
        flyUpButton = new Button("Up");
        emergencyButton = new Button("Panic !");
        batteryLevel = new Text("Batterylevel: " + tello.getBatteryLevel());
    }

    @Override
    public void layoutParts() {
        setHgap(20);
        setVgap(20);
        setPadding(new Insets(50));
        add(startButton, 0, 0);
        add(flyUpButton, 0, 1);
        add(landButton, 0, 2);
        add(emergencyButton, 0, 3);
        add(batteryLevel, 0, 4);
    }

    @Override
    public void setupEventHandlers() {
        startButton.setOnAction(event -> tello.takeOff());  // das blockiert das UI. Kann das so bleiben?
        landButton.setOnAction(event -> tello.land());
        flyUpButton.setOnAction(event -> tello.up(50));

        emergencyButton.setOnAction(event -> tello.emergency());
    }
}
