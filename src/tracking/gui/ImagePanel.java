package tracking.gui;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSize;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.Transient;

import javax.swing.JPanel;

import com.googlecode.javacv.cpp.opencv_core.CvSize;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class ImagePanel extends JPanel {

    /**
     * A serialisation ID.
     */
    private static final long serialVersionUID = 8254440253038010799L;

    /**
     * The {@link IplImage} shown in the {@link ImagePanel}.
     */
    private IplImage image;

    /**
     * The image ratio as double value.
     */
    private double imageRatio;

    /**
     * If true, the image is rotated 180 degree.
     */
    private boolean rotation;

    /**
     * If false, the image is in portrait mode and rotated by 90 degree.
     */
    private boolean landscape;

    /**
     * Create a new {@link ImagePanel}.
     * 
     * @param dimension
     *            {@link Dimension} of the {@link ImagePanel}
     */
    public ImagePanel(Dimension dimension) {
        CvSize size = new CvSize((int) dimension.getWidth(),
                (int) dimension.getHeight());
        image = cvCreateImage(size, IPL_DEPTH_8U, 1);
        imageRatio = (double) size.width() / (double) size.height();
        landscape = true;
        rotation = false;
        // this.add(new JLabel(new ImageIcon(image.getBufferedImage())));
    }

    /**
     * Set the image in the {@link ImagePanel}. It's resized and the {@link JPanel} updates itself.
     * 
     * @param newimage
     *            the new {@link IplImage}
     */
    public void setImage(IplImage newimage) {

        if (image.depth() != newimage.depth()
                || image.nChannels() != newimage.nChannels()) {
            // release the old image when the new one is created
            IplImage releaseImage = image;
            image = cvCreateImage(cvGetSize(image), newimage.depth(),
                    newimage.nChannels());
            cvReleaseImage(releaseImage);
        }

        cvResize(newimage, image);
        // this.removeAll();
        // this.add(new JLabel(new ImageIcon(image.getBufferedImage())));
        this.updateUI();
    }

    @Override
    @Transient
    public Dimension getPreferredSize() {
        if (landscape) {
            return new Dimension(image.width(), image.height());
        } else {
            return new Dimension(image.height(), image.width());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphic = (Graphics2D) g;
        double rotation;
        int x;
        int y;
        if (!this.rotation && landscape) {
            rotation = 0;
            x = 0;
            y = 0;
        } else if (!this.rotation && !landscape) {
            rotation = Math.PI / 2;
            x = image.height() / 2;
            y = image.height() / 2;
        } else if (this.rotation && landscape) {
            rotation = Math.PI;
            x = image.width() / 2;
            y = image.height() / 2;
        } else {
            rotation = Math.PI / 2;
            x = image.height() / 2;
            y = image.height() / 2;
            graphic.rotate(rotation, x, y);
            rotation = Math.PI;
            x = image.width() / 2;
            y = image.height() / 2;
        }
        graphic.rotate(rotation, x, y);
        graphic.drawImage(image.getBufferedImage(), 0, 0, null);
    }

    /**
     * Resize the image in the {@link ImagePanel}.
     * 
     * @param width
     *            The image width. Height is set in ratio.
     */
    public void setImageSize(int width) {
        IplImage releaseImage = image;
        if (landscape) {
            image = cvCreateImage(cvSize(width, (int) (width / imageRatio)),
                    releaseImage.depth(), releaseImage.nChannels());
        } else {
            image = cvCreateImage(
                    cvSize((int) (width / imageRatio), (int) (width)),
                    releaseImage.depth(), releaseImage.nChannels());
        }
        cvResize(releaseImage, image);
        cvReleaseImage(releaseImage);
        // this.removeAll();
        // this.add(new JLabel(new ImageIcon(image.getBufferedImage())));
        this.updateUI();
    }

    /**
     * Set orientation of the image.
     * 
     * @param landscape
     *            true is landscape mode, false portrait mode.
     */
    public void setOrientation(boolean landscape) {
        this.landscape = landscape;
        if (!landscape) {
            setImageSize(image.width());
        } else {
            setImageSize(image.height());
        }
        this.updateUI();

    }

    /**
     * Set rotarion of the image.
     * 
     * @param rotation
     *            if true, image is rotated 180 degree.
     */
    public void setRotation(boolean rotation) {
        this.rotation = rotation;
        this.updateUI();
    }

}
