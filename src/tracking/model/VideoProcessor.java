package tracking.model;

import static com.googlecode.javacv.cpp.opencv_core.cvCloneImage;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_FPS;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_FRAME_COUNT;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_POS_FRAMES;
import static com.googlecode.javacv.cpp.opencv_highgui.CV_CAP_PROP_POS_MSEC;
import static com.googlecode.javacv.cpp.opencv_highgui.cvGetCaptureProperty;
import static com.googlecode.javacv.cpp.opencv_highgui.cvQueryFrame;
import static com.googlecode.javacv.cpp.opencv_highgui.cvReleaseCapture;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSetCaptureProperty;

import java.io.File;
import java.util.LinkedList;
import java.util.Observable;

import tracking.gui.VideoProcessorFrame;
import tracking.model.plugins.ObjectDetectionPlugin;
import tracking.model.plugins.ResultObject;
import tracking.model.plugins.StreetObject;

import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;

public class VideoProcessor extends Observable implements Runnable {

    /**
     * If <code>false</code> drawing in the video frame that is shown in the gui is deactivated.
     */
    private boolean drawIntoImage;

    /**
     * A {@link LinkedList} of all found street objects.
     */
    private LinkedList<StreetObject> allStreetObjects;

    /**
     * Position of the actual frame as {@link Integer};
     */
    private int actualFrame;

    /**
     * {@link CvCapture} can capture single frames from the viedo and holds informations about video
     * properties.
     */
    private CvCapture capture = null;

    /**
     * The length of the video in frames. Needed for the actual position method, because
     * CV_CAP_PROP_POS_AVI_RATIO seems not to work on mp4 files.
     */
    private double lengthInFrames;

    /**
     * {@link Boolean} to stop the thread.
     */
    private boolean running;

    /**
     * A {@link LinkedList} with the plugins for tracking objects in the video
     */
    private LinkedList<ObjectDetectionPlugin> plugins;

    /**
     * @param video
     *            a video {@link File}
     * @param plugins
     *            a {@link LinkedList} of {@link ObjectDetectionPlugin}s.
     */
    public VideoProcessor(CvCapture videoCapture,
            LinkedList<ObjectDetectionPlugin> plugins) {

        this.plugins = plugins;
        this.drawIntoImage = true;
        this.running = true;
        this.actualFrame = 0;
        this.allStreetObjects = new LinkedList<StreetObject>();
        this.capture = videoCapture;
        cvSetCaptureProperty(capture, CV_CAP_PROP_POS_FRAMES, 0);

    }

    /**
     * Grab every frame from the video process it in all plugins one after another.
     */
    public void run() {

        this.lengthInFrames = cvGetCaptureProperty(capture,
                CV_CAP_PROP_FRAME_COUNT);

        // HIER NUR REFERENZ AUF DEN GRABBER

        IplImage sizeAndDepth = cvQueryFrame(capture);

        IplImage original = cvCreateImage(cvGetSize(sizeAndDepth),
                sizeAndDepth.depth(), sizeAndDepth.nChannels());
        IplImage drawable = cvCreateImage(cvGetSize(sizeAndDepth),
                sizeAndDepth.depth(), sizeAndDepth.nChannels());

        System.out.println("Strat Processing...");
        System.out.println("Frames to grab: " + lengthInFrames);

        // temporary found street objects
        LinkedList<StreetObject> tempAllStreetObjList;

        // -2 because lengthInFrames variies +/-2 sometimes..
        for (int i = 0; i < lengthInFrames - 2 && running; i++) {
//            Report.startProcess(); // XXX

            IplImage releaserOriginal = original;
            original = cvCloneImage(cvQueryFrame(capture));
            cvReleaseImage(releaserOriginal); // could not be referenced. clone image and release
                                              // the original.

            IplImage releaserDrawable = drawable;
            drawable = cvCloneImage(original);
            cvReleaseImage(releaserDrawable);

            tempAllStreetObjList = new LinkedList<StreetObject>();
            actualFrame = i;

            // go through all ImageObjectTracker plugins and get the resultObjects.
            for (ObjectDetectionPlugin plugin : plugins) {

                // local result object
                ResultObject tempResObject = plugin.process(original, drawable);

                // actualize drawable image for next plugin
                if (tempResObject.getDrawImage() != null) {

                    // deallocate the reference from the image in drawable
                    IplImage releaserDra = drawable;
                    // clone image to release the tempResultObject
                    drawable = cvCloneImage(tempResObject.getDrawImage());
                    cvReleaseImage(releaserDra);
                    tempResObject.releaseImage();
                }

                // store StreetObjects
                LinkedList<StreetObject> tempStreetObjects = tempResObject
                        .getStreetObjects();

                if (tempStreetObjects != null && !tempStreetObjects.isEmpty()) {
                    // set the actual frame porsition in every street object.
                    for (StreetObject obj : tempStreetObjects) {
                        obj.setFrame(i);
                        obj.setTimestamp(cvGetCaptureProperty(capture,
                                CV_CAP_PROP_POS_MSEC));
                        obj.setVideoFPS(cvGetCaptureProperty(capture,
                                CV_CAP_PROP_FPS));
                    }
                    tempAllStreetObjList.addAll(tempStreetObjects);

                }
            }

            ResultObject result;

            if (drawIntoImage) {
                result = new ResultObject(tempAllStreetObjList, drawable);

            } else {
                result = new ResultObject(tempAllStreetObjList, original);
            }

            // add found street objects to allStreetObjects list
            allStreetObjects.addAll(tempAllStreetObjList);

            // notify observer with the actual image and the found objects
            setChanged();
            notifyObservers(result);
            // Report.endProcess(); // XXX
        }

        // notify observer processor has finished
        setChanged();
        notifyObservers(false);

        cvReleaseCapture(capture);
        cvReleaseImage(original);
        cvReleaseImage(drawable);

    }

    /**
     * Returns progress from 0% - 100%
     * 
     * @return progress in %
     */
    public int getProgress() {
        // -3 because length in frames variies from +/-2 sometimes..
        return lengthInFrames - 3 <= actualFrame ? 100 : (int) ((double) 100
                / lengthInFrames * actualFrame);
    }

    /**
     * Return all found {@link StreetObject} in a {@link LinkedList}.
     * 
     * @return {@link LinkedList} of {@link StreetObject}s
     */
    public LinkedList<StreetObject> getAllStreetObjects() {
        return allStreetObjects;
    }

    /**
     * Stop the video processing.
     */
    public void stop() {
        running = false;
    }

    /**
     * Retrurns the list of {@link ObjectDetectionPlugin} plugins. This method is used in the
     * {@link VideoProcessorFrame} for setting the drawIntoImage(boolean) method in the
     * {@link ObjectDetectionPlugin}s.
     * 
     * @return
     */
    public LinkedList<ObjectDetectionPlugin> getPlugins() {
        return plugins;
    }

    /**
     * Enable global drawing.
     * 
     * @param draw
     *            if false, no plugin is drawing in the output image.
     */
    public void setDrawIntoImage(boolean draw) {
        drawIntoImage = draw;
    }

    /**
     * Return if drawing is allowed.
     * 
     * @return true, if plugins are drawing into the output image, false otherwise.
     */
    public boolean getDrawIntoImage() {
        return drawIntoImage;
    }
}
