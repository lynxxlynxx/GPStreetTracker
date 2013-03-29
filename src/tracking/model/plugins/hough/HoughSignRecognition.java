package tracking.model.plugins.hough;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.CV_FONT_HERSHEY_SIMPLEX;
import static com.googlecode.javacv.cpp.opencv_core.cvCloneImage;
import static com.googlecode.javacv.cpp.opencv_core.cvInitFont;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvPutText;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_CCOMP;
import static com.googlecode.javacv.cpp.opencv_imgproc.blur;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;

import java.util.LinkedList;

import javax.swing.JDialog;

import tracking.gui.MainFrame;
import tracking.gui.plugins.hough.colorfilter.ColorFilterPanel;
import tracking.gui.plugins.hough.colorfilter.HoughEditorFrame;
import tracking.model.plugins.ObjectDetectionPlugin;
import tracking.model.plugins.ResultObject;
import tracking.model.plugins.StreetObject;
import tracking.model.plugins.StreetObject.Type;
import tracking.model.plugins.hough.colorfilter.ColorFilter;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvFont;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;

/**
 * This class uses the {@link CircleDetection} and the HoughRectangular class to recognize shapes in
 * the video that could be traffic signs and saves them into TrafficSign Objects.
 * 
 * @author Philipp
 */
public class HoughSignRecognition implements ObjectDetectionPlugin {

    /**
     * An ID {@link Integer} to identify the plugin.
     */
    private int pluginID = -1;

    /**
     * The name of the plugin as {@link String}.
     */
    private static final String NAME = "Hough Sign Recognition";

    /**
     * <code>true</code> if the plugin comes with an editor frame.
     */
    private static final boolean IS_EDITABLE = true;

    /**
     * {@link LinkedList} of {@link CvScalar}s.
     */
    private LinkedList<CvScalar> highScalars;

    /**
     * {@link LinkedList} of {@link CvScalar}s.
     */
    private LinkedList<CvScalar> lowScalars;

    /**
     * {@link ColorFilter}
     */
    private ColorFilter cFilter;

    /**
     * The {@link MainFrame} to check if the video is set yet and to get the video file.
     */
    private MainFrame frame;

    /**
     * {@link SignTracker} to track the signs
     */
    private SignTracker signTracking;

    // Set the default values here.
    /**
     * Blur radius for blob detection aka cvContour. X and y value.
     */
    private int[] cvContourValue = { 15, 15 };

    /**
     * Min/Max Values for blob detection aka cvContour. X and y value.
     */
    private int[][] cvContourMinMax = { { 0, 30 }, { 0, 30 } };
    /**
     * Blur radius for circle detection. X and y value.
     */
    private int[] trackCircleBlurValue = { 7, 7 };

    /**
     * Min/Max blur radius for circle detection. X and y value.
     */
    private int[][] trackCircleBlurMinMax = { { 0, 30 }, { 0, 30 } };

    /**
     * Parameters for circle detection aka cvHoughCircle.
     * <code> int dp, int minDist, int param1, int param2, int minRadius, int maxRadius </code>.
     * 
     * @param dp
     *            : inverse of the ratio of the accumulator resolution. If dp = 1 accumulator has
     *            same resolution as the input image, if dp=2 accumulator has half the resolution of
     *            the input image. ...
     * @param minDist
     *            : minimum distance between the centers of the detected circles.
     * @param param1
     *            : Higher threshold for the canny edge detector, the lower one is twice smaller.
     * @param param2
     *            : it is the accumulator threshold for the circle centers at the detection stage.
     *            The smaller it is, the more false circles may be detected. Circles, corresponding
     *            to the larger accumulator values, will be returned first.
     * @param minRadius
     *            : minumum radius of the circle.
     * @param maxRadius
     *            : maximum radius of the circle.
     */
    private int[] trackCircleValue = { 2, 50, 200, 70, 0, 50 };

    /**
     * Min/Max values for cvHoughCircle
     */
    private int[][] trackCircleMinMax = { { 1, 4 }, { 30, 2000 }, { 50, 400 },
            { 20, 200 }, { 0, 100 }, { 10, 200 } };

