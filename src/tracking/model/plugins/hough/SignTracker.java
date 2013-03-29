package tracking.model.plugins.hough;

import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;

import java.awt.geom.Line2D;
import java.util.LinkedList;

import tracking.model.plugins.StreetObject;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Processing the location data of the signs from the {@link HoughSignRecognition} class. Filter
 * points, so that every sign is only recognized once.
 * 
 * @author Philipp Unger
 */
public class SignTracker {

    /**
     * List of sign location points
     */
    private LinkedList<TrackingPoint> trackingPoints;

    /**
     * List of located lines
     */
    private LinkedList<TrackingLine> trackingLines;

    private double maxLineDistance;
    private double maxPointDistance;
    private int maxFrameDelay;

    /**
     * Constructor for sign tracker
     * 
     * @param maxLineDistance
     *            The maximum distance a point can have to a line, built by two other points.
     * @param maxPointDistance
     *            maximum distance a point can have to the tip of the line.
     * @param maxFrameDelay
     *            maximum frame delay until a line or a point is recognized as a sign. E.g.
     *            maxFrameDelay = 5, when a tracking line does not get new points within 5 frames,
     *            it is recognized as a sign.
     */
    public SignTracker(double maxLineDistance, double maxPointDistance,
            int maxFrameDelay) {
        this.maxLineDistance = maxLineDistance;
        this.maxPointDistance = maxPointDistance;
        this.maxFrameDelay = maxFrameDelay;
        this.trackingLines = new LinkedList<TrackingLine>();
        this.trackingPoints = new LinkedList<TrackingPoint>();
    }

    /**
     * Add trackpoints to the {@link SignTracker}. The tracking methos is "Take First Hit". That
     * means, the first 3 points which are detected on one line are deleted and become a line. Then,
     * the remaining points are processed. In line-point detection, the first point detected on a
     * line is added to this line as new point and the detection goes on with the next line in the
     * list. This method returns a {@link LinkedList} of {@link StreetObject}
     * 
     * @param trackingPoints
     *            a {@link LinkedList} of new found tracking points
     * @return {@link LinkedList} of the tracked {@link StreetObject}
     */
    public LinkedList<StreetObject> addTrackpoints(
            LinkedList<TrackingPoint> trackingPoints) {
        this.trackingPoints.addAll(trackingPoints);
        dctLinePoint();
        dctPointLine();
        LinkedList<StreetObject> objects = new LinkedList<StreetObject>();

        for (int i = 0; i < trackingLines.size(); i++) {
            TrackingLine line = trackingLines.get(i);
            if (line.getFrameDelay() > maxFrameDelay) {
                objects.add(new StreetObject(line.getImage(), line.getType()));
                line.releaseImage();
                trackingLines.remove(i);
                i--;
            }
        }

        for (int i = 0; i < this.trackingPoints.size(); i++) {
            TrackingPoint point = this.trackingPoints.get(i);
            if (point.getFrameDelay() > maxFrameDelay) {
                objects.add(new StreetObject(point.getImage(), point.getType()));
                point.releaseImage();
                this.trackingPoints.remove(i);
                i--;
            }
        }

        incrementFrameDelay();
        return objects;
    }

    /**
     * Detect lines with 3 points on it with "Take First Hit" method
     */
    private void dctPointLine() {
        // trackingPoints is null, when all trackingPoints are tracked in the dctLinePoint method
        if (trackingPoints == null || trackingPoints.isEmpty()
                || trackingPoints.size() < 3) {
            return;
        }
        // find all lines between each pair of 2 points
        for (int i = 0; i < trackingPoints.size() - 1; i++) {
            TrackingPoint P1 = trackingPoints.get(i);

            jfor: for (int j = i + 1; j < trackingPoints.size(); j++) {
                TrackingPoint P2 = trackingPoints.get(j);

                // check if the points have a maximum distance of maxPointDistance
                if (P1.getType() == P2.getType()
                        && P1.distance(P2) <= maxPointDistance) {

                    // check if one of the remaining points is on a line built from 2 other points
                    for (int j2 = 0; j2 < trackingPoints.size(); j2++) {

                        // these points already are on the line
                        if (j2 != i && j2 != j) {
                            TrackingPoint P3 = trackingPoints.get(j2);

                            // The type of P3 must be the same as P2 and P1
                            // The distance of P3 to P2 must be lower than maxPointDistance. P2 is
                            // the tip of our temporary line: When the sign is only going in one
                            // direction, the points are sorted in the chronology they appear.
                            // The distance of P3 to the line(P1,P2) must be lower than
                            // maxLineDistance.
                            if (P2.getType() == P3.getType()
                                    && P3.distance(P2) <= maxPointDistance
                                    && Line2D.ptLineDist(P1.getX(), P1.getY(),
                                            P2.getX(), P2.getY(), P3.getX(),
                                            P3.getY()) <= maxLineDistance) {

                                // the point is on the line. Make a new line.
                                trackingLines.add(new TrackingLine(P2, P3, P3
                                        .getType(), getMaxImage(P2, P3)));

                                // release images and remove points, that are now a line, form
                                // trackingPoints list.
                                P1.releaseImage();
                                P2.releaseImage();
                                P3.releaseImage();
                                // when a point is removed from the list, one or more indices change
                                // and i,j and j2 have to be corrected.
                                trackingPoints.remove(i);
                                j--;
                                trackingPoints.remove(j);
                                if (j2 >= i && j2 < j + 1) {
                                    j2--;
                                } else if (j2 >= j + 1) {
                                    j2 = j2 - 2;
                                }
                                trackingPoints.remove(j2);
                                i--;
                                break jfor;
                            }
                        }
                    }// end for j2
                } // end if distance P1 P2
            }// end for j
        }// end for i

    }

