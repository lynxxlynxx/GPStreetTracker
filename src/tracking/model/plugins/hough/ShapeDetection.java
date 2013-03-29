package tracking.model.plugins.hough;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HOUGH_GRADIENT;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HOUGH_STANDARD;
import static com.googlecode.javacv.cpp.opencv_imgproc.blur;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvHoughCircles;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvHoughLines2;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Vector;

import tracking.model.plugins.StreetObject;
import tracking.model.plugins.StreetObject.Type;

import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ShapeDetection {

    private static CvMemStorage storage = cvCreateMemStorage(0);

    private static CvSeq sequence;

    /**
     * Search for {@link StreetObject} shapes and return the {@link StreetObject.Type}.
     * 
     * @param roiImage
     *            search in this {@link IplImage}
     * @return the {@link StreetObject.Type} of the found shape.
     *         {@link StreetObject.Type.NOT_SPECIFIED} if no shape is detected.
     */
    public static StreetObject.Type detect(IplImage roiImage, int circleBlurX,
            int circleBlurY, int db, int minDist, int param1, int param2,
            int minRadius, int maxRadius, int polygonBlurX, int polygonBlurY,
            int lineThreshold, int cannyLow, int cannyHigh,
            double verifyAngleThreshold, double cleanLinesAngleThreshold,
            double cleanLinesDistanceThreshold) {
        return trackCircle(roiImage, circleBlurX, circleBlurY, db, minDist,
                param1, param2, minRadius, maxRadius) ? StreetObject.Type.SIGN_CIRCLE
                : trackPolygon(roiImage, polygonBlurX, polygonBlurY,
                        lineThreshold, cannyLow, cannyHigh,
                        verifyAngleThreshold, cleanLinesAngleThreshold,
                        cleanLinesDistanceThreshold);
    }

    /**
     * Use cvHoughCircle to detect cicles.
     * 
     * @param roiImage
     * @return
     */
    private static boolean trackCircle(IplImage roiImage, int circleBlurX,
            int circleBlurY, int db, int minDist, int param1, int param2,
            int minRadius, int maxRadius) {
        storage = CvMemStorage.create();
        sequence = new CvSeq();

        // Get the roi image of the given image. This is a new instance.
        IplImage tmpImage = DetectionUtils.getRoiImage(roiImage);

        blur(tmpImage, tmpImage, cvSize(circleBlurX, circleBlurY),
                cvPoint(0, 0), 0);

        /**
         * CvSeq* circles = cvHoughCircles(CvArr* image, void* circle_storage, int method, double
         * dp, double min_dist, double param1=100, double param2=100, int min_radius=0, int
         * max_radius=0 )
         * 
         * @param circles
         *            – Output vector of found circles. Each vector is encoded as a 3-element
         *            floating-point vector (x, y, radius).
         * @param image
         *            – 8-bit, single-channel, grayscale input image.
         * @param circle_storage
         *            – In C function this is a memory storage that will contain the output sequence
         *            of found circles.
         * @param method
         *            – Detection method to use. Currently, the only implemented method is
         *            CV_HOUGH_GRADIENT , which is basically 21HT , described in [Yuen90].
         * @param dp
         *            – Inverse ratio of the accumulator resolution to the image resolution. For
         *            example, if dp=1 , the accumulator has the same resolution as the input image.
         *            If dp=2 , the accumulator has half as big width and height.
         * @param minDist
         *            – Minimum distance between the centers of the detected circles. If the
         *            parameter is too small, multiple neighbor circles may be falsely detected in
         *            addition to a true one. If it is too large, some circles may be missed.
         * @param param1
         *            – First method-specific parameter. In case of CV_HOUGH_GRADIENT , it is the
         *            higher threshold of the two passed to the Canny() edge detector (the lower one
         *            is twice smaller). = 100
         * @param param2
         *            – Second method-specific parameter. In case of CV_HOUGH_GRADIENT , it is the
         *            accumulator threshold for the circle centers at the detection stage. The
         *            smaller it is, the more false circles may be detected. Circles, corresponding
         *            to the larger accumulator values, will be returned first. = 100
         * @param minRadius
         *            – Minimum circle radius. = 0
         * @param maxRadius
         *            – Maximum circle radius. = 0
         */
        sequence = cvHoughCircles(roiImage, storage, CV_HOUGH_GRADIENT, db,
                minDist, param1, param2, minRadius, maxRadius);

        // release the tmpImage, because it's not longer needed.
        cvReleaseImage(tmpImage);

        // if there is one or more cicles return true, else return false.
        if (sequence.total() != 0) {
            return true;
        }
        return false;
    }

    /**
     * Find lines in the specified image and try to detect polygons.
     * 
     * @param srcImage
     *            the {@link IplImage}
     * @return the {@link Type} of the shape
     */
    private static StreetObject.Type trackPolygon(IplImage srcImage,
            int polygonBlurX, int polygonBlurY, int lineThreshold,
            int cannyLow, int cannyHigh, double verifyAngleThreshold,
            double cleanLinesAngleThreshold, double cleanLinesDistanceThreshold) {

        // get roi of srcImage
        IplImage workImage = DetectionUtils.getRoiImage(srcImage);

        blur(workImage, workImage, cvSize(polygonBlurX, polygonBlurY),
                cvPoint(0, 0), 0);
        cvCanny(workImage, workImage, cannyLow, cannyHigh, 3);
        sequence = new CvSeq();

        /**
         * CvSeq* lines = cvHoughLines2(CvArr* image, void* line_storage, int method, double rho,
         * double theta, int threshold, double param1=0, double param2=0 )
         * 
         * @param lines
         *            – Output vector of lines. Each line is represented by a two-element vector
         *            (\rho, \theta) . \rho is the distance from the coordinate origin (0,0)
         *            (top-left corner of the image). \theta is the line rotation angle in radians (
         *            0 \sim \textrm{vertical line}, \pi/2 \sim \textrm{horizontal line} ).
         * @param image
         *            – 8-bit, single-channel binary source image. The image may be modified by the
         *            function.
         * @param line_storage
         *            – In C function this is a memory storage that will contain the output sequence
         *            of found lines.
         * @param method
         *            – One of the following Hough transform variants: CV_HOUGH_STANDARD classical
         *            or standard Hough transform. Every line is represented by two floating-point
         *            numbers (\rho, \theta) , where \rho is a distance between (0,0) point and the
         *            line, and \theta is the angle between x-axis and the normal to the line. Thus,
         *            the matrix must be (the created sequence will be) of CV_32FC2 type
         *            CV_HOUGH_PROBABILISTIC probabilistic Hough transform (more efficient in case
         *            if the picture contains a few long linear segments). It returns line segments
         *            rather than the whole line. Each segment is represented by starting and ending
         *            points, and the matrix must be (the created sequence will be) of the CV_32SC4
         *            type. CV_HOUGH_MULTI_SCALE multi-scale variant of the classical Hough
         *            transform. The lines are encoded the same way as CV_HOUGH_STANDARD.
         * @param rho
         *            – Distance resolution of the accumulator in pixels.
         * @param theta
         *            – Angle resolution of the accumulator in radians.
         * @param threshold
         *            – Accumulator threshold parameter. Only those lines are returned that get
         *            enough votes ( >\texttt{threshold} ).
         * @param param1
         *            – First method-dependent parameter: For the classical Hough transform, it is
         *            not used (0). For the probabilistic Hough transform, it is the minimum line
         *            length. For the multi-scale Hough transform, it is srn.
         * @param param2
         *            – Second method-dependent parameter: For the classical Hough transform, it is
         *            not used (0). For the probabilistic Hough transform, it is the maximum gap
         *            between line segments lying on the same line to treat them as a single line
         *            segment (that is, to join them). For the multi-scale Hough transform, it is
         *            stn.
         */
        sequence = cvHoughLines2(workImage, storage, CV_HOUGH_STANDARD, 1,
                Math.PI / 180, lineThreshold, 0, 0); // standard 150 bei threshold

        // release workImage. the only image in this whole class
        cvReleaseImage(workImage);

        // list for the lines
        LinkedList<CvPoint2D32f> list = new LinkedList<CvPoint2D32f>();

        // take lines from seqence and put them into a list of CvPoint2D32f
        for (int i = 0; i < sequence.total(); i++) {
            // in 2d ebene projezieren
            CvPoint2D32f point = new CvPoint2D32f(cvGetSeqElem(sequence, i));
            list.add(point);
        }

        // list for cleaned lines
        LinkedList<CvPoint2D32f> cleanList = cleanLines(list,
                cleanLinesAngleThreshold, cleanLinesDistanceThreshold);

        if (cleanList.size() <= 2) {
            return Type.NOT_SPECIFIED;
        }

        // compute the angles between the lines
        double[] angles = computeAngles(cleanList);

        return verify(angles, verifyAngleThreshold);
    }

    /**
     * Verfify the shape of the lines to each other
     * 
     * @param angles
     *            the double array of angles
     * @param threshold
     *            the angle threshold to calibrate the range
     * @return
     */
    private static StreetObject.Type verify(double[] angles, double threshold) {
        if (verifyTriangle(angles, threshold)) {
            return Type.SIGN_TRIANGLE;
        } else if (verifyRectangle(angles, threshold)) {
            return Type.SIGN_RECTANGLE;
        } else {
            return Type.NOT_SPECIFIED;
        }
    }

    /**
     * Help method for verify. Verify if shape is a triangle. Count the angles of the intersecting
     * lines. If there are 3 60 degree angles, it's a triangle.
     * 
     * @param angles
     *            the double array of angles
     * @param threshold
     *            the angle threshold to calibrate the range
     * @return
     */
    private static boolean verifyTriangle(double[] angles, double threshold) {
        int count = 0;
        for (int i = 0; i < angles.length; i++) {
            double ang = angles[i];
            if (ang > Math.PI / 180 * 90) {
                ang = Math.PI - ang;
            }
            if (ang <= (Math.PI / 180 * 60) + (Math.PI / 180 * threshold)
                    && ang >= (Math.PI / 180 * 60)
                            - (Math.PI / 180 * threshold)) {
                count++;
            }

        }
        return count == 2 ? true : false;

    }

    /**
     * Help method for verify. Verify if shape is a rectangle. Count the angles of the intersecting
     * lines. If there are 4 90 degree angles, it's a rectangle.
     * 
     * @param angles
     *            the double array of angles
     * @param threshold
     *            the angle threshold to calibrate the range
     * @return
     */
    private static boolean verifyRectangle(double[] angles, double threshold) {
        int count = 0;
        for (int i = 0; i < angles.length; i++) {
            double ang = angles[i];
            if (ang <= (Math.PI / 180 * 90) + (Math.PI / 180 * threshold)
                    && ang >= (Math.PI / 180 * 90)
                            - (Math.PI / 180 * threshold)) {
                count++;
            }

        }
        return count == 2 ? true : false;
    }

    /**
     * Delete duplicates of lines from a {@link LinkedList} with {@link CvPoint2D32f} points,
     * point.x() is the distance rho and point.y() is the angle theta.
     * 
     * @param theta
     *            the maximum angle between the lines
     * @param rho
     *            the maximum distance between the lines
     * @return the list after deleting duplicates
     */
    private static LinkedList<CvPoint2D32f> cleanLines(
            LinkedList<CvPoint2D32f> list, double theta, double rho) {

        LinkedList<CvPoint2D32f> result = new LinkedList<CvPoint2D32f>();

        if (list == null || list.isEmpty()) {
            return list;
        } else if (list.size() == 1) { // return list if there is only 1 entry
            return list;
        }

        @SuppressWarnings("unchecked")
        LinkedList<CvPoint2D32f> lines = (LinkedList<CvPoint2D32f>) list
                .clone();

        Collections.sort(lines, new sortTheta()); // sort list according to angles

        /*
         * Angle filter: go through the list and compare the last object with the actual one. If the
         * angle between these two lines is bigger than the given theta value, then the split off
         * the list with the concurrent lines and give it to the range filter.
         */

        int lastPos = 0;
        CvPoint2D32f last = lines.get(0);
        CvPoint2D32f actual;
        // devide angles, if angles are not in range theta, give sublist to rangeFilter
        for (int i = 0; i < lines.size(); i++) {
            actual = lines.get(i);
            if (DetectionGeometry.inEpsilonRange(actual.y(), last.y(), theta)) { // if actual is in
                                                                                 // range with last,
                // go to next
                last = actual;
            } else {
                // if actual is not in range, rangeFilter sublist
                result.addAll(rangeFilter(subList(lines, lastPos, i), rho));
                lastPos = i;
                last = actual;
            }
        }
        // add last part of list
        result.addAll(rangeFilter(subList(lines, lastPos, lines.size()), rho));
        return result;
    }

    /**
     * fromIndex inclusive and toIndex exclusive
     */
    private static LinkedList<CvPoint2D32f> subList(
            LinkedList<CvPoint2D32f> list, int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex > list.size() || toIndex < fromIndex
                || toIndex > list.size() || fromIndex == toIndex) {
            return null;
        }

        LinkedList<CvPoint2D32f> result = new LinkedList<CvPoint2D32f>();

        for (int i = fromIndex; i < toIndex; i++) {
            result.add(list.get(i));
        }
        return result;
    }

    /**
     * Help method for cleanLines. Same as angleFilter in the cleanLines method, but for distances
     * (point.x())
     */
    private static LinkedList<CvPoint2D32f> rangeFilter(
            LinkedList<CvPoint2D32f> list, double rho) {

        if (list.size() == 1) { // return list if there is only 1 entry
            return list;
        }

        @SuppressWarnings("unchecked")
        LinkedList<CvPoint2D32f> lines = (LinkedList<CvPoint2D32f>) list
                .clone();

        LinkedList<CvPoint2D32f> result = new LinkedList<CvPoint2D32f>();

        Collections.sort(lines, new sortRho()); // sort sublist according to distance
        int lastPos = 0;
        CvPoint2D32f last = lines.get(0);
        CvPoint2D32f actual;

        for (int i = 0; i < lines.size(); i++) {
            actual = lines.get(i);
            if (DetectionGeometry.inEpsilonRange(actual.x(), last.x(), rho)) { // if actual is in
                                                                               // range with last,
                // go to next
                last = actual;
            } else {
                // if actual is not in range, get average line from sublist
                result.add(getAverageLine(subList(lines, lastPos, i)));
                lastPos = i;
                last = actual;
            }
        }
        // add the last part of the list
        result.add(getAverageLine(subList(lines, lastPos, lines.size())));

        return result;
    }

    /**
     * Help method for rangeFilter. Calculate the average distance rho (point.x()) of the given
     * lines and return the line with the nearest value
     */
    private static CvPoint2D32f getAverageLine(LinkedList<CvPoint2D32f> lines) {
        if (lines.isEmpty()) {
            return null;
        } else if (lines.size() < 3) {
            return lines.get(0);
        } else if (lines.size() == 3) {
            return lines.get(1);
        }
        double value = 0;
        int amount = 0;
        for (int i = 0; i < lines.size(); i++) {
            amount++;
            value += lines.get(i).x();
        }

        double average = value / amount;
        int index = lines.size() - 1; // set to maximum index
        double minValue = lines.get(lines.size() - 1).x(); // set to maximum value
        for (int i = 0; i < lines.size(); i++) {
            double tmp = Math.abs(lines.get(i).x() - average);
            if (tmp < minValue) {
                minValue = tmp;
                index = i;
            }
        }
        return lines.get(index);
    }

    /**
     * Compute all angles between the lines.
     * 
     * @param list
     *            a {@link LinkedList} of {@link CvPoint2D32f} which represent lines. Point.x is the
     *            distance from (0,0), Point.y is the angle theta.
     * @return
     */
    private static double[] computeAngles(LinkedList<CvPoint2D32f> list) {
        if (list.size() < 2) {
            return null;
        }
        // int pos = DetectionGeometry.factorial(list.size()).intValue()
        // / (2 * DetectionGeometry.factorial(list.size() - 2).intValue()); EXCEPTION: 13! > 2^32 -
        // 1

        int pos = (list.size() - 1) * list.size() / 2;

        double[] result = new double[pos];

        int[] possi = new int[pos * 2];
        // compute all possibilities
        int c = 0;
        for (int i = 0; i < list.size() - 1; i++) {
            for (int j = i + 1; j < list.size(); j++) {
                possi[c] = i;
                possi[c + 1] = j;
                c += 2;
            }
        }

        // System.out.println("possies");
        // for (int i = 0; i < possi.length; i++) {
        // System.out.print(possi[i] + " ");
        // }

        int possicounter = 0;
        for (int i = 0; i < result.length; i++) {
            result[i] = getAngleBetweenLines(list.get(possi[possicounter]),
                    list.get(possi[possicounter + 1]));
            possicounter += 2;
        }

        return result;
    }

    /**
     * Compute the angles between two lines in {@link CvPoint2D32f} format. This method uses the
     * computeAngle method from {@link Geometry} class.
     * 
     * @param line1the
     *            first line
     * @param line2
     *            the second line
     * @return the angle between the lines
     */
    private static double getAngleBetweenLines(CvPoint2D32f line1,
            CvPoint2D32f line2) {
        // compute 2 points of each line
        CvPoint2D32f[] points1 = DetectionGeometry.getPointsOnLine(line1);
        CvPoint2D32f[] points2 = DetectionGeometry.getPointsOnLine(line2);

        // compute intersection between the two lines
        double[] intersection = DetectionGeometry.getLineLineIntersection(
                points1[0].x(), points1[0].y(), points1[1].x(), points1[1].y(),
                points2[0].x(), points2[0].y(), points2[1].x(), points2[1].y());

        // lines do not intersect. They are the same or parallel
        if (intersection == null) {
            return 0;
        }

        double[] isec = { intersection[0], intersection[1], 0 };
        // take one point of each line
        double[] p1 = { points1[0].x(), points1[0].y(), 0 };
        double[] p2 = { points2[0].x(), points2[0].y(), 0 };
        // return the angle between the lines, the geometry class needs 3D points

        return DetectionGeometry.computeAngle(p1, isec, p2);

    }

    /**
     * Comparator class to sort a {@link LinkedList} < {@link CvPoint2D32f}> according to point.x().
     * 
     * @author Philipp
     */
    public static class sortRho implements Comparator<CvPoint2D32f> {

        @Override
        public int compare(CvPoint2D32f p1, CvPoint2D32f p2) {
            return p1.x() < p2.x() ? -1 : p1.x() > p2.x() ? 1 : 0;
        }
    }

    /**
     * Comparator class to sort a {@link LinkedList} < {@link CvPoint2D32f}> according to point.y().
     * 
     * @author Philipp
     */
    public static class sortTheta implements Comparator<CvPoint2D32f> {

        @Override
        public int compare(CvPoint2D32f p1, CvPoint2D32f p2) {
            return p1.y() < p2.y() ? -1 : p1.y() > p2.y() ? 1 : 0;
        }
    }

    /**
     * Comparator class to sort a {@link Vector}<Float>
     * 
     * @author Philipp
     */
    public static class sortThetaVector implements Comparator<Vector<Float>> {

        @Override
        public int compare(Vector<Float> a, Vector<Float> b) {
            float x = a.get(1);
            float y = b.get(1);

            if (x < y)
                return -1;
            if (x > y)
                return 1;
            return 0;

        }
    }
}