    /**
     * Blur radius for the polygon detection.
     */
    private int[] trackPolygonBlurValue = { 2, 2 };

    /**
     * Min/Max blur radius value for the polygon detection.
     */
    private int[][] trackPolygonBlurMinMax = { { 0, 30 }, { 0, 30 } };

    /**
     * Parameters for the polygon detection.
     * 
     * @param accumulator_threshold
     *            – Accumulator threshold parameter. Only those lines are returned that get enough
     *            votes ( >\texttt{threshold} ).
     * @param canny_lower_threshold
     *            lower threshold for canny edge detection
     * @param canny_higher_threshold
     */
    private int[] trackPolygonValue = { 35, 50, 200 };

    /**
     * Min/Max value for polygon detection.
     */
    private int[][] trackPolygonMinMax = { { 10, 200 }, { 20, 200 },
            { 50, 400 } };

    /**
     * Parameters for sign tracker.
     * <code> int maxLineDistance, int maxPointDistance, int maxFrameDelay </code>
     */
    private int[] signTrackerValue = { 500, 1000, 5 };

    /**
     * Min/Max value for sign Tracker.
     */
    private int[][] signTrackerMinMax = { { 50, 2000 }, { 50, 2000 }, { 0, 30 } };

    /**
     * Parameters for shape detection.
     * <code> double verifyAngleTreshold, double cleanLinesAngleThreshold, double cleanLinesDistanceThreshold </code>
     */
    private double[] shapedetectionValue = { 2.0, 5.0, 8.0 };

    /**
     * Min/Max value for shape detection.
     */
    private double[][] shapedetectionMinMax = { { 1.0, 20.0 }, { 1.0, 10.0 },
            { 1.0, 30.0 } };
    /**
     * Parameters for blob detection. <code> int maxBlobs, int minBlobSize, int maxBlobSize </code>
     */
    private int[] blobDetectionValue = { 500, 400, 10000 };

    /**
     * Min/Max value for blob detection.
     */
    private int[][] blobDetectionMinMax = { { 5, 1000 }, { 10, 10000 },
            { 200, 40000 } };

    /**
     * Local boolan allow drawing into the image shown in the user interface.
     */
    private boolean drawIntoImage = true;

    /**
     * Constructor for the {@link HoughSignRecognition} class
     * 
     * @param frame
     *            the {@link MainFrame} to get the video file
     */
    public HoughSignRecognition(MainFrame frame) {
        this.frame = frame;
        this.highScalars = new LinkedList<CvScalar>();
        this.lowScalars = new LinkedList<CvScalar>();
        this.signTracking = new SignTracker(signTrackerValue[0],
                signTrackerValue[1], signTrackerValue[2]);

    }

