package tracking.model.plugins;

import static com.googlecode.javacv.cpp.opencv_core.cvCloneImage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetRectSubPix;

import java.awt.Point;

import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class StreetObject {

    /**
     * An image from the detected object
     */
    private IplImage image;

    /**
     * In which frame of the video the object is located
     */
    private int frame;

    /**
     * The time in milliseconds the object was located in the video.
     */
    private double timestamp;

    /**
     * The frames per second rate from the video.
     */
    private double videoFPS;

    /**
     * The location of the center of the object
     */
    private Point center;

    /**
     * Name of the plugin which located the object
     */
    private String plugin;

    /**
     * Type of the {@link StreetObject}.
     */
    private Type type;

    /**
     * {@link Type} of the {@link StreetObject}.
     * 
     * @author Philipp
     */
    public static enum Type {
        SIGN_CIRCLE, SIGN_TRIANGLE, SIGN_RECTANGLE, SIGN_OCTAGON, NOT_SPECIFIED;
    }
    

    /**
     * Default Constructor.
     */
    public StreetObject() {
        this.frame = -1;
        this.timestamp = -1;
        this.videoFPS = -1;
        this.image = null;
        this.plugin = null;
        this.center = null;
        this.type = Type.NOT_SPECIFIED;
    }

    /**
     * This object represents the properties of a traffic sign.
     * 
     * @param videoframe
     *            the {@link Integer} for the frame position in the video.
     * @param trafficsignimage
     *            an {@link IplImage} of the {@link StreetObject}. ROI is saved as a clone.
     * @param plugin
     *            the {@link String} name of the plugin which located the {@link StreetObject}.
     * @param center
     *            the {@link Point} which represents the center of the {@link StreetObject}.
     * @param type
     *            the {@link Type} of the {@link StreetObject}
     */
    public StreetObject(int videoframe, IplImage trafficsignimage,
            String plugin, Point center, Type type) {

        this.frame = videoframe;
        this.timestamp = -1;
        this.videoFPS = -1;

        // only save the roi region of the image
        CvRect roi = cvGetImageROI(trafficsignimage);

        this.image = cvCreateImage(cvSize(roi.width(), roi.height()),
                trafficsignimage.depth(), trafficsignimage.nChannels());

        // save the ROI area of trafficsignimage into image
        cvGetRectSubPix(trafficsignimage, image, new CvPoint2D32f(
                (roi.width() / 2), (roi.height() / 2)));

        this.plugin = plugin;
        this.center = center;
        this.type = type;
    }

    /**
     * This object represents the properties of a traffic sign.
     * 
     * @param trafficsignimage
     *            an {@link IplImage} of the {@link StreetObject}. ROI is saved as a clone.
     * @param plugin
     *            the {@link String} name of the {@link ObjectDetectionPlugin} plugin which located the
     *            {@link StreetObject}.
     * @param center
     *            the {@link Point} which represents the center of the {@link StreetObject}.
     * @param type
     *            the {@link Type} of the {@link StreetObject}
     */
    public StreetObject(IplImage trafficsignimage, String plugin, Point center,
            Type type) {

        this.frame = -1;
        this.timestamp = -1;
        this.videoFPS = -1;

        // only save the roi region of the image
        CvRect roi = cvGetImageROI(trafficsignimage);

        this.image = cvCreateImage(cvSize(roi.width(), roi.height()),
                trafficsignimage.depth(), trafficsignimage.nChannels());

        // save the ROI area of trafficsignimage into image
        cvGetRectSubPix(trafficsignimage, image, new CvPoint2D32f(
                (roi.width() / 2), (roi.height() / 2)));
        this.plugin = plugin;
        this.center = center;
        this.type = type;;
    }

    /**
     * This object represents the properties of a traffic sign.
     * 
     * @param trafficsignimage
     *            an {@link IplImage} of the {@link StreetObject}. Image is saved as a clone, not
     *            the ROI
     */
    public StreetObject(IplImage trafficsignimage, Type type) {

        this.frame = -1;
        this.timestamp = -1;
        this.videoFPS = -1;
        this.image = cvCloneImage(trafficsignimage);
        this.plugin = null;
        this.center = null;
        this.type = type;
    }

    /**
     * Return the {@link IplImage} of the {@link StreetObject}
     * 
     * @return the {@link IplImage}
     */
    public IplImage getImage() {
        return image;
    }

    /**
     * Return the timestamp of the street object in ms as double.
     * 
     * @return timestamp, -1 if not set yet.
     */
    public double getTimestampDouble() {
        if (timestamp == -1) {
            if (videoFPS == -1 || frame == -1) {
                return -1;
            } else {
                return (double) frame / videoFPS;
            }
        } else {
            return timestamp;
        }
    }

    /**
     * Return timestamp of the street object in sec as Integer.
     * 
     * @return timestamp, -1 if not set yet.
     */
    public int getTimestampInt() {
        if (timestamp == -1) {
            if (videoFPS == -1 || frame == -1) {
                return -1;
            } else {
                return (int) ((double) frame / videoFPS);
            }
        } else {
            return (int) (timestamp / (double) 1000);
        }
    }

    /**
     * Return the {@link Integer} of the frame number
     * 
     * @return the {@link Integer} of the frame number
     */
    public int getFrame() {
        return frame;
    }

    /**
     * Return the frames per second rate from the video, this object was found in.
     * 
     * @return the frame rate per second
     */
    public double getVideoFPS() {
        return this.videoFPS;
    }

    /**
     * Return the {@link String} name of the sign.
     * 
     * @return the {@link String} name of the sign. <code>null</code> if not named yet.
     */
    public String getPluginName() {
        return plugin;
    }

    /**
     * Return the center point of the {@link StreetObject}.
     * 
     * @return
     */
    public Point getCenter() {
        return center;
    }

    /**
     * Return the {@link Type} of the {@link StreetObject}.
     * 
     * @return
     */
    public Type getType() {
        return type;
    }

    /**
     * Set the name of the {@link StreetObject}.
     * 
     * @param name
     */
    public void setPluginName(String name) {
        this.plugin = name;
    }

    /**
     * Set the timestamp in msec of the {@link StreetObject};
     * 
     * @param timestamp
     */
    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Set the frame of the {@link StreetObject}.
     * 
     * @param frame
     */
    public void setFrame(int frame) {
        this.frame = frame;
    }

    public void setVideoFPS(double videoFPS) {
        this.videoFPS = videoFPS;
    }

    /**
     * Set the image of the {@link StreetObject}.
     * 
     * @param image
     *            ROI saved as a clone.
     */
    public void setImage(IplImage image) {

        // only save the roi region of the image
        CvRect roi = cvGetImageROI(image);

        this.image = cvCreateImage(cvSize(roi.width(), roi.height()),
                image.depth(), image.nChannels());

        // save the ROI area of trafficsignimage into image
        cvGetRectSubPix(image, image,
                new CvPoint2D32f((roi.width() / 2), (roi.height() / 2)));
    }

    /**
     * Set center point of the {@link StreetObject}.
     * 
     * @param center
     */
    public void setCenter(Point center) {
        this.center = center;
    }

    /**
     * Set {@link Type} of the {@link StreetObject}.
     * 
     * @param type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Release the local {@link IplImage} image.
     */
    public void releaseImage() {
        cvReleaseImage(this.image);
    }
}
