package telloflix.model;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleStringProperty;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import tello.models.util.ObservableValue;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static tello.DeepLearningFaceDetection.detectAndDraw;


/**
 * WICHTIG: Diese Klasse ist nur eine erste, rudimentäre Version für die Drohnen-Steuerung via Java.
 * <p>
 * Sie zeigt an einigen Beispielen wie Kommandos an die Tello-Drohne verschickt und
 * Status-Informationen empfangen werden können.
 * <p>
 * Auch viele der bestehenden Methoden sind nicht optimal. Der Name der Methode, die Parameter-Liste, die Implementierung
 * oder der Rückgabewert können noch verbessert werden.
 * <p>
 * Der Umbau, die Erweiterung und Anpassung dieser Klasse sind die Hauptaufgaben dieser Übung (neben dem UI).
 */
public class TelloFlix {
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    // das ist die IP-Adresse der "echten" Drohne (Hinweis: Ihr Laptop muss mit dem WLAN der Drohne verbunden sein)
    public static final String REAL_TELLO_IP_ADDRESS = "192.168.10.1";

    //todo: hier die in TelloCamp angezeigte IP-Adresse oder falls man mit der echten Drohne fliegen will 'REAL_TELLO_IP_ADDRESS' eintragen
    public static final String TELLO_IP_ADDRESS = "10.207.14.123";

    // ueber diesen Port werden die Kommandos verschickt
    //todo: überprüfen, ob das in TelloCamp auch so gesetzt ist
    private static final int COMMAND_PORT = 8889;

    // wird für den Video-Kanal benötigt
    private static final String LOCAL_IP_ADDRESS = "0.0.0.0";

    // die Ports über die Status-Meldungen und Video-Bilder ankommen
    //todo: überprüfen, ob das in TelloCamp auch so gesetzt ist
    private static final int STATE_PORT = 8890;
    public static final int VIDEO_PORT = 11111;

    public static final int VIDEO_WIDTH  = 1280;
    public static final int VIDEO_HEIGHT = 960;

    public InetAddress    telloAddress = null;
    private DatagramSocket commandSocket;

    private DatagramSocket statusSocket;

    private boolean connected = false;

    private final ObservableValue<Frame> currentFrame = new ObservableValue<>(null);

    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    public boolean videoStreamOn = false;

    private ExecutorService executor = Executors.newSingleThreadExecutor();



    public ObservableValue<String> batteryLevel = new ObservableValue<>("bat: 0%");
    private int fwd_bk;
    private int left_right;

    private int up_down;

    private int yaw;


    /**
     * Verbindung zur Drohne (oder TelloCamp) aufbauen
     *
     * @return true, falls der Verbindungsaufbau geklappt hat
     */
    public boolean connect() {
        try {
            telloAddress = InetAddress.getByName(REAL_TELLO_IP_ADDRESS);

            commandSocket = new DatagramSocket();
            commandSocket.connect(telloAddress, COMMAND_PORT);

            boolean response = sendCommandAndWait("command");
            connected = response;

            return response;
        } catch (UnknownHostException e) {
            LOGGER.severe("unknown Host");
            return false;
        } catch (SocketException e) {
            LOGGER.severe("cannot connect");
            return false;
        }
    }

    /**
     * Alle Verbindungen zur Drohne trennen und alle laufenden Threads beenden
     */
    public void disconnect() {
        connected = false; //this will terminate the "listenToStatus-Thread"
        if (commandSocket != null) {
            commandSocket.close();
        }
        if (statusSocket != null) {
            statusSocket.close();
        }
    }

    /**
     * Darf erst nach einem 'connect' aufgerufen werden
     */
    public void startStateListener(){
        try {
            statusSocket = new DatagramSocket(STATE_PORT);

            // der Status soll kontinuierlich empfangen und weiterverarbeitet werden.
            // am einfachsten erreicht man das mit einem separaten Thread
            Thread statusThread = new Thread(this::listenToState);
            statusThread.setDaemon(true);
            statusThread.start();
        } catch (SocketException e) {
            LOGGER.severe("cannot connect to status port");
        }
    }

    /**
     * Must be called after 'startStatusListener'
     */
    public void startVideoListener() {
        sendCommandAndWait("streamon");

        new Thread(() -> {
            String videoAddress = "udp://" + LOCAL_IP_ADDRESS + ":" + VIDEO_PORT;
            grabber = new FFmpegFrameGrabber(videoAddress);
            grabber.setImageMode(FrameGrabber.ImageMode.COLOR);
            grabber.setFormat("h264");
            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            grabber.setImageWidth(VIDEO_WIDTH);
            grabber.setImageHeight(VIDEO_HEIGHT);

            //recorder = new FFmpegFrameRecorder("recorded_" + timeNow +".mp4", grabber.getImageWidth(), grabber.getImageHeight(), 0);
            //recorder.setFormat("mp4");
            //recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

            try {
                grabber.start();
                recorder.start();
            } catch (Exception e) {
                LOGGER.severe("can't start FrameGrabber " + e.getMessage());
            }

            Thread videoThread = new Thread(this::listenToVideo);
            videoThread.setDaemon(true);
            videoThread.start();
        }).start();

    }

