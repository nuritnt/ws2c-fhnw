package tello;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_dnn.*;
import org.bytedeco.opencv.opencv_videoio.*;

import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_dnn.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_videoio.*;

/**
 * Created on Jul 28, 2018
 *
 * @author Taha Emara
 * Email : taha@emaraic.com
 *
 * This example does face detection using deep learning model which provides a
 * great accuracy compared to OpenCV face detection using Haar cascades.
 *
 * This example is based on this code
 * https://github.com/opencv/opencv/blob/master/modules/dnn/misc/face_detector_accuracy.py
 *
 * To run this example you need two files: deploy.prototxt can be downloaded
 * from
 * https://github.com/opencv/opencv/blob/master/samples/dnn/face_detector/deploy.prototxt
 *
 * and res10_300x300_ssd_iter_140000.caffemodel
 * https://github.com/opencv/opencv_3rdparty/blob/dnn_samples_face_detector_20170830/res10_300x300_ssd_iter_140000.caffemodel
 *
 */
public class DeepLearningFaceDetection {

    //saves architecture / configuration of the DeepLearning model, specifies how the network is designed to take image as an input
    private static final String PROTO_FILE = "src/main/java/tello/deploy.prototxt";
    //contains the learned parameters of model (weight and biases)
    private static final String CAFFE_MODEL_FILE = "src/main/java/tello/res10_300x300_ssd_iter_140000.caffemodel";
    private static final OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
    private static Net net = null;

    static {
        net = readNetFromCaffe(PROTO_FILE, CAFFE_MODEL_FILE);
    }

    public static List<Rect> detect(Mat image) {//detect faces and draw a blue rectangle around each face
//resized to (300x300) to match network
        resize(image, image, new Size(300, 300));//resize the image to match the input size of the model

        //create a 4-dimensional blob from image with NCHW (Number of images in the batch -for training only-, Channel, Height, Width) dimensions order,
        //for more detailes read the official docs at https://docs.opencv.org/trunk/d6/d0f/group__dnn.html#gabd0e76da3c6ad15c08b01ef21ad55dd8
        // input for model, pre-processed image
        Mat blob = blobFromImage(image, 1.0, new Size(300, 300), new Scalar(104.0, 177.0, 123.0, 0), false, false, CV_32F);

        net.setInput(blob);//set the input to network model
        Mat output = net.forward();//feed forward the input to the network to get the output matrix

        List<Rect> allRects = new ArrayList<>();

        Mat ne = new Mat(new Size(output.size(3), output.size(2)), CV_32F, output.ptr(0, 0));//extract a 2d matrix for 4d output matrix with form of (number of detections x 7)

        FloatIndexer srcIndexer = ne.createIndexer(); // create indexer to access elements of the matric

        for (int i = 0; i < output.size(3); i++) {//iterate to extract elements
            float confidence = srcIndexer.get(i, 2);
            float f1 = srcIndexer.get(i, 3);
            float f2 = srcIndexer.get(i, 4);
            float f3 = srcIndexer.get(i, 5);
            float f4 = srcIndexer.get(i, 6);
            if (confidence > .6) {
                float tx = f1 * 300;//top left point's x
                float ty = f2 * 300;//top left point's y
                float bx = f3 * 300;//bottom right point's x
                float by = f4 * 300;//bottom right point's y
                allRects.add(new Rect(new Point((int) tx, (int) ty),
                                      new Point((int) bx, (int) by)));
            }
        }

        return allRects;
        }


    public static void draw (Mat image, List<Rect> allRects){
        for(Rect r : allRects){
            rectangle(image, r,
                    new Scalar(0 , 255, 0, 0),
                    3, 0, 0);//print green rectangle
        }
    }

    //public static void main(String[] args) {
    //    VideoCapture capture = new VideoCapture();
    //    capture.set(CAP_PROP_FRAME_WIDTH, 1280);
    //    capture.set(CAP_PROP_FRAME_HEIGHT, 720);
//
    //    if (!capture.open(0)) {
    //        System.out.println("Can not open the file !!!");
    //    }
    //    // stores every each frame from the camera
    //    Mat colorimg = new Mat();
    //    //display video feed and detected face
    //    CanvasFrame mainframe = new CanvasFrame("Face Detection", CanvasFrame.getDefaultGamma() / 2.2);
    //    mainframe.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    //    mainframe.setCanvasSize(600, 600);
    //    mainframe.setLocationRelativeTo(null);
    //    mainframe.setVisible(true);
//
    //    while (true) {
    //        while (capture.read(colorimg) && mainframe.isVisible()) {
    //            Mat detectedImage = detect(colorimg);
    //            draw(colorimg, detectedImage);
    //            mainframe.showImage(converter.convert(colorimg));
    //            try {
    //                Thread.sleep(50); //delay to control frame rate of video
    //            } catch (InterruptedException ex) {
    //                System.out.println(ex.getMessage());
    //            }
//
    //        }
    //    }
    //}

}
