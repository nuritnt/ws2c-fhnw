package telloflix.views;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.bytedeco.javacv.JavaFXFrameConverter;
import telloflix.model.TelloFlix;
import telloflix.views.utils.ViewMixin;


public class TelloFlixUI extends GridPane implements ViewMixin {
    private final TelloFlix tello;

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
    private Button flipLeftButton;
    private Button flipRightButton;
    private Button flipForwardButton;
    private Button flipBackwardButton;
    private Button recordButton;

    private Canvas frameCanvas;

    private final JavaFXFrameConverter frameConverter = new JavaFXFrameConverter();

    /**
     * Jeder UI-Teil benoetigt die darzustellenden Informationen.
     *
     * @param tello das PresentationModel, das visualisiert und manipuliert werden soll
     */
    public TelloFlixUI(TelloFlix tello) {
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
        startButton     = new Button("Takeoff");
        landButton      = new Button("Land");
        flyUpButton     = new Button("Up");
        emergencyButton = new Button("Panic !");
        batteryLevel = new Text("Batterylevel: " + tello.getBatteryLevel());
        yawLeftButton = new Button("Yaw Left");
        yawRightButton = new Button("Yaw Right");
        flyDownButton = new Button("Down");
        flyForwardButton = new Button("Forward");
        flyBackwardButton = new Button("Backward");
        flyLeftButton = new Button("Left");
        flyRightButton = new Button("Right");
        flipLeftButton = new Button("Flip Left");
        flipRightButton = new Button("Flip Right");
        flipForwardButton = new Button("Flip Forward");
        flipBackwardButton = new Button("Flip Backward");
        recordButton = new Button("Record");

        frameCanvas = new Canvas(TelloFlix.VIDEO_WIDTH, TelloFlix.VIDEO_HEIGHT);
    }

    @Override
    public void layoutParts() {
        setHgap(20);
        setVgap(20);
        setValignment(emergencyButton, VPos.TOP);
        setPadding(new Insets(50));
        add(startButton    , 0, 0);
        add(flyUpButton    , 0, 1);
        add(landButton     , 0, 2);
        add(emergencyButton, 0, 3);

        add(frameCanvas,     2, 1, 4, 4);

        add(batteryLevel, 5, 0);
        add(yawLeftButton, 0,2);
        add(yawRightButton,1,2);
        add(flyDownButton,0,3,2,1);
        add(flyForwardButton,6,1,2,1);
        add(flyBackwardButton,6,3,2,1);
        add(flyLeftButton,6,2);
        add(flyRightButton,7,2);

        add(flipLeftButton, 0, 4);
        add(flipRightButton, 1, 4);
        add(flipForwardButton, 6, 4);
        add(flipBackwardButton, 7, 4);
        add(recordButton, 0, 0);
    }

    @Override
    public void setupEventHandlers() {
        startButton.setOnAction(event     -> tello.takeOff());  // das blockiert das UI. Kann das so bleiben?
        landButton.setOnAction(event      -> tello.land());
        flyUpButton.setOnAction(event     -> tello.up(50));
        emergencyButton.setOnAction(event -> tello.emergency());
        yawLeftButton.setOnAction(event   -> tello.ccw(45));
        yawRightButton.setOnAction(event  -> tello.cw(45));
        flyDownButton.setOnAction(event   -> tello.down(50));
        flyForwardButton.setOnAction(event-> tello.forward(50));
        flyBackwardButton.setOnAction(event-> tello.back(50));
        flyLeftButton.setOnAction(event   -> tello.left(50));
        flyRightButton.setOnAction(event  -> tello.right(50));
        flipLeftButton.setOnAction(event  -> tello.flip("l"));
        flipRightButton.setOnAction(event -> tello.flip("r"));
        flipForwardButton.setOnAction(event-> tello.flip("f"));
        flipBackwardButton.setOnAction(event-> tello.flip("b"));
        recordButton.setOnAction(event    -> tello.record());

    }

    @Override
    public void setupValueChangedListeners() {
        //hier wird direkt das API von ObservableValue genutzt. Das geht (offensichtlich)
        //Wie würde ein "besseres" API, das hier im View genutzt werden kann, aussehen?
        //Was ist die geeignete Klasse/Interface, die das bessere API bereitstellt?
        //todo: Entwerfen Sie das aus ihrer Sicht optimale API um auf Änderungen eines ObservableValue adäquat im View reagieren zu können
        tello.currentFrameValue().onChange(((oldValue, newValue) ->
                Platform.runLater(() -> {  //Änderungen im UI sollten immer im UI-Thread passieren
                    GraphicsContext ctx = frameCanvas.getGraphicsContext2D();

                    if (newValue == null || newValue.image == null) {
                        ctx.setFill(Color.PAPAYAWHIP);
                        ctx.fillRect(0, 0, frameCanvas.getWidth(), frameCanvas.getHeight());
                    } else {
                        Image image = frameConverter.convert(newValue);
                        ctx.drawImage(image, 0, 0, frameCanvas.getWidth(), frameCanvas.getHeight());
                    }
                })));
    }
}