    /**
     * Filter the image and do Hough Transformation for finding Signs. Save the found signs in a
     * {@link LinkedList} of {@link StreetObject}s. Draw "Hough-Cicles and Lines" in the
     * {@link IplImage} drawImage.
     * 
     * @param original
     *            the image which goes through the image filters and Hough-Transformation.
     * @param drawable
     *            the image in which lines and circles are drawn
     * @return {@link ResultObject} that contains a {@link LinkedList} of {@link StreetObject}s and
     *         an {@link IplImage}. Should be released in the calling method.
     */
    @Override
    public ResultObject process(IplImage original, IplImage drawable) {

        // Report.startFrame(); // XXX

        /*
         * Filter all images into one filteredimage and detect objects with the hough transformation
         * algorithms in the detect method
         */

        // Initialize new ColorFilter if not done yet.
        if (cFilter == null) {
            signTracking.setValues(signTrackerValue[0], signTrackerValue[1],
                    signTrackerValue[2]);

            cFilter = new ColorFilter(original);
            cFilter.setLows(lowScalars);
            cFilter.setHighs(highScalars);
        } else {
            // setze neues originalbild vor den filter
            cFilter.setImage(original);
        }

        cFilter.filter(1);
        return detect(original, cFilter.getImage(), drawable);

        // // objects are initialized. So all old streetobjects and their saved images should be
        // // deleted by the garbage collector
        // LinkedList<StreetObject> objects = new LinkedList<StreetObject>();
        //
        // IplImage draw = cvCloneImage(drawable);
        //
        // ResultObject tmpResult = new ResultObject(
        // new LinkedList<StreetObject>(), draw);
        //
        // // filter the original image with all given scalars
        // for (int index = 0; index < lowScalars.size(); index++) {
        // cFilter.setLowScalar(lowScalars.get(index));
        // cFilter.setHighScalar(highScalars.get(index));
        // cFilter.filter();
        //
        // // detect objects in the image
        // IplImage releaseTmp = tmpResult.getDrawImage();
        // tmpResult = detect(cFilter.getImage(), draw);
        // cvReleaseImage(releaseTmp);
        //
        // // add detected StreetObjects to list
        // objects.addAll(tmpResult.getStreetObjects());
        //
        // // deallocalize the old reference in draw
        // IplImage releaseDraw = draw;
        // draw = tmpResult.getDrawImage();
        // cvReleaseImage(releaseDraw);
        // }
        //
        // // make new resultobject to return it to the video processor
        // IplImage releaseTmp = tmpResult.getDrawImage();
        // tmpResult = new ResultObject(objects, draw);
        // cvReleaseImage(releaseTmp);
        // // release the draw image, because is not longer needed
        // cvReleaseImage(draw);
        //
        // // the image of the tmpResult should be released in the calling method
        // return tmpResult;
    }

    /**
     * A sequence of sequences of points that represent pixels that are directly connected to each
     * other.
     */
    private CvSeq contours;
    /**
     * Sequence for iteraring over the sequences of contours.
     */
    private CvSeq ptr;
    /**
     * Memory storage for cvFindContours method.
     */
    private CvMemStorage mem = CvMemStorage.create();
    /**
     * Rectangular box around the pixels that are directly connected to each other.
     */
    private CvRect boundbox;

    private boolean detected = false;

    /**
     * @param sourceImage
     *            the filtered greyscale {@link IplImage} with depth 1
     * @param drawableImage
     *            the drawable rgb {@link IplImage} with depth 3
     * @return
     */
    private ResultObject detect(IplImage original, IplImage sourceImage,
            IplImage drawableImage) {

        // clone sourceImage for blur and contour detection
        IplImage originalImage = cvCloneImage(original);
        IplImage workImage = cvCloneImage(sourceImage);
        IplImage workImageROI = cvCloneImage(sourceImage);
        IplImage workDrawable = cvCloneImage(drawableImage);

        // blur to get a better blob result in cvFindContours, use global cvContourBlur values
        blur(workImage, workImage,
                cvSize(cvContourValue[0], cvContourValue[1]), cvPoint(0, 0), 0);

        // initialize new CvSeqences
        contours = new CvSeq();
        // iterator for contours
        ptr = new CvSeq();

        // find Blobs and load them into contours sequenz
        cvFindContours(workImage, mem, contours,
                Loader.sizeof(CvContour.class), CV_RETR_CCOMP,
                CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));

        // initialize new list for tracking points to track the signs.
        LinkedList<TrackingPoint> trackingPoints = new LinkedList<TrackingPoint>();

