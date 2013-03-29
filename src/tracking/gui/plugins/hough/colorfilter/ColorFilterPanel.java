package tracking.gui.plugins.hough.colorfilter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tracking.gui.ImagePanel;
import tracking.gui.plugins.hough.colorfilter.controller.ColorFilterSliderListener;
import tracking.gui.plugins.hough.colorfilter.controller.ColorFilterTextFieldListener;
import tracking.gui.plugins.hough.colorfilter.controller.FrameGrabberSliderListener;
import tracking.model.plugins.hough.HoughSignRecognition;
import tracking.model.plugins.hough.colorfilter.ColorFilter;
import tracking.model.plugins.hough.colorfilter.VideoFrameGrabber;

import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;

public class ColorFilterPanel extends JPanel implements Observer {

    /**
     * A serialisation ID.
     */
    private static final long serialVersionUID = -4670079717035124981L;

    /**
     * The size of the video preview image. Only the width can be set here, the height is set
     * relatively to the video.
     */
    private static final int IMAGE_WIDTH = 300;

    /**
     * Enum for the {@link JSlider} ActionCommand.
     * 
     * @author Philipp
     */
    public static enum Slider {
        LOWER_BLUE, LOWER_GREEN, LOWER_RED, LOWER_ALPHA, UPPER_BLUE,
        UPPER_GREEN, UPPER_RED, UPPER_ALPHA;
    }

    /**
     * Double values for the color thresholds.
     */
    private double lowerBlueComponent, lowerGreenComponent, lowerRedComponent,
            lowerAlpha, upperBlueComponent, upperGreenComponent,
            upperRedComponent, upperAlpha;

    /**
     * {@link JSlider} for the color thresholds.
     */
    private JSlider lowerBlueSlider, lowerGreenSlider, lowerRedSlider,
            upperBlueSlider, upperGreenSlider, upperRedSlider;

    /**
     * {@link JSlider} to choose the position in the video.
     */
    private JSlider videoFrameSlider;

    private JLabel videoCurrentTime, videoMaxTime;

    /**
     * {@link JTextField} to set the color thresholds.
     */
    private JTextField lowerBlueValue, lowerGreenValue, lowerRedValue,
            upperBlueValue, upperGreenValue, upperRedValue;

    /**
     * Text {@link JLabel} Lower Limit
     */
    private static final JLabel LOWER_LIMIT = new JLabel("Lower Limit");
    private static final JLabel UPPER_LIMIT = new JLabel("Upper Limit");
    private static final JLabel BLUE = new JLabel("Blue");
    private static final JLabel GREEN = new JLabel("Green");
    private static final JLabel RED = new JLabel("Red");

    // private static final JLabel ALPHA = new JLabel("Alpha"); only needed when video has an alpha
    // channel.

    private static final JLabel FRAMES = new JLabel("Frames");

    private static final int MAX_VALUE = 255;
    private static final int MIN_VALUE = 0;
    private static final int START_VALUE = 0;

    private JPanel filterpanel;

    // ###########################################

    private ColorFilter cfilter;
    private ImagePanel originalimage;
    private ImagePanel filteredimage;
    private VideoFrameGrabber vfgrabber;

    // ###########################################

    private HoughSignRecognition model;

    private DefaultListModel<String> scalarList;

    /**
     * {@link JButton}s to add or remove scalar settings.
     */
    private JButton addScalar, removeScalar;

    /**
     * Shows if all filters are shown in the filter preview.
     */
    private JCheckBox sumup;

    /**
     * Local scalar list. List of all set scalars.
     */
    private LinkedList<CvScalar> localHighScalars, localLowScalars;

    /**
     * Shows if all filters are shown in the filter preview.
     */
    public boolean sumUp;

