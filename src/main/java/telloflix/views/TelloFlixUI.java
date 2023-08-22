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
import org.bytedeco.javacv.JavaFXFrameConverter;
import telloflix.model.TelloFlix;
import telloflix.views.utils.ViewMixin;


public class TelloFlixUI extends GridPane implements ViewMixin {
    private final TelloFlix tello;

    private Button startButton;
    private Button landButton;
    private Button flyUpButton;
    private Button emergencyButton;

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

        add(frameCanvas,     1, 0, 1, 4);
    }

    @Override
    public void setupEventHandlers() {
        startButton.setOnAction(event     -> tello.takeOff());  // das blockiert das UI. Kann das so bleiben?
        landButton.setOnAction(event      -> tello.land());
        flyUpButton.setOnAction(event     -> tello.up(50));
        emergencyButton.setOnAction(event -> tello.emergency());
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