        if (!contours.isNull()) {

            int blobCounter = 0; // counter for maximum blobs

            // go throug all blobs in countour sequenz
            for (ptr = contours; ptr != null; ptr = ptr.h_next()) {
                boundbox = cvBoundingRect(ptr, 0);
                int square = boundbox.height() * boundbox.width();

                // check if size is in range
                if (square < blobDetectionValue[2]
                        && square > blobDetectionValue[1]) {

                    blobCounter++; // break for loop if maximum blob amount is reached.
                    if (blobCounter > blobDetectionValue[0]) {
                        break;
                    }
                    // set region of interest only on found blobs
                    cvSetImageROI(workImageROI, boundbox);

                    // Report.start(); // XXX

                    StreetObject.Type type = ShapeDetection.detect(
                            workImageROI, trackCircleBlurValue[0],
                            trackCircleBlurValue[1], trackCircleValue[0],
                            trackCircleValue[1], trackCircleValue[2],
                            trackCircleValue[3], trackCircleValue[4],
                            trackCircleValue[5], trackPolygonBlurValue[0],
                            trackPolygonBlurValue[1], trackPolygonValue[0],
                            trackPolygonValue[1], trackPolygonValue[2],
                            shapedetectionValue[0], shapedetectionValue[1],
                            shapedetectionValue[2]);

                    // add the location and type of the sign to the tracking points list
                    if (type != Type.NOT_SPECIFIED) { // use original image for segmentation

                        // if (type == Type.SIGN_TRIANGLE // XXX
                        // || type == Type.SIGN_RECTANGLE
                        // || type == Type.SIGN_OCTAGON) {
                        // Report.endLine(boundbox.width() * boundbox.height());
                        // } else if (type == Type.SIGN_CIRCLE) {
                        // Report.endCircle(boundbox.width()
                        // * boundbox.height());
                        // }

                        cvSetImageROI(originalImage, boundbox);
                        trackingPoints.add(new TrackingPoint(DetectionUtils
                                .getCenter(boundbox), type, originalImage));
                        cvResetImageROI(originalImage);

                        // write text above recognized signs
                        CvFont font = new CvFont(10); // font for text printing
                        cvInitFont(font, CV_FONT_HERSHEY_SIMPLEX, 1.0, 1.0, 0,
                                2, CV_AA);
                        cvPutText(workDrawable, type.name(),
                                cvPoint(boundbox.x(), boundbox.y() - 5), // print text into image
                                font, CvScalar.RED);

                        detected = true;

                    }
                    // else {
                    // Report.endNot(boundbox.width() * boundbox.height()); // XXX
                    // }

                    cvRectangle(
                            // draw recangles around the blobs
                            workDrawable,
                            cvPoint(boundbox.x(), boundbox.y()),
                            cvPoint(boundbox.x() + boundbox.width(),
                                    boundbox.y() + boundbox.height()),
                            detected ? CvScalar.RED : CvScalar.GREEN, 1, 0, 0);

                    detected = false;

                    /*
                     * for exporting testimages while tracking
                     */
                    // if (detected) {
                    // cvSaveImage(
                    // "C:\\Users\\Philipp\\Desktop\\Bilder\\"
                    // + counter + System.currentTimeMillis()
                    // + ".jpg", workDrawable);
                    // counter++;
                    // detected = false;
                    // }

                }// ---- if between min max ----
            } // ---- for loop in countours ----

            // Report.endFrame(blobCounter); // XXX

        } // ---- if (! contours.isNull()) ----

        // add track point list to sign tracking
        // A reference to this list will be given to the ResultObject
        LinkedList<StreetObject> streetObjects = signTracking
                .addTrackpoints(trackingPoints);
        if (!streetObjects.isEmpty()) {
            for (StreetObject obj : streetObjects) {
                obj.setPluginName(this.getPluginName() + " "
                        + this.getPluginID());
            }
        }

        // only give reference to streetObjects
        ResultObject res;
        if (drawIntoImage) { // return the original drawableImage, when drawing is deactivated. So
                             // the drawings from other plugins are shown anyway.
            res = new ResultObject(streetObjects, workDrawable);
        } else {
            res = new ResultObject(streetObjects, drawableImage);
        }

        // release all local images
        cvReleaseImage(workImage);
        cvReleaseImage(workImageROI);
        cvReleaseImage(workDrawable);
        cvReleaseImage(originalImage);

