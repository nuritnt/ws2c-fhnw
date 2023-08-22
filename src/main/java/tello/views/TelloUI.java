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
    private Button yawLeftButton;
    private Button yawRightButton;

    private Button flyDownButton;
    private Button flyForwardButton;
    private Button flyBackwardButton;
    private Button flyLeftButton;
    private Button flyRightButton;



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
        yawLeftButton = new Button("Yaw Left");
        yawRightButton = new Button("Yaw Right");
        flyDownButton = new Button("Down");
        flyForwardButton = new Button("Forward");
        flyBackwardButton = new Button("Backward");
        flyLeftButton = new Button("Left");
        flyRightButton = new Button("Right");

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
        add(yawLeftButton, 1,0);
        add(yawRightButton,1,1);
        add(flyDownButton,1,2);
        add(flyForwardButton,1,3);
        add(flyBackwardButton,1,4);
        add(flyLeftButton,1,5);
        add(flyRightButton,1,6);

    }

    @Override
    public void setupEventHandlers() {
        startButton.setOnAction(event -> tello.takeOff());  // das blockiert das UI. Kann das so bleiben?
        landButton.setOnAction(event -> tello.land());
        flyUpButton.setOnAction(event -> tello.up(50));
        yawLeftButton.setOnAction(event -> tello.ccw(45));
        yawRightButton.setOnAction(event -> tello.cw(45));
        flyDownButton.setOnAction(event -> tello.down(50));
        flyForwardButton.setOnAction(event -> tello.forward(50));
        flyBackwardButton.setOnAction(event -> tello.back(50));
        flyLeftButton.setOnAction(event -> tello.left(50));
        flyRightButton.setOnAction(event -> tello.right(50));

        emergencyButton.setOnAction(event -> tello.emergency());
    }
}
