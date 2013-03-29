package tracking.gui;

import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tracking.gui.layouts.VerticalFlowLayout;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * The class {@link ImageListPanel} is a modified {@link JPanel} to list Images vertical.
 * 
 * @author Philipp Unger
 */
public class ImageListPanel extends JPanel {

    /**
     * A serialisitaion ID.
     */
    private static final long serialVersionUID = -1728637141320082991L;

    Dimension imageSize;

    /**
     * Create a new {@link ImageListPanel}.
     * 
     * @param imageSize
     *            the size of the images listed in the {@link ImageListPanel}.
     */
    public ImageListPanel(Dimension imageSize) {
        this.imageSize = imageSize;
        this.setLayout(new VerticalFlowLayout());
    }

    /**
     * Add an {@link IplImage} to the {@link ImageListPanel}.
     * 
     * @param image the {@link IplImage}.
     */
    public void addImage(IplImage image) {
        IplImage tmpImage = cvCreateImage(
                cvSize(imageSize.width, imageSize.height), image.depth(),
                image.nChannels());

        // Resize image to the size of localImage. In this step localImage now includes the bitmap
        // data of image.
        cvResize(image, tmpImage);
        this.add(new JLabel(new ImageIcon(tmpImage.getBufferedImage())));
        cvReleaseImage(tmpImage);
        this.updateUI();
    }

}