        return res;
    }

    /**
     * Return lowScalars. Called in the {@link ColorFilterPanel}.
     * 
     * @return the {@link LinkedList} lowScalars
     */
    public LinkedList<CvScalar> getLowScalars() {
        return lowScalars;
    }

    /**
     * Return highScalars. Called in the {@link ColorFilterPanel}.
     * 
     * @return
     */
    public LinkedList<CvScalar> getHighScalars() {
        return highScalars;
    }

    /**
     * Set lowScalars and highScalars. Called in the {@link ColorFilterPanel}.
     * 
     * @param lowScalars
     * @param highScalars
     */
    public void setScalarLists(LinkedList<CvScalar> lowScalars,
            LinkedList<CvScalar> highScalars) {
        this.lowScalars = lowScalars;
        this.highScalars = highScalars;
    }

    /**
     * Return the chosen video file. Called in the {@link ColorFilterPanel}.
     * 
     * @return
     */
    public CvCapture getVideoCapture() {
        return frame.getVideoCapture();
    }

    /**
     * Return the standard value for cvContour.
     * 
     * @return
     */
    public int[] getCvContourValue() {
        return cvContourValue;
    }

    /**
     * Return the min max values for cvContour.
     * 
     * @return
     */
    public int[][] getCvContourMinMax() {
        return cvContourMinMax;
    }

    /**
     * Return the circle blur value.
     * 
     * @return
     */
    public int[] getTrackCircleBlurValue() {
        return trackCircleBlurValue;
    }

    /**
     * Return the min max value for circle blur.
     * 
     * @return
     */
    public int[][] getTrackCircleBlurMinMax() {
        return trackCircleBlurMinMax;
    }

    /**
     * Return the track circle standard value.
     * 
     * @return
     */
    public int[] getTrackCircleValue() {
        return trackCircleValue;
    }

    /**
     * Return the min max value for track circle.
     * 
     * @return
     */
    public int[][] getTrackCircleMinMax() {
        return trackCircleMinMax;
    }

    /**
     * Return the standard values for polygon blur.
     * 
     * @return
     */
    public int[] getTrackPolygonBlurValue() {
        return trackPolygonBlurValue;
    }

    /**
     * Return the min max values for polygon blur.
     * 
     * @return
     */
    public int[][] getTrackPolygonBlurMinMax() {
        return trackPolygonBlurMinMax;
    }

    /**
     * Return the standard values for polygon detection.
     * 
     * @return
     */
    public int[] getTrackPolygonValue() {
        return trackPolygonValue;
    }

    /**
     * Return the min max values for polygon detection.
     * 
     * @return
     */
    public int[][] getTrackPolygonMinMax() {
        return trackPolygonMinMax;
    }

    /**
     * Return standard values for sign tracking.
     * 
     * @return
     */
    public int[] getSignTrackerValue() {
        return signTrackerValue;
    }

    /**
     * Return min max values for sign tracking.
     * 
     * @return
     */
    public int[][] getSignTrackerMinMax() {
        return signTrackerMinMax;
    }

    /**
     * Return standard values for shape detection.
     * 
     * @return
     */
    public double[] getShapeDetectionValue() {
        return shapedetectionValue;
    }

    /**
     * Return min max values for sign detection.
     * 
     * @return
     */
    public double[][] getShapeDetectionMinMax() {
        return shapedetectionMinMax;
    }

    /**
     * Return standard values for blob detection.
     * 
     * @return
     */
    public int[] getBlobDetectionValue() {
        return blobDetectionValue;
    }

    /**
     * Return min max values for blob detection.
     * 
     * @return
     */
    public int[][] getBlobDetectionMinMax() {
        return blobDetectionMinMax;
    }

    /**
     * Set blur values for blob detection.
     * 
     * @param blurX
     *            kernel size x.
     * @param blurY
     *            kernel size y.
     */
    public void setCvContourValue(int blurX, int blurY) {
        cvContourValue[0] = blurX;
        cvContourValue[1] = blurY;
    }

    /**
     * Set blur values for circle detection.
     * 
     * @param blurX
     *            kernel size x
     * @param blurY
     *            kernel size y
     */
    public void setTrackCicleBlurValue(int blurX, int blurY) {
        trackCircleBlurValue[0] = blurX;
        trackCircleBlurValue[1] = blurY;
    }

    /**
     * Set values for circle detection.
     * 
     * @param db
     *            inverse ration of the akkumulator size.
     * @param minDist
     *            minimal distance between circle centers
     * @param param1
     *            higher threshold for canny edge detector, lower is twice as big
     * @param param2
     *            threshold for circle detection in the recognition stage
     * @param minRadius
     *            minimum circle radius
     * @param maxRadius
     *            maximum circle radius
     */
    public void setTrackCircleValue(int db, int minDist, int param1,
            int param2, int minRadius, int maxRadius) {
        trackCircleValue[0] = db;
        trackCircleValue[1] = minDist;
        trackCircleValue[2] = param1;
        trackCircleValue[3] = param2;
        trackCircleValue[4] = minRadius;
        trackCircleValue[5] = maxRadius;
    }

    /**
     * Set blur value for polygon detection.
     * 
     * @param blurX
     *            kernel size x
     * @param blurY
     *            kernel size y
     */
    public void setTrackPolygonBlurValue(int blurX, int blurY) {
        trackPolygonBlurValue[0] = blurX;
        trackPolygonBlurValue[1] = blurY;
    }

    /**
     * Set values for line detection
     * 
     * @param lineThreshold
     *            threshold for line voting
     * @param lowerCannyThreshold
     * @param higherCannyThreshold
     */
    public void setTrackPolygonValue(int lineThreshold,
            int lowerCannyThreshold, int higherCannyThreshold) {
        trackPolygonValue[0] = lineThreshold;
        trackPolygonValue[1] = lowerCannyThreshold;
        trackPolygonValue[2] = higherCannyThreshold;
    }

    /**
     * Set values for sign tracker.
     * 
     * @param maxLineDistance
     * @param maxPointDistance
     * @param maxFrameDelay
     */
    public void setSignTrackerValue(int maxLineDistance, int maxPointDistance,
            int maxFrameDelay) {
        signTrackerValue[0] = maxLineDistance;
        signTrackerValue[1] = maxPointDistance;
        signTrackerValue[2] = maxFrameDelay;
    }

    /**
     * Set values for shape detection.
     * 
     * @param verifyAngleThreshold
     * @param cleanLinesAngleThreshold
     * @param cleanLinesDistanceThreshold
     */
    public void setShapeDetectionValue(double verifyAngleThreshold,
            double cleanLinesAngleThreshold, double cleanLinesDistanceThreshold) {
        shapedetectionValue[0] = verifyAngleThreshold;
        shapedetectionValue[1] = cleanLinesAngleThreshold;
        shapedetectionValue[2] = cleanLinesDistanceThreshold;
    }

    /**
     * Set values for blob detection.
     * 
     * @param maxBlobs
     * @param minBlobSize
     * @param maxBlobSize
     */
    public void setBlobDetectionValue(int maxBlobs, int minBlobSize,
            int maxBlobSize) {
        blobDetectionValue[0] = maxBlobs;
        blobDetectionValue[1] = minBlobSize;
        blobDetectionValue[2] = maxBlobSize;
    }

    @Override
    public int getPluginID() {
        return pluginID;
    }

    @Override
    public void setPluginID(int id) {
        pluginID = id;

    }

    @Override
    public String getPluginName() {
        return NAME;
    }

    @Override
    public ObjectDetectionPlugin clonePlugin() {
        HoughSignRecognition hsr = new HoughSignRecognition(frame);
        hsr.setPluginID(pluginID);
        hsr.setScalarLists(lowScalars, highScalars);
        return hsr;
    }

    @Override
    public boolean isEditable() {
        return IS_EDITABLE;
    }

    @Override
    public JDialog getEditorDialog() {
        return new HoughEditorFrame(this);
    }

    @Override
    public void setDrawIntoImage(boolean draw) {
        drawIntoImage = draw;
    }

    @Override
    public boolean getDrawIntoImage() {
        return drawIntoImage;
    }
}
