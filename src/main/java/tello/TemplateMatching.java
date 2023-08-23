package tello;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_highgui.cvShowImage;
import static org.bytedeco.opencv.global.opencv_highgui.cvWaitKey;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * Example of template javacv (opencv) template matching using the last java build
 *
 * We need 2 default parameters like this (source image, image to find )
 * "C:\Users\Waldema\Desktop\bg.jpg" "C:\Users\Waldema\Desktop\imageToFind.jpg"
 *
 * @author Waldemar Neto
 */
public class TemplateMatching {

    public static void main(String[] args) throws Exception {

        newStyle(args);
        //oldStyle(args);

    }


    public static void newStyle(String[] args) throws Exception {
        // Load the template images as grayscale
        List<Mat> templates = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            templates.add(imread(args[i], IMREAD_GRAYSCALE));
        }

        // Open the webcam
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0); // 0 represents the default camera
        grabber.start();

        CanvasFrame sourceFrame = new CanvasFrame("Template Matching"); // Create a window for displaying results
        sourceFrame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);

        // Initialize the converter
        OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();

        // Loop to continuously capture frames and perform template matching
        while (sourceFrame.isVisible()) {
            Frame webcamFrame = grabber.grab(); // Capture a frame from the webcam
            if (webcamFrame == null) {
                break;
            }

            // Convert the grabbed Frame to Mat format
            Mat sourceColor = convertFrameToMat(webcamFrame);

            // Convert sourceColor to grayscale
            Mat sourceGrey = new Mat();
            cvtColor(sourceColor, sourceGrey, COLOR_BGR2GRAY);

            // Loop through each template and perform template matching
            for (Mat template : templates) {
                Mat result = new Mat();
                matchTemplate(sourceGrey, template, result, TM_CCOEFF_NORMED);

                // Find the best match location
                DoublePointer minVal = new DoublePointer();
                DoublePointer maxVal = new DoublePointer();
                Point min = new Point();
                Point max = new Point();
                minMaxLoc(result, minVal, maxVal, min, max, null);

                // Draw a rectangle around the matched area
                rectangle(sourceColor, new Rect(max.x(), max.y(), template.cols(), template.rows()), Scalar.RED, 2, 0, 0);
            }

            // Display the processed frame with the rectangles
            sourceFrame.showImage(matConverter.convert(sourceColor));
        }

        // Release resources
        grabber.stop();
        sourceFrame.dispose();
    }


    // Convert Frame to Mat
    public static Mat convertFrameToMat(Frame frame) {
        Java2DFrameConverter converter = new Java2DFrameConverter();
        BufferedImage bufferedImage = converter.getBufferedImage(frame);
        OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
        return matConverter.convertToMat(converter.convert(bufferedImage));
    }


    // some usefull things.
    public static Scalar randColor(){
        int b,g,r;
        b= ThreadLocalRandom.current().nextInt(0, 255 + 1);
        g= ThreadLocalRandom.current().nextInt(0, 255 + 1);
        r= ThreadLocalRandom.current().nextInt(0, 255 + 1);
        return new Scalar (b,g,r,0);
    }

    public static List<Point> getPointsFromMatAboveThreshold(Mat m, float t){
        List<Point> matches = new ArrayList<Point>();
        FloatIndexer indexer = m.createIndexer();
        for (int y = 0; y < m.rows(); y++) {
            for (int x = 0; x < m.cols(); x++) {
                if (indexer.get(y,x)>t) {
                    System.out.println("(" + x + "," + y +") = "+ indexer.get(y,x));
                    matches.add(new Point(x, y));
                }
            }
        }
        return matches;
    }

    public static void oldStyle(String[] args){
        //get color source image to draw red rect on later
        IplImage srcColor = cvLoadImage(args[0]);
        //create blank 1 channel image same size as the source
        IplImage src = cvCreateImage(cvGetSize(srcColor), IPL_DEPTH_8U, 1);
        //convert source to grey and copy to src
        cvCvtColor(srcColor, src, CV_BGR2GRAY);
        //get the image to match loaded in greyscale.
        IplImage tmp = cvLoadImage(args[1], 0);
        //this image will hold the strength of the match
        //as the template is translated across the image
        IplImage result = cvCreateImage(
                cvSize(src.width() - tmp.width() + 1,
                        src.height() - tmp.height() + 1), IPL_DEPTH_32F, src.nChannels());

        cvZero(result);

        // Match Template Function from OpenCV
        cvMatchTemplate(src, tmp, result, CV_TM_CCORR_NORMED);

        // double[] min_val = new double[2];
        // double[] max_val = new double[2];
        DoublePointer min_val = new DoublePointer();
        DoublePointer max_val = new DoublePointer();

        CvPoint minLoc = new CvPoint();
        CvPoint maxLoc = new CvPoint();

        cvMinMaxLoc(result, min_val, max_val, minLoc, maxLoc, null);

        // Get the Max or Min Correlation Value
        // System.out.println(Arrays.toString(min_val));
        // System.out.println(Arrays.toString(max_val));

        CvPoint point = new CvPoint();
        point.x(maxLoc.x() + tmp.width());
        point.y(maxLoc.y() + tmp.height());
        // cvMinMaxLoc(src, min_val, max_val,0,0,result);

        cvRectangle(srcColor, maxLoc, point, CvScalar.RED, 2, 8, 0); // Draw a
        // Rectangle for
        // Matched
        // Region

        cvShowImage("Lena Image", srcColor);
        cvWaitKey(0);
        cvReleaseImage(srcColor);
        cvReleaseImage(src);
        cvReleaseImage(tmp);
        cvReleaseImage(result);
    }
}