    /**
     * Return the bigger image of P1 and P2
     * 
     * @param P1
     * @param P2
     * @return
     */
    private IplImage getMaxImage(TrackingPoint P1, TrackingPoint P2) {
        int x = P1.getImage().height() * P1.getImage().width();
        int y = P2.getImage().height() * P2.getImage().width();

        if (x < y) {
            return P2.getImage();
        } else {
            return P1.getImage();
        }
    }

    /**
     * Detect point that belongs to a line with "Take First Hit" method
     */
    private void dctLinePoint() {

        if (trackingLines.isEmpty() || trackingPoints.isEmpty()) {
            return;
        }

        // go through all lines
        for (int i = 0; i < trackingLines.size(); i++) {
            TrackingLine line = trackingLines.get(i);
            // compare with all points
            for (int j = 0; j < trackingPoints.size(); j++) {
                TrackingPoint point = trackingPoints.get(j);

                // ****a new point is found
                // Frame delay of new point must be 0. That means it's a really new point.
                // The smallest distance of the point to the line must be lower than
                // maxLineDistance.
                // The distance from P2, which is the tip of the line, to the new point must be
                // lower than maxPointDistance.
                // The StreetObject.Type of the new point must be the same as the Type of the line.
                if (point.getFrameDelay() == 0
                        && line.getType() == point.getType()
                        && line.getP2().distance(point) <= maxPointDistance
                        && line.ptLineDist(point) <= maxLineDistance) {

                    // set new tip of the line
                    line.setLine(line.getP2(), point);

                    // set new image for the line if it's bigger than the old one. (better quality)
                    IplImage old = line.getImage();
                    IplImage nevv = point.getImage();

                    int x = old.height() * old.width();
                    int y = nevv.height() * nevv.width();
                    if (x < y) {
                        line.setImage(nevv);
                        cvReleaseImage(old);
                    }
                    line.resetFrameDelay();
                    point.releaseImage();
                    trackingPoints.remove(j);

                } // end if point recognized

            }// end for loop: points
        }
    }

    // /**
    // * fromIndex inclusive and toIndex exclusive
    // */
    // private LinkedList<TrackingLine> subList(LinkedList<TrackingLine> list,
    // int fromIndex, int toIndex) {
    // if (fromIndex < 0 || fromIndex > list.size() || toIndex < fromIndex
    // || toIndex > list.size() || fromIndex == toIndex) {
    // return null;
    // }
    //
    // LinkedList<TrackingLine> result = new LinkedList<TrackingLine>();
    //
    // for (int i = fromIndex; i < toIndex; i++) {
    // result.add(list.get(i));
    // }
    // return result;
    // }

    public void setValues(double maxLineDistance, double maxPointDistance,
            int maxFrameDelay) {
        this.maxFrameDelay = maxFrameDelay;
        this.maxLineDistance = maxLineDistance;
        this.maxPointDistance = maxPointDistance;
    }

    /**
     * Increment the frame delays in all tracking points and tracking lines.
     */
    private void incrementFrameDelay() {
        for (TrackingPoint point : trackingPoints) {
            point.incrementFrameDelay();
        }
        for (TrackingLine line : trackingLines) {
            line.incrementFrameDelay();
        }
    }

}
