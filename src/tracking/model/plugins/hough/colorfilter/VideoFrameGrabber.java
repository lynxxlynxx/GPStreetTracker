package tracking.model.plugins.hough.colorfilter;

import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_FRAME_COUNT;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_FRAME_HEIGHT;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_FRAME_WIDTH;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_POS_FRAMES;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_POS_MSEC;
import static com.googlecode.javacv.cpp.opencv_highgui.cvGetCaptureProperty;
import static com.googlecode.javacv.cpp.opencv_highgui.cvQueryFrame;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSetCaptureProperty;

import java.awt.Dimension;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;

public class VideoFrameGrabber {

    private CvCapture vfCapture = null;

    /**
     * Create a {@link VideoFrameGrabber} for the video <code> video </code>.
     * 
     * @param video
     *            the path of the video file
     */
    public VideoFrameGrabber(CvCapture vfCapture) {
        this.vfCapture = vfCapture;
    }

    /**
     * Return frame at time.
     * 
     * @param time
     *            in msec
     * @return IplImage of the frame
     */
    public IplImage grabFrameAtTime(double time) {
        cvSetCaptureProperty(vfCapture, CV_CAP_PROP_POS_MSEC, time);
        return cvQueryFrame(vfCapture);
    }

    /**
     * Return frame at frame.
     * 
     * @param frames
     *            the number of the frame
     * @return IplImage of the frame
     */
    public IplImage grabFrameAtFrame(double frames) {
        cvSetCaptureProperty(vfCapture, CV_CAP_PROP_POS_FRAMES, frames);
        return cvQueryFrame(vfCapture);
    }

    /**
     * Return the length of the video in frames.
     * 
     * @return the CV_CAP_PROP_FRAME_COUNT of the video
     */
    public int getVideoLength() {
        return (int) cvGetCaptureProperty(vfCapture, CV_CAP_PROP_FRAME_COUNT);
    }

    /**
     * Get the {@link Dimension} of the video.
     * 
     * @return the dimension of the video.
     */
    public Dimension getVideoDimension() {
        return new Dimension((int) cvGetCaptureProperty(vfCapture,
                CV_CAP_PROP_FRAME_HEIGHT), (int) cvGetCaptureProperty(
                vfCapture, CV_CAP_PROP_FRAME_WIDTH));
    }

    /**
     * Release the cvCapture of the video. CvCapture has not to be released anymore, because only
     * one capture is used in the whole framework
     */
    public void releaseCapture() {
        // cvReleaseCapture(vfCapture);

    }

}
