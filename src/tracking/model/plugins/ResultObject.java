package tracking.model.plugins;

import static com.googlecode.javacv.cpp.opencv_core.cvCloneImage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;

import java.util.LinkedList;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Is returned by an ImageObjectTracker Plugin after the image was processed.
 * 
 * @author Philipp
 */
public class ResultObject {

    private LinkedList<StreetObject> streetObjects;

    private IplImage draImage;

    public ResultObject(LinkedList<StreetObject> streetObjects,
            IplImage drawImage) {
        this.streetObjects = streetObjects;
        this.draImage = cvCloneImage(drawImage);
    }

    public void releaseImage() {
        cvReleaseImage(draImage);
    }

    public LinkedList<StreetObject> getStreetObjects() {
        return streetObjects;
    }

    public IplImage getDrawImage() {
        return draImage;
    }

}
