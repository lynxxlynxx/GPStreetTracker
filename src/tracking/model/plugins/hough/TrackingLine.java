package tracking.model.plugins.hough;

import static com.googlecode.javacv.cpp.opencv_core.cvCloneImage;

import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

import tracking.model.plugins.StreetObject;
import tracking.model.plugins.StreetObject.Type;

public class TrackingLine extends Line2D {

    /**
     * {@link Type} of the {@link StreetObject} on this line.
     */
    private StreetObject.Type type;

    /**
     * Start {@link Point} of the line.
     */
    private Point P1;

    /**
     * End {@link Point} of the line.
     */
    private Point P2;

    /**
     * The image of the recognized sign.
     */
    private IplImage image;

    /**
     * Frame delay.
     */
    private int delay;

    /**
     * @param P1
     *            start {@link Point} of the line
     * @param P2
     *            end {@link Point} of the line. This is the tip of the line. Important for
     *            calculating the distance to new points.
     * @param type
     * @param image
     *            Image is save as clone.
     */
    public TrackingLine(Point2D P1, Point2D P2, StreetObject.Type type,
            IplImage image) {
        this.type = type;
        this.P1 = new Point((int) P1.getX(), (int) P1.getY());
        this.P2 =new Point((int) P2.getX(), (int) P2.getY());
        this.image = cvCloneImage(image);
        this.delay = 0;
    }

    /**
     * Return the actual frame delay
     */
    public int getFrameDelay() {
        return this.delay;
    }

    /**
     * Set the image of the sign. The image will be saved as a clone.
     * 
     * @param iamge
     *            the image to set
     */
    public void setImage(IplImage image) {
        this.image = cvCloneImage(image);
    }

    /**
     * Return reference to the actual image saved in this line.
     * 
     * @return
     */
    public IplImage getImage() {
        return this.image;
    }

    /**
     * Release the local image. Call when this line is no longer needed.
     */
    public void releaseImage() {
        cvReleaseImage(image);
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

    @Override
    public Rectangle2D getBounds2D() {
        return new Rectangle(Math.min(P1.x, P2.x), Math.min(P1.y, P2.y),
                Math.max(P1.x, P2.x), Math.max(P1.y, P2.y));
    }

    @Override
    public Point2D getP1() {
        return this.P1;
    }

    /**
     * Returns the end {@link Point2D} of the line. This is the tip of the line.
     */
    @Override
    public Point2D getP2() {
        return this.P2;
    }

    @Override
    public double getX1() {
        return this.P1.getX();
    }

    @Override
    public double getX2() {
        return this.P2.getX();
    }

    @Override
    public double getY1() {
        return this.P1.getY();
    }

    @Override
    public double getY2() {
        return this.P2.getY();
    }

    /**
     * Set the start end the end {@link Point} of the line. <code>x2, y2</code> are the coordinates
     * of the end Point, which is the tip of the line.
     */
    @Override
    public void setLine(double x1, double y1, double x2, double y2) {
        this.P1 = new Point((int) x1, (int) x2);
        this.P2 = new Point((int) x2, (int) y2);
    }
}
