package tracking.model.plugins.hough;

import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;

/**
 * Collection of geometry methods for {@link ShapeDetection}. Also includes some excerpts of the
 * class Geometry by geosoft.no. All methods are static.
 */
public class DetectionGeometry {

    /**
     * Return true if the absolute difference between theta1 and theta2 is lower than epsilon.
     * 
     * @param theta1
     *            the first angle
     * @param theta2
     *            the second angle
     * @param epsilon
     *            the maximum range
     * @return
     */
    public static boolean inEpsilonRange(double theta1, double theta2,
            double epsilon) {

        return Math.abs(theta1 - theta2) <= epsilon;
    }

    /**
     * Compute 2 Points of a given line.
     * 
     * @param line
     *            the line in {@link CvPoint2D32f} format.
     * @return A {@link CvPoint2D32f} array with [0] is the first point and [1] is the second point
     */
    public static CvPoint2D32f[] getPointsOnLine(CvPoint2D32f line) {
        CvPoint2D32f[] result = new CvPoint2D32f[2];

        float rho = line.x();
        float theta = line.y();
        double a = Math.cos((double) theta);
        double b = Math.sin((double) theta);
        double x0 = a * rho;
        double y0 = b * rho;
        result[0] = new CvPoint2D32f((int) Math.round(x0 + 10000 * (-b)),
                (int) Math.round(y0 + 10000 * (a)));
        result[1] = new CvPoint2D32f((int) Math.round(x0 - 10000 * (-b)),
                (int) Math.round(y0 - 10000 * (a)));

        return result;
    }

    /**
     * Get the Intersection between 2 lines.
     * 
     * @param x1
     *            of first point of line 1
     * @param y1
     *            of first point of line 1
     * @param x2
     *            of second point of line 1
     * @param y2
     *            of secoind point of line 1
     * @param x3
     *            of first point of line 2
     * @param y3
     *            of first point of line 2
     * @param x4
     *            of second point of line 2
     * @param y4
     *            of second point of line 2
     * @return the intersection point as double array
     */
    public static double[] getLineLineIntersection(double x1, double y1,
            double x2, double y2, double x3, double y3, double x4, double y4) {
        double det1And2 = det(x1, y1, x2, y2);
        double det3And4 = det(x3, y3, x4, y4);
        double x1LessX2 = x1 - x2;
        double y1LessY2 = y1 - y2;
        double x3LessX4 = x3 - x4;
        double y3LessY4 = y3 - y4;
        double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
        if (det1Less2And3Less4 == 0) {
            // the denominator is zero so the lines are parallel and there's either no solution (or
            // multiple solutions if the lines overlap) so return null.
            return null;
        }
        double x = (det(det1And2, x1LessX2, det3And4, x3LessX4) / det1Less2And3Less4);
        double y = (det(det1And2, y1LessY2, det3And4, y3LessY4) / det1Less2And3Less4);
        double[] result = new double[2];
        result[0] = x;
        result[1] = y;
        return result;
    }

    private static double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }

    /**
     * Find the angle between twree points. P0 is center point
     * 
     * @param p0
     *            , p1, p2 Three points finding angle between [x,y,z].
     * @return Angle (in radians) between given points.
     */
    public static double computeAngle(double[] p0, double[] p1, double[] p2) {
        double[] v0 = DetectionGeometry.createVector(p0, p1);
        double[] v1 = DetectionGeometry.createVector(p0, p2);

        double dotProduct = DetectionGeometry.computeDotProduct(v0, v1);

        double length1 = DetectionGeometry.length(v0);
        double length2 = DetectionGeometry.length(v1);

        double denominator = length1 * length2;

        double product = denominator != 0.0 ? dotProduct / denominator : 0.0;

        double angle = Math.acos(product);

        return angle;
    }

    /**
     * Compute the dot product (a scalar) between two vectors.
     * 
     * @param v0
     *            , v1 Vectors to compute dot product between [x,y,z].
     * @return Dot product of given vectors.
     */
    public static double computeDotProduct(double[] v0, double[] v1) {
        return v0[0] * v1[0] + v0[1] * v1[1] + v0[2] * v1[2];
    }

    /**
     * Construct the vector specified by two points.
     * 
     * @param p0
     *            , p1 Points the construct vector between [x,y,z].
     * @return v Vector from p0 to p1 [x,y,z].
     */
    public static double[] createVector(double[] p0, double[] p1) {
        double v[] = { p1[0] - p0[0], p1[1] - p0[1], p1[2] - p0[2] };
        return v;
    }

    /**
     * Return the length of a vector.
     * 
     * @param v
     *            Vector to compute length of [x,y,z].
     * @return Length of vector.
     */
    public static double length(double[] v) {
        return Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
    }

    /**
     * @param a
     * @param b
     * @return
     */
    public static double pythagoras(double a, double b) {
        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    /**
     * Tests if the interior of the first {@link CvRect} intersects the interior of the second
     * {@link CvRect}.
     * 
     * @param rect1
     *            the first {@link CvRect}
     * @param rect2
     *            the second {@link CvRect}
     * @return <code>true</code> if the first <code>CvRect</code> intersects the second
     *         <code>CvRect</code>; <code>false</code> otherwise.
     */
    public static boolean rectIntersection(CvRect rect1, CvRect rect2) {
        if (rect1.isNull() || rect2.isNull() || rect1.width() <= 0
                || rect1.height() <= 0 || rect2.width() <= 0
                || rect2.height() <= 0) {
            return false;
        }
        int x0 = rect1.x();
        int y0 = rect1.y();
        int w0 = rect1.width();
        int h0 = rect1.height();
        int x1 = rect2.x();
        int y1 = rect2.y();
        int w1 = rect2.width();
        int h1 = rect2.height();

        return (x1 + w1 > x0 && y1 + h1 > y0 && x1 < x0 + w0 && y1 < y0 + h0);
    }

}
