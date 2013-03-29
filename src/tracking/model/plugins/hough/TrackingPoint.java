package tracking.model.plugins.hough;

import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;

import java.awt.Point;
import java.awt.geom.Point2D;

import tracking.model.plugins.StreetObject;
import tracking.model.plugins.StreetObject.Type;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class TrackingPoint extends Point2D {

    /**
     * {@link Type} of the {@link StreetObject} on this line.
     */
    private StreetObject.Type type;

    /**
     * The coordinates of this {@link TrackingPoint}
     */
    private Point point;

    /**
     * The image of the recognized sign.
     */
    private IplImage image;

    /**
     * Frame delay.
     */
    private int delay;

    /**
     * @param point
     * @param type
     * @param image
     *            ROI is saved as clone.
     */
    public TrackingPoint(Point point, StreetObject.Type type, IplImage image) {
        this.type = type;
        this.point = point;
        this.image = DetectionUtils.getRoiImage(image);
        this.delay = 0;
    }

    /**
     * Return the actual frame delay
     */
    public int getFrameDelay() {
        return this.delay;
    }

    /*
     * Increment the actual frame delay
     */
    public void incrementFrameDelay() {
        this.delay++;
    }

    /*
     * Reset the actual frame delay
     */
    public void resetFrameDelay() {
        this.delay = 0;
    }

    /**
     * Return the {@link Type} of the {@link StreetObject} on this line.
     * 
     * @return
     */
    public StreetObject.Type getType() {
        return this.type;
    }

    /**
     * Return the {@link IplImage} of the sign, whose location is represented by this class.
     * 
     * @return ths image.
     */
    public IplImage getImage() {
        return this.image;
    }

    /**
     * Release the image of the tracking point.
     */
    public void releaseImage() {
        cvReleaseImage(image);
    }

    @Override
    public double getX() {
        return this.point.getX();
    }

    @Override
    public double getY() {
        return this.point.getY();
    }

    @Override
    public void setLocation(double x, double y) {
        this.point = new Point((int) x, (int) y);
    }
}
