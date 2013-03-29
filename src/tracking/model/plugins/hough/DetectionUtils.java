package tracking.model.plugins.hough;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvGetRectSubPix;

import java.awt.Point;
import java.util.LinkedList;

import tracking.model.plugins.StreetObject;

import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class DetectionUtils {

    /**
     * Return the center {@link Point} of a {@link CvRect}.
     * 
     * @param boundbox
     * @return
     */
    public static Point getCenter(CvRect boundbox) {
        Point result = new Point();
        result.setLocation(boundbox.x() + (boundbox.width() / 2), boundbox.y()
                + (boundbox.height() / 2));
        return result;
    }

   

    /**
     * Return the roi of the src {@link IplImage} as a croped {@link IplImage}.
     * 
     * @param src
     *            the {@link IplImage} with the roi that has to be extracted
     * @return the croped {@link IplImage}
     */
    public static IplImage getRoiImage(IplImage src) {

        // get the roi region of the image
        CvRect roi = cvGetImageROI(src);

        IplImage dst = cvCreateImage(cvSize(roi.width(), roi.height()),
                src.depth(), src.nChannels());

        // save the ROI area of src to dst
        cvGetRectSubPix(src, dst,
                new CvPoint2D32f((roi.width() / 2), (roi.height() / 2)));

        return dst;
    }

    /**
     * Method to clone {@link LinkedList}<{@link StreetObject}>
     * 
     * @param list
     *            the list to clone
     * @return the clone {@link LinkedList}<{@link StreetObject}>
     */
    public static LinkedList<StreetObject> cloneStreetObjectList(
            LinkedList<StreetObject> list) {
        LinkedList<StreetObject> result = new LinkedList<StreetObject>();
        for (StreetObject obj : list) {
            result.add(obj);
        }
        return result;
    }

}
