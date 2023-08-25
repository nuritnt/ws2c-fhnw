package telloflix.views;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
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
    private Text batteryLabel;
    private SimpleStringProperty batteryLevel = new SimpleStringProperty("0");
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


    public Button recordButton;
    private Canvas frameCanvas;

    private Slider forward_backward_slider;

    private Slider left_right_slider;
    private Text leftLabel;
    private Text rightLabel;
    private Slider up_down_slider;
    private Text upLabel;
    private Text downLabel;
    private Slider yaw_slider;
    private Text yawLabel;
    private Text forwardLabel;
    private Text backwardLabel;

    private Button resetSlidersButton;

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
        batteryLabel = new Text("Batterylevel: " + batteryLabel);
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
        resetSlidersButton = new Button("Reset Sliders");

        frameCanvas = new Canvas(480, 360);

        forward_backward_slider = new Slider(-100, 100, 0);
        forward_backward_slider.setOrientation(Orientation.VERTICAL);

        left_right_slider = new Slider(-100, 100, 0);
        left_right_slider.setOrientation(Orientation.HORIZONTAL);

        up_down_slider = new Slider(-100, 100, 0);
        up_down_slider.setOrientation(Orientation.VERTICAL);

        yaw_slider = new Slider(-100, 100, 0);
        yaw_slider.setOrientation(Orientation.HORIZONTAL);

        leftLabel = new Text("Left");
        rightLabel = new Text("Right");
        upLabel = new Text("Up");
        downLabel = new Text("Down");
        yawLabel = new Text("Yaw");
        forwardLabel = new Text("Forward");
        backwardLabel = new Text("Backward");

    }

    @Override
    public void layoutParts() {
        setHgap(20);
        setVgap(20);
        setValignment(emergencyButton, VPos.TOP);
        setPadding(new Insets(50));
        add(recordButton, 1, 0,2,1);

        add(startButton    , 3, 0);
        add(landButton     , 4, 0);
        add(emergencyButton, 5, 0);
        add(batteryLabel, 6, 0);

        add(flyUpButton    , 1, 1,2,1);
        add(yawLeftButton, 1,2);
        add(yawRightButton,2,2);
        add(flyDownButton,1,3,2,1);


        add(frameCanvas,     3, 1, 4, 3);

        add(flyForwardButton, 7, 1, 2, 1);
        add(flyBackwardButton, 7, 3, 2, 1);
        add(flyLeftButton, 7, 2);
        add(flyRightButton, 8, 2);

        add(flipLeftButton, 3, 4);
        add(flipRightButton, 6, 4);
        add(flipForwardButton, 4, 4);
        add(flipBackwardButton, 5, 4);

        add(resetSlidersButton, 4, 5);


        add(forward_backward_slider, 9, 1, 1, 3);
        add(left_right_slider, 6, 5, 2, 1);
        add(up_down_slider, 0, 1, 1, 3);
        add(yaw_slider, 2, 5, 2, 1);

        add(leftLabel, 5,5);
        add(rightLabel, 8,5);
        add(upLabel, 0,0);
        add(downLabel, 0,4);
        add(yawLabel, 1,5);
        add(forwardLabel, 9,0);
        add(backwardLabel, 9,4);


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
        recordButton.setOnAction(event -> {
            if (tello.videoStreamOn) {
                recordButton.setStyle("-fx-background-color: white");
                recordButton.setText("Record");

                tello.stopRecorder();
            } else {
                recordButton.setStyle("-fx-background-color: red");
                recordButton.setText("Recording...");
                tello.startRecorder();
            }
            //event.consume(); // Consume the event to prevent further propagation
        });
        resetSlidersButton.setOnAction(event -> {
            forward_backward_slider.setValue(0);
            left_right_slider.setValue(0);
            up_down_slider.setValue(0);
            yaw_slider.setValue(0);
        });
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
        forward_backward_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            tello.rcFrwd(newValue.intValue());
        });
        left_right_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            tello.rcLeftRight(newValue.intValue());
        });

        up_down_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            tello.rcUpDown(newValue.intValue());
        });

        yaw_slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            tello.rcYaw(newValue.intValue());
        });




    }

    @Override
    public void setupBindings() {
        ViewMixin.super.setupBindings();
        batteryLabel.textProperty().bind(batteryLevel);
        tello.batteryLevel.onChange((oldValue, newValue) -> {
            batteryLevel.setValue(newValue.toString());
        });
    }
}
