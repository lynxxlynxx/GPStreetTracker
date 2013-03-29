package tracking.model.plugins.hough.colorfilter;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvInRangeS;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_HSV2RGB;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RGB2HSV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Observable;

import tracking.gui.plugins.hough.colorfilter.ColorFilterPanel;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ColorFilter extends Observable {

    private IplImage originalimageHSV;

    private IplImage filteredimage;

    private LinkedList<CvScalar> lows;

    private LinkedList<CvScalar> highs;

    private CvScalar actuallow;

    private CvScalar actualhigh;

    /**
     * Create a color filter for multi scalar filtering. The original image is filtered with all
     * given scalars and then all resulting images are summed up to one image, that only includes
     * the white thresholded parts of all pics.
     * 
     * @param originalimage
     * @param actuallow
     * @param actualhigh
     * @param lows
     * @param highs
     */
    public ColorFilter(IplImage originalimage, CvScalar actuallow,
            CvScalar actualhigh, LinkedList<CvScalar> lows,
            LinkedList<CvScalar> highs) {

        this.originalimageHSV = cvCreateImage(cvGetSize(originalimage),
                IPL_DEPTH_8U, 3);
        pushHSVValue(originalimage, originalimageHSV);
        this.filteredimage = cvCreateImage(cvGetSize(originalimage),
                IPL_DEPTH_8U, 1);
        this.actuallow = actuallow;
        this.actualhigh = actualhigh;
        this.lows = lows;
        this.highs = highs;
        filter(0);
    }

    /**
     * Create blank {@link ColorFilter}.
     * 
     * @param originalimage
     */
    public ColorFilter(IplImage originalimage) {

        this.originalimageHSV = cvCreateImage(cvGetSize(originalimage),
                IPL_DEPTH_8U, 3);
        pushHSVValue(originalimage, originalimageHSV);
        this.filteredimage = cvCreateImage(cvGetSize(originalimage),
                IPL_DEPTH_8U, 1);
        this.actuallow = new CvScalar(255, 255, 255, 255);
        this.actualhigh = new CvScalar(0, 0, 0, 0);
        this.lows = new LinkedList<CvScalar>();
        this.highs = new LinkedList<CvScalar>();

    }

    /**
     * Filter the image with all given parameters and sum the results up into one image. This image
     * can be returned with the getImage() method.
     * 
     * @param the
     *            filtering method. 0 is with actual filter parameters, 1 is only with lows and
     *            highs which are set in setLows() and setHighs().
     */
    public void filter(int method) {

        boolean allright = lows != null && highs != null && !lows.isEmpty()
                && !highs.isEmpty() && lows.size() == highs.size();

        if (method == 0) {
            cvInRangeS(originalimageHSV, actuallow, actualhigh, filteredimage);
        } else if (method == 1 && allright) {
            cvInRangeS(originalimageHSV, lows.get(0), highs.get(0),
                    filteredimage);
        } else {
            return;
        }

        if (allright) {

            ByteBuffer buff1 = filteredimage.getByteBuffer();
            ByteBuffer buff2;
            IplImage tmp = cvCreateImage(cvGetSize(filteredimage),
                    filteredimage.depth(), filteredimage.nChannels());

            for (int i = method; i < lows.size(); i++) { // go through all filters
                cvInRangeS(originalimageHSV, lows.get(i), highs.get(i), tmp);
                buff2 = tmp.getByteBuffer();

                for (int j = 0; j < buff2.capacity(); j++) { // write all pixels that are != 0 into
                                                             // filteredimage
                    byte b = buff2.get(j);
                    if (b != 0) {
                        buff1.put(j, b);
                    }
                }
            }
            cvReleaseImage(tmp);

        }

    }

    /**
     * Set the Value of originalimageHSV to maximum.
     */
    private void pushHSVValue(IplImage src, IplImage dst) {
        cvCvtColor(src, dst, CV_RGB2HSV);
        ByteBuffer buff = dst.getByteBuffer();
        for (int i = 0; i < buff.capacity(); i++) {
            if (i % 3 == 2) {
                buff.put(i, (byte) (255)); // 255 max. unsigned byte value
            }
        }
        cvCvtColor(dst, dst, CV_HSV2RGB);
    }

    /**
     * Return the filtered image.
     * 
     * @return
     */
    public IplImage getImage() {
        return filteredimage;
    }

    /**
     * Set the Image that has to be filtered. It must have the same size as the initial image.
     * 
     * @param image
     *            the new {@link IplImage}.
     */
    public void setImage(IplImage image) {
        pushHSVValue(image, originalimageHSV);
    }

    /**
     * Set the Image that has to be filtered. It will be resized to the initial images size.
     * 
     * @param image
     *            the new {@link IplImage}.
     */
    public void setImageR(IplImage image) {
        IplImage tmp = cvCreateImage(cvGetSize(originalimageHSV),
                image.depth(), image.nChannels());
        cvResize(image, tmp);
        pushHSVValue(tmp, originalimageHSV);
        cvReleaseImage(tmp);
    }

    /**
     * Return the low {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @return
     */
    public CvScalar getLowScalar() {
        return actuallow;
    }

    /**
     * Return the high {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @return
     */
    public CvScalar getHighScalar() {
        return actualhigh;
    }

    /**
     * Set the actuallow {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param scalar
     */
    public void setLowScalar(CvScalar scalar) {
        actuallow = scalar;
    }

    /**
     * Set the actualhigh {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param scalar
     */
    public void setHighScalar(CvScalar scalar) {
        actualhigh = scalar;
    }

    /**
     * Set the blue value of actuallow {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param value
     */
    public void setLowBlue(double value) {
        actuallow.setVal(0, value);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the green value of actuallow {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param value
     */
    public void setLowGreen(double value) {
        actuallow.setVal(1, value);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the red value of actuallow {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param value
     */
    public void setLowRed(double value) {
        actuallow.setVal(2, value);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the alpha value of actuallow {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param value
     */
    public void setLowAlpha(double value) {
        actuallow.setVal(3, value);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the blue value of actualhigh {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param value
     */
    public void setHighBlue(double value) {
        actualhigh.setVal(0, value);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the green value of actualhigh {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param value
     */
    public void setHighGreen(double value) {
        actualhigh.setVal(1, value);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the red value of actualhigh {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param value
     */
    public void setHighRed(double value) {
        actualhigh.setVal(2, value);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the alpha value of actualhigh {@link CvScalar}. Called in the {@link ColorFilterPanel}.
     * 
     * @param value
     */
    public void setHighAlpha(double value) {
        actualhigh.setVal(3, value);
        setChanged();
        notifyObservers();
    }

    /**
     * Set the local low scalar list for pre filtering.
     * 
     * @param lows
     */
    public void setLows(LinkedList<CvScalar> lows) {
        this.lows = lows;
    }

    /**
     * Set the local high scalar list for pre filtering.
     * 
     * @param highs
     */
    public void setHighs(LinkedList<CvScalar> highs) {
        this.highs = highs;
    }

}