    /**
     * Stop motors immediately.
     */
    public void emergency() {
        sendFireAndForgetCommand("emergency");
    }

    /**
     * Auto takeoff.
     *
     */
    public void takeOff() {
        sendCommandAsync("takeoff");
    }

    /**
     * Auto landing.
     *
     */
    public void land() {
        sendCommandAsync("land");
    }

    /**
     * Fly up.
     *
     * @param z distance in cm
     */
    public void up(int z) {
        sendCommandAsync("up " + z);
    }



    /**
     * Set speed to “x” cm/s. x = 10-100
     *
     * @param speed
     */
    public void setSpeed(int speed) {
         sendFireAndForgetCommand("speed " + assureRange(speed, 10, 100));
    }

    /**
     * Obtain current battery percentage.
     *
     * @return battery level 0-100
     */
    public int getBatteryLevel(){
        try {
            return Integer.parseInt(sendReadCommand("battery?"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Wait some time before you send next command.
     *
     * @param duration
     */
    public void delay(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void down(int z) {
        sendCommandAsync("down " + z);
    }
    public void ccw(int z) {
        sendCommandAsync("ccw " + z);
    }
    public void cw(int z) {
        sendCommandAsync("cw " + z);
    }
    public void left(int z) {
        sendCommandAsync("left " + z);
    }
    public void right(int z) {
        sendCommandAsync("right " + z);
    }
    public void forward(int z) {
        sendCommandAsync("forward " + z);
    }
    public void back(int z) {
        sendCommandAsync("back " + z);
    }
    public void flip(String direction) {
        sendCommandAsync("flip " + direction);
    }



    // todo: Hier fehlen noch viele notwendige Methoden zur Drohnen-Steuerung, z.B. 'down', 'left', 'right'
    // besonders wichtig: 'rc'
    // oder: Verarbeitung des Video-Signals

    // die nachfolgenden Hilfsmethoden sollten 'private' bleiben.

    /**
     * Send control command to Tello and wait for its response. This is necessary for all commands that need some time to finish
     * <p>
     * Possible control commands:
     * - command:        entry SDK mode
     * - takeoff:        Tello auto takeoff
     * - land:           Tello auto land
     * - up x:           Tello fly up with distance x cm. x: 20-500
     * - down x:         Tello fly down with distance x cm. x: 20-500
     * - left x:         Tello fly left with distance x cm. x: 20-500
     * - right x:        Tello fly right with distance x cm. x: 20-500
     * - forward x:      Tello fly forward with distance x cm. x: 20-500
     * - back x:         Tello fly back with distance x cm. x: 20-500
     * - cw x:           Tello rotate x degree clockwise x: 1-3600
     * - ccw x:          Tello rotate x degree counter-clockwise. x: 1-3600
     * - flip x:         Tello fly flip x l (left) r (right) f (forward) b (back)
     * - go x y z speed: Tello fly to x y z in speed (cm/s)
     *     x: 20-500
     *     y: 20-500
     *     z: 20-500
     *     speed: 10-100
     * <p>
     * - curve x1 y1 z1 x2 y2 z2 speed: Tello fly a curve defined by the current and two given
     *   coordinates with speed (cm/s).
     *   If the arc radius is not within the range of 0.5-10 meters, Tello responses false.
     *   x/y/z can’t be between -20 – 20 at the same time .
     *     x1, x2: 20-500 y1, y2: 20-500
     *     z1, z2: 20-500
     *     speed: 10-60
     * <p>
     * @param command this command will be sent to Tello
     * @return true if command was successful otherwise false
     */
    private boolean sendCommandAndWait(final String command) {
        sendFireAndForgetCommand(command);

        final String response = getResponse().trim();

        return "ok".equals(response);
    }

    private void sendCommandAsync(final String command) {
        executor.execute(() -> sendCommandAndWait(command));
    }

    /**
     * Send command to Tello without expecting a response.
     * Use this method when you want to send a command continuously
     * <p>
     * - rc a b c d: Send RC control via four channels.
     *   a: left/right (-100~100)
     *   b: forward/backward (-100~100)
     *   c: up/down (-100~100)
     *   d: yaw (-100~100)
     * <p>
     * - streamon:       Set video stream on
     * - streamoff:      Set video stream off
     * - emergency:      Stop all motors immediately
     * - stop:           Hovers in the air. Works at any time.
     * - speed x:        Set speed to x cm/s. x: 10-100
     * - wifi ssid pass: Set Wi-Fi with SSID password
     *
     * @param command  this command will be sent to Tello
     */
    private void sendFireAndForgetCommand(String command) {
        try {
            final byte[]         sendData   = command.getBytes();
            final DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, telloAddress, COMMAND_PORT);
            commandSocket.send(sendPacket);
            LOGGER.info("Command : " + command);
        } catch (IOException e) {
            LOGGER.severe("can't send command : " + command);
        }
    }

    /**
     * Send read command to Tello and wait for its response. Possible read commands:
     *   - speed?:    obtain current speed (cm/s): x: 1-100
     *   - battery?:  obtain current battery percentage: x: 0-100
     *   - time?:     obtain current flight time (s): time
     *   - wifi?:     obtain Wi-Fi SNR: snr
     *   - sdk?:      obtain the Tello SDK version
     *   - sn?:       get obtain the Tello serial number
     *
     * Please notice: In most cases it's more appropriate to use the status listener
     *
     * @param command the read command to be sent
     * @return the value as String
     */
    private String sendReadCommand(String command) {
        sendFireAndForgetCommand(command);
        return getResponse();
    }


    /**
     * Wartet auf die Antwort der Drohne auf ein ihr geschicktes Kommando.
     * <p>
     * Achtung: Auf manche Kommandos wie 'rc' schickt die Drohne keine Response.
     *
     * @return the drone's response to a command
     */
    private String getResponse() {
        try {
            byte[]               receiveData   = new byte[1024];
            final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            commandSocket.receive(receivePacket);
            String received = trimResponse(receivePacket);
            LOGGER.info("Received : " + received);

            return received;
        } catch (Exception e) {
            LOGGER.severe("can't receive data " + e.getLocalizedMessage());
            return "error";
        }
    }

    /**
     * Nimmt die von der Drohne geschickten Status-Meldungen entgegen und verarbeitet sie.
     * <p>
     * Aktuell wird der Status einfach nur auf die Konsole geloggt.
     */
    private void listenToState() {
        byte[]               receiveData   = new byte[1024];
        final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        while (connected) {
            try {
                statusSocket.receive(receivePacket);
                String received = trimResponse(receivePacket);
                // todo: hier den empfangenen String so weiterverarbeiten, dass die Informationen anschliessend z.B. im UI angezeigt werden koennen
                Arrays.stream(received.split(";")).anyMatch(s -> {
                    if (s.startsWith("bat")) {
                        batteryLevel.setValue("bat: "+ s.substring(4) + "%");
                        return true;
                    }
                    return false;
                });

                //LOGGER.info("state : " + received);
            } catch (Exception e) {
                LOGGER.severe("can't receive state " + e.getLocalizedMessage());
            }
        }
    }

    private void listenToVideo() {
        int counter = 0;
        while (connected) {
            try {
                Frame frame = grabber.grabImage();
                //hier frame verarbeiten



                if (frame.image != null ) {
                    Frame clone = frame.clone();
                    if(counter == 15){
                        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
                        Mat colorimg = converter.convert(clone);

                        detectAndDraw(colorimg);
                        currentFrame.setValue(converter.convert(colorimg));
                        counter = 0;
                    }
                    else {
                        currentFrame.setValue(clone);
                        counter++;
                    }


                   //
                   // currentFrame.setValue(clone);
                    if(videoStreamOn){
                        recorder.record(clone);
                    }
                }
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
            }
        }
        try {
            recorder.stop();
            grabber.stop();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private String trimResponse(DatagramPacket receivePacket) {
        byte[] response = Arrays.copyOf(receivePacket.getData(), receivePacket.getLength());
        return new String(response, StandardCharsets.UTF_8).trim();
    }

    private int assureRange(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    public ObservableValue<Frame> currentFrameValue() {
        return currentFrame;
    }

    public void stopRecorder() {
            try {
                videoStreamOn = false;
                recorder.stop();
                recorder.release();
            } catch (FFmpegFrameRecorder.Exception e) {
                throw new RuntimeException(e);
            }
    }

    public void startRecorder(){

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String formattedDateTime = now.format(formatter);

        recorder = new FFmpegFrameRecorder("recorded_" + formattedDateTime +".mp4", grabber.getImageWidth(), grabber.getImageHeight(), 0);
        recorder.setFormat("mp4");
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

        videoStreamOn = true;


        try {
             recorder.start();
        } catch (Exception e) {
            LOGGER.severe("can't start FrameGrabber " + e.getMessage());
        }
    }

    public void rcFrwd(int newValue) {
        fwd_bk = newValue;
        sendFireAndForgetCommand("rc " + left_right + " " + fwd_bk + " " +  up_down + " " + yaw);
    }

    public void rcLeftRight(int newValue) {
        left_right = newValue;
        sendFireAndForgetCommand("rc " + left_right + " " + fwd_bk + " " +  up_down + " " + yaw);
    }

    public void rcUpDown(int newValue) {
        up_down = newValue;
        sendFireAndForgetCommand("rc " + left_right + " " + fwd_bk + " " + up_down + " " + yaw);
    }

    public void rcYaw(int newValue) {
        yaw = newValue;
        sendFireAndForgetCommand("rc " + left_right + " " + fwd_bk + " " + up_down + " " + yaw);
    }
}