    /**
     * Create a new {@link ColorFilterPanel} for the Plugin {@link HoughSignRecognition}.
     * 
     * @param pluginModel
     *            the plugin {@link HoughSignRecognition}
     */
    public ColorFilterPanel(HoughSignRecognition pluginModel) {

        this.model = pluginModel;

        /*
         * Set Scalar Component Start Values
         */
        lowerBlueComponent = START_VALUE;
        lowerGreenComponent = START_VALUE;
        lowerRedComponent = START_VALUE;
        lowerAlpha = START_VALUE;
        upperBlueComponent = MAX_VALUE - START_VALUE;
        upperGreenComponent = MAX_VALUE - START_VALUE;
        upperRedComponent = MAX_VALUE - START_VALUE;
        upperAlpha = MAX_VALUE - START_VALUE;

        /*
         * Set Text Fields
         */
        lowerBlueValue = new JTextField("" + lowerBlueComponent);
        lowerGreenValue = new JTextField("" + lowerGreenComponent);
        lowerRedValue = new JTextField("" + lowerRedComponent);
        // lowerAlphaValue = new JTextField("" + lowerAlpha);
        upperBlueValue = new JTextField("" + upperBlueComponent);
        upperGreenValue = new JTextField("" + upperGreenComponent);
        upperRedValue = new JTextField("" + upperRedComponent);
        // upperAlphaValue = new JTextField("" + upperAlpha);
        // Video Time
        videoCurrentTime = new JLabel();
        videoMaxTime = new JLabel();

        /*
         * Set Sliders
         */
        lowerBlueSlider = new JSlider(MIN_VALUE, MAX_VALUE, START_VALUE);
        lowerGreenSlider = new JSlider(MIN_VALUE, MAX_VALUE, START_VALUE);
        lowerRedSlider = new JSlider(MIN_VALUE, MAX_VALUE, START_VALUE);
        // lowerAlphaSlider = new JSlider(MIN_VALUE, MAX_VALUE, START_VALUE);
        upperBlueSlider = new JSlider(MIN_VALUE, MAX_VALUE, MAX_VALUE
                - START_VALUE);
        upperGreenSlider = new JSlider(MIN_VALUE, MAX_VALUE, MAX_VALUE
                - START_VALUE);
        upperRedSlider = new JSlider(MIN_VALUE, MAX_VALUE, MAX_VALUE
                - START_VALUE);
        // upperAlphaSlider = new JSlider(MIN_VALUE, MAX_VALUE, MAX_VALUE
        // - START_VALUE);

        CvScalar lowerscalar = new CvScalar(lowerBlueComponent,
                lowerGreenComponent, lowerRedComponent, lowerAlpha);

        CvScalar upperscalar = new CvScalar(upperBlueComponent,
                upperGreenComponent, upperRedComponent, upperAlpha);

        vfgrabber = new VideoFrameGrabber(model.getVideoCapture());

        cfilter = new ColorFilter(vfgrabber.grabFrameAtFrame(1), lowerscalar,
                upperscalar, new LinkedList<CvScalar>(),
                new LinkedList<CvScalar>());

        cfilter.addObserver(this);

        /*
         * Setting Video Sliders and Labels
         */
        int videoLength = vfgrabber.getVideoLength() - 1;
        videoFrameSlider = new JSlider(0, videoLength, 0);
        videoFrameSlider.setMinorTickSpacing(0);
        videoFrameSlider.setMajorTickSpacing(videoLength / 10);
        videoFrameSlider.setPaintTicks(true);
        videoFrameSlider.setPaintLabels(true);
        videoCurrentTime.setText("0");
        videoMaxTime.setText("" + (videoLength));

        /*
         * Add GUI elements to Panel filterpanel
         */
        filterpanel = new JPanel();
        filterpanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        /*
         * 1st column lower sliders
         */
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;
        gbc.gridx = 0;
        gbc.gridy = 0;
        filterpanel.add(LOWER_LIMIT, gbc);
        gbc.gridy = 1;
        filterpanel.add(lowerBlueSlider, gbc);
        gbc.gridy = 2;
        filterpanel.add(lowerGreenSlider, gbc);
        gbc.gridy = 3;
        filterpanel.add(lowerRedSlider, gbc);
        // gbc.gridy = 4;
        // filterpanel.add(lowerAlphaSlider, gbc);

        /*
         * 2nd column lower value fields
         */
        gbc.gridx = 1;
        gbc.gridy = 1;
        filterpanel.add(lowerBlueValue, gbc);
        gbc.gridy = 2;
        filterpanel.add(lowerGreenValue, gbc);
        gbc.gridy = 3;
        filterpanel.add(lowerRedValue, gbc);
        // gbc.gridy = 4;
        // filterpanel.add(lowerAlphaValue, gbc);

        /*
         * 3rd column names
         */
        gbc.fill = GridBagConstraints.NONE;;
        gbc.gridx = 2;
        gbc.gridy = 1;
        filterpanel.add(BLUE, gbc);
        gbc.gridy = 2;
        filterpanel.add(GREEN, gbc);
        gbc.gridy = 3;
        filterpanel.add(RED, gbc);
        // gbc.gridy = 4;
        // filterpanel.add(ALPHA, gbc);

        /*
         * 4th column upper value fields
         */
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 3;
        gbc.gridy = 1;
        filterpanel.add(upperBlueValue, gbc);
        gbc.gridy = 2;
        filterpanel.add(upperGreenValue, gbc);
        gbc.gridy = 3;
        filterpanel.add(upperRedValue, gbc);
        // gbc.gridy = 4;
        // filterpanel.add(upperAlphaValue, gbc);

        /*
         * 5th column upper sliders
         */
        gbc.gridx = 4;
        gbc.gridy = 0;
        filterpanel.add(UPPER_LIMIT, gbc);
        gbc.gridy = 1;
        filterpanel.add(upperBlueSlider, gbc);
        gbc.gridy = 2;
        filterpanel.add(upperGreenSlider, gbc);
        gbc.gridy = 3;
        filterpanel.add(upperRedSlider, gbc);
        // gbc.gridy = 4;
        // filterpanel.add(upperAlphaSlider, gbc);

        /*
         * Add Image Panels
         */
        gbc.fill = GridBagConstraints.NONE;;
        gbc.gridx = 0;
        gbc.gridy = 5;
        Dimension videoDimension = vfgrabber.getVideoDimension();
        int imageHeight = (int) (videoDimension.getHeight() / (videoDimension
                .getWidth() / IMAGE_WIDTH));

        originalimage = new ImagePanel(new Dimension(imageHeight, IMAGE_WIDTH));
        filterpanel.add(originalimage, gbc);
        gbc.gridx = 4;
        filteredimage = new ImagePanel(new Dimension(imageHeight, IMAGE_WIDTH));
        filterpanel.add(filteredimage, gbc);

        /*
         * Add Video Time Labels
         */
        gbc.gridx = 0;
        gbc.gridy = 6;
        filterpanel.add(videoCurrentTime, gbc);
        gbc.gridx = 2;
        gbc.gridy = 6;
        filterpanel.add(FRAMES, gbc);
        gbc.gridx = 4;
        gbc.gridy = 6;
        filterpanel.add(videoMaxTime, gbc);

        /*
         * Add Video Slider
         */
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 5;
        filterpanel.add(videoFrameSlider, gbc);

        /*
         * Set Listeners for Sliders
         */
        lowerBlueSlider.addMouseMotionListener(new ColorFilterSliderListener(
                cfilter, lowerBlueValue, Slider.LOWER_BLUE));
        lowerGreenSlider.addMouseMotionListener(new ColorFilterSliderListener(
                cfilter, lowerGreenValue, Slider.LOWER_GREEN));
        lowerRedSlider.addMouseMotionListener(new ColorFilterSliderListener(
                cfilter, lowerRedValue, Slider.LOWER_RED));
        // lowerAlphaSlider.addMouseMotionListener(new ColorFilterSliderListener(
        // cfilter, lowerAlphaValue, Slider.LOWER_ALPHA));
        upperBlueSlider.addMouseMotionListener(new ColorFilterSliderListener(
                cfilter, upperBlueValue, Slider.UPPER_BLUE));
        upperGreenSlider.addMouseMotionListener(new ColorFilterSliderListener(
                cfilter, upperGreenValue, Slider.UPPER_GREEN));
        upperRedSlider.addMouseMotionListener(new ColorFilterSliderListener(
                cfilter, upperRedValue, Slider.UPPER_RED));
        // upperAlphaSlider.addMouseMotionListener(new ColorFilterSliderListener(
        // cfilter, upperAlphaValue, Slider.UPPER_ALPHA));

        // Video Frame Slider
        videoFrameSlider.addMouseMotionListener(new FrameGrabberSliderListener(
                this));

        /*
         * Set Listeners for Textfields
         */
        lowerBlueValue.addActionListener(new ColorFilterTextFieldListener(
                cfilter, lowerBlueSlider, Slider.LOWER_BLUE));
        lowerGreenValue.addActionListener(new ColorFilterTextFieldListener(
                cfilter, lowerGreenSlider, Slider.LOWER_GREEN));
        lowerRedValue.addActionListener(new ColorFilterTextFieldListener(
                cfilter, lowerRedSlider, Slider.LOWER_RED));
        // lowerAlphaValue.addActionListener(new ColorFilterTextFieldListener(
        // cfilter, lowerAlphaSlider, Slider.LOWER_ALPHA));
        upperBlueValue.addActionListener(new ColorFilterTextFieldListener(
                cfilter, upperBlueSlider, Slider.UPPER_BLUE));
        upperGreenValue.addActionListener(new ColorFilterTextFieldListener(
                cfilter, upperGreenSlider, Slider.UPPER_GREEN));
        upperRedValue.addActionListener(new ColorFilterTextFieldListener(
                cfilter, upperRedSlider, Slider.UPPER_RED));
        // upperAlphaValue.addActionListener(new ColorFilterTextFieldListener(
        // cfilter, upperAlphaSlider, Slider.UPPER_ALPHA));

        // ##########################LIST##################
        /*
         * Setup the filterlist and the add and remove button
         */
        this.localLowScalars = new LinkedList<CvScalar>();
        this.localHighScalars = new LinkedList<CvScalar>();
        this.scalarList = new DefaultListModel<String>();
        final JList<String> list = new JList<String>(scalarList);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 100));

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 4;
        gbc.gridheight = 3;
        filterpanel.add(listScroller, gbc);

        CvScalar low;
        CvScalar high;

        // Add last set scalars to list if available
        for (int i = model.getHighScalars().size() - 1; i >= 0; i--) {
            low = cloneCvScalar(model.getLowScalars().get(i));
            high = cloneCvScalar(model.getHighScalars().get(i));

            addScalarsToScalarList(low, high);
            localLowScalars.addFirst(low);
            localHighScalars.addFirst(high);

        }

        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new GridBagLayout());

        /*
         * Setup the Add button
         */
        this.addScalar = new JButton("Add");

        this.addScalar.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                CvScalar low = cloneCvScalar(cfilter.getLowScalar());
                CvScalar high = cloneCvScalar(cfilter.getHighScalar());

                // add scalars to the local list in the user interface
                addScalarsToScalarList(low, high);

                // add scalars to local Scalar Lists
                localLowScalars.addFirst(low);
                localHighScalars.addFirst(high);

                if (sumUp) {
                    cfilter.setLows(localLowScalars);
                    cfilter.setHighs(localHighScalars);
                }

                // set selected index at the new added String
                list.setSelectedIndex(0);
            }
        });

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        buttonpanel.add(addScalar, gbc);

        /*
         * Setup the Remove button
         */
        this.removeScalar = new JButton("Remove");

        this.removeScalar.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int index = list.getSelectedIndex();
                if (index != -1) {

                    scalarList.remove(index);
                    localHighScalars.remove(index);
                    localLowScalars.remove(index);

                    if (sumUp) {
                        cfilter.setLows(localLowScalars);
                        cfilter.setHighs(localHighScalars);
                    }

                    if (scalarList.getSize() != 0) {
                        if (index != 0) {
                            list.setSelectedIndex(index - 1);
                            list.ensureIndexIsVisible(index - 1);
                        } else {
                            list.setSelectedIndex(0);
                            list.ensureIndexIsVisible(0);
                        }
                    }
                }
            }
        });

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        buttonpanel.add(removeScalar, gbc);

        // Summing up filtered images Checkbox:
        sumup = new JCheckBox();
        sumup.setSelected(false);
        sumUp = false;
        sumup.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                sumUp = sumup.isSelected();

                // if sum up is selected, sum up all filters, reset filters otherwise
                if (sumUp) {
                    cfilter.setLows(localLowScalars);
                    cfilter.setHighs(localHighScalars);
                } else {
                    cfilter.setLows(new LinkedList<CvScalar>());
                    cfilter.setHighs(new LinkedList<CvScalar>());
                }

                cfilter.filter(0);
                filteredimage.setImage(cfilter.getImage());
                filteredimage.updateUI();
            }
        });

        // add text and checkbox to a little panel and add it to the filterpanel
        JPanel checkboxpanel = new JPanel();
        checkboxpanel.setLayout(new FlowLayout());
        JLabel checkboxtext = new JLabel("All filters");
        checkboxpanel.add(checkboxtext);
        checkboxpanel.add(sumup);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        buttonpanel.add(checkboxpanel, gbc);

        JButton standardRed = new JButton("Add standard red");
        standardRed.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CvScalar low = new CvScalar(9, 0, 0, 0);
                CvScalar high = new CvScalar(194, 170, 255, 0);
                // add scalars to the local list in the user interface
                addScalarsToScalarList(low, high);

                // add scalars to local Scalar Lists
                localLowScalars.addFirst(low);
                localHighScalars.addFirst(high);

                if (sumUp) {
                    cfilter.setLows(localLowScalars);
                    cfilter.setHighs(localHighScalars);
                    cfilter.filter(0);
                    filteredimage.setImage(cfilter.getImage());
                    filteredimage.updateUI();
                }

            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        buttonpanel.add(standardRed, gbc);

        JButton standardBlue = new JButton("Add standard blue");
        standardBlue.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CvScalar low = new CvScalar(0, 7, 0, 0);
                CvScalar high = new CvScalar(255, 162, 121, 0);
                // add scalars to the local list in the user interface
                addScalarsToScalarList(low, high);

                // add scalars to local Scalar Lists
                localLowScalars.addFirst(low);
                localHighScalars.addFirst(high);

                if (sumUp) {
                    cfilter.setLows(localLowScalars);
                    cfilter.setHighs(localHighScalars);
                    cfilter.filter(0);
                    filteredimage.setImage(cfilter.getImage());
                    filteredimage.updateUI();
                }

            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        buttonpanel.add(standardBlue, gbc);

        JButton standardYellow = new JButton("Add standard yellow");
        standardYellow.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {

                CvScalar low = new CvScalar(0, 198, 235, 0);
                CvScalar high = new CvScalar(133, 255, 255, 0);
                // add scalars to the local list in the user interface
                addScalarsToScalarList(low, high);

                // add scalars to local Scalar Lists
                localLowScalars.addFirst(low);
                localHighScalars.addFirst(high);

                if (sumUp) {
                    cfilter.setLows(localLowScalars);
                    cfilter.setHighs(localHighScalars);
                    cfilter.filter(0);
                    filteredimage.setImage(cfilter.getImage());
                    filteredimage.updateUI();
                }

            }
        });
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        buttonpanel.add(standardYellow, gbc);

        // add buttonpanel to filterpanel
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 4;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        filterpanel.add(buttonpanel, gbc);

        // this.okButton = new JButton("Ok");
        //
        // this.okButton.addActionListener(new ActionListener() {
        //
        // @Override
        // public void actionPerformed(ActionEvent arg0) {
        // model.setScalarLists(localLowScalars, localHighScalars);
        // dispose();
        // }
        // });
        //
        // gbconstraints.fill = GridBagConstraints.NONE;
        // gbconstraints.anchor = GridBagConstraints.CENTER;
        // gbconstraints.gridx = 4;
        // gbconstraints.gridy = 10;
        // gbconstraints.gridwidth = 1;
        // gbconstraints.gridheight = 1;
        //
        // filterpanel.add(okButton, gbconstraints);

        IplImage image = vfgrabber.grabFrameAtFrame(0);
        if (image != null) {
            originalimage.setImage(image);
            filteredimage.setImage(cfilter.getImage());
        } else
            System.err
                    .println("Error in ColorFilterFrame: IplImage at video frame 0 is null");

        /*
         * Add Panel filterpanel to Frame ColorFilterFrame colorFilterFrame verwenden, der nun ein
         * JPanel ist.
         */
        this.setLayout(new BorderLayout());
        this.add(filterpanel);
    }

    /**
     * Save scalars to HoughSignRecognition model, when the ok button is pressed.
     */
    public void okButtonPressed() {
        model.setScalarLists(localLowScalars, localHighScalars);
    }

    /**
     * Release the {@link CvCapture} of the {@link VideoFrameGrabber}.
     */
    public void releaseCapture() {
        vfgrabber.releaseCapture();
    }

    /**
     * Private Method to clone CvScalars.
     * 
     * @param scalar
     *            the CvScalar to clone
     * @return the clone
     */
    private CvScalar cloneCvScalar(CvScalar scalar) {
        return new CvScalar(scalar.getVal(0), scalar.getVal(1),
                scalar.getVal(2), scalar.getVal(3));
    }

    /**
     * Add filter scalars to the scalar list.
     * 
     * @param lo
     *            the lower {@link CvScalar}
     * @param up
     *            the upper {@link CvScalar}
     */
    private void addScalarsToScalarList(CvScalar lo, CvScalar up) {
        scalarList.add(0, lo.toString() + "  " + up.toString());
    }

    /**
     * Set filterimage to frame.
     * 
     * @param frame
     *            the new frame postition.
     */
    public void setFilterImageAtFrame(int frame) {
        IplImage image = vfgrabber.grabFrameAtFrame(frame);
        originalimage.setImage(image);
        cfilter.setImage(image);
        cfilter.filter(0);
        filteredimage.setImage(cfilter.getImage());
        videoCurrentTime.setText("" + frame);
    }

    /**
     * Set size of the image panels. Only image width can be set, the height is in ratio.
     * 
     * @param width
     */
    public void setImagePanelSize(int width) {
        originalimage.setImageSize(width);
        filteredimage.setImageSize(width);
    }

    /**
     * Set the orientation of the {@link ImagePanel}s.
     * 
     * @param landscape
     *            <code> true </code> is for landscape orientation.
     */
    public void setImagePanelOrientation(boolean landscape) {
        originalimage.setOrientation(landscape);
        filteredimage.setOrientation(landscape);
    }

    /**
     * Set the rotation of the image.
     * 
     * @param rotation
     *            true is rotation of 180° in degree. false is no rotation.
     */
    public void setImagePanelRotation(boolean rotation) {
        originalimage.setRotation(rotation);
        filteredimage.setRotation(rotation);
    }

    @Override
    public void update(Observable o, Object arg) {
        cfilter.filter(0);
        IplImage img = cfilter.getImage();
        filteredimage.setImage(img);
        filteredimage.updateUI();
    }

}
