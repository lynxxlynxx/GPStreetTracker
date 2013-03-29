package tracking.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import tracking.gui.controller.StartButtonListener;
import tracking.model.GPSProcessor;
import tracking.model.VideoProcessor;
import tracking.model.plugins.ObjectDetectionPlugin;
import tracking.model.plugins.ResultObject;
import tracking.model.plugins.StreetObject;

public class VideoProcessorFrame extends JFrame implements Observer {

    /**
     * A serialisation ID.
     */
    private static final long serialVersionUID = 5807268329694642881L;

    /**
     * Initial video preview image size.
     */
    private static final Dimension VIDEO_IMAGE_SIZE = new Dimension(420, 280);

    /**
     * Initial found {@link StreetObject} list panel size.
     */
    private static final Dimension LIST_IMAGE_SIZE = new Dimension(80, 50);

    private VideoProcessor model;

    private ImagePanel videoPanel;

    private ImageListPanel imageList;

    private JProgressBar progressBar;

    private JPanel mainPanel;

    private JButton startButton;

    private JScrollPane imageListPane;

    private JMenuBar menubar;

    private LinkedList<ObjectDetectionPlugin> plugins;

    /**
     * Create the frame that shows the actual video frame and a list of images of the found objects
     * 
     * @param model
     *            a {@link VideoProcessor} instance
     */
    public VideoProcessorFrame(final VideoProcessor model) {

        this.model = model;
        this.model.addObserver(this);

        this.plugins = model.getPlugins();

        this.mainPanel = new JPanel();
        this.mainPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        /*
         * Setup image panels
         */
        this.videoPanel = new ImagePanel(VIDEO_IMAGE_SIZE);
        this.videoPanel.setPreferredSize(new Dimension((int) VIDEO_IMAGE_SIZE
                .getWidth() + 20, (int) VIDEO_IMAGE_SIZE.getHeight() + 40));
        // create a new panel for the border in case the paint component method is rotated
        JPanel videoPanelBorder = new JPanel();
        videoPanelBorder.setBorder(new TitledBorder("Video preview"));
        videoPanelBorder.add(videoPanel);

        gbc.gridx = 1;
        gbc.gridy = 1;
        this.mainPanel.add(videoPanelBorder, gbc);

        // setup ImageListPanel in a JScrollPane
        this.imageList = new ImageListPanel(LIST_IMAGE_SIZE);
        this.imageListPane = new JScrollPane(imageList);
        this.imageListPane.setPreferredSize(new Dimension((int) LIST_IMAGE_SIZE
                .getWidth() + 20, (int) VIDEO_IMAGE_SIZE.getHeight() + 40));
        this.imageListPane.setBorder(new TitledBorder("Street Objects"));
        gbc.gridx = 2;
        gbc.gridy = 1;
        this.mainPanel.add(imageListPane, gbc);

        /*
         * Setup Prograss Bar
         */
        this.progressBar = new JProgressBar(0, 100);
        this.progressBar.setValue(0);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.mainPanel.add(progressBar, gbc);

        /*
         * Setup startButton
         */
        this.startButton = new JButton("Start");
        startButton.setActionCommand("start");
        this.startButton.addActionListener(new StartButtonListener(this));
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.mainPanel.add(startButton, gbc);

        // JMenuBar
        menubar = new JMenuBar();
        // Add drawing menu to set the drawIntoImage boolean in every single plugin.
        JMenu menuDrawing = new JMenu("Draw");
        menuDrawing
                .setToolTipText("Enable or disable the plugins to draw into the image");

        // setup drawing checkboxes for all available plugins
        for (final ObjectDetectionPlugin trk : plugins) {
            final JCheckBoxMenuItem itm = new JCheckBoxMenuItem(
                    trk.getPluginName() + " " + trk.getPluginID());
            itm.setToolTipText("Enable or disable the drawing of "
                    + trk.getPluginName() + " " + trk.getPluginID());
            itm.setState(trk.getDrawIntoImage());
            itm.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    trk.setDrawIntoImage(itm.getState());
                }
            });
            menuDrawing.add(itm);
        }
        menuDrawing.addSeparator();

        // add a enable drawing checkbox
        final JCheckBoxMenuItem drawAll = new JCheckBoxMenuItem(
                "Enable drawing");
        drawAll.setToolTipText("Enable or disable the drawing of all plugins");
        drawAll.setState(model.getDrawIntoImage());

        drawAll.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                model.setDrawIntoImage(drawAll.getState());
            }
        });
        menuDrawing.add(drawAll);
        JMenu rotation = new JMenu("Rotation");
        final JCheckBoxMenuItem landscape = new JCheckBoxMenuItem(
                "Landscape/Portrait");
        landscape.setState(true);
        landscape.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                setImageOrientation(landscape.getState());
            }
        });

        final JCheckBoxMenuItem rotationCheckbox = new JCheckBoxMenuItem(
                "Rotate 180°");
        rotationCheckbox.setState(false);
        rotationCheckbox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                setImageRotation(rotationCheckbox.getState());
            }
        });

        rotation.add(landscape);
        rotation.add(rotationCheckbox);
        menubar.add(rotation);
        menubar.add(menuDrawing);

        this.setJMenuBar(menubar);

        // add stuff to this frame
        this.add(mainPanel);
        this.pack();
        this.setLocation(MainFrame.getCenterScreenPosition(this.getSize()));
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    /**
     * Start new thread for the video processor and start the processor.
     */
    public void startButtonPressed() {
        Thread processor = new Thread(model);
        processor.start();
    }

    /**
     * Stop the video processing of the {@link VideoProcessor}.
     */
    public void stopButtonPressed() {
        model.stop();
    }

    /**
     * Start the {@link GPSProcessorFrame} if {@link StreetObject} were found.
     */
    public void nextButtonPressed() {
        LinkedList<StreetObject> list = model.getAllStreetObjects();

        if (list == null || list.isEmpty()) {
            String message = "No street objects were found. \nTry again with another plugin configuration.";
            JOptionPane.showMessageDialog(this, message, "No objects found!",
                    JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); // no objects were found and the programm closes.
        } else {

//            Report.writeResult("C:\\Users\\Philipp\\Desktop\\Report"
//                    + Math.random() * 100 + ".txt"); // XXX
            GPSProcessor gpsProc = new GPSProcessor(list);
            GPSProcessorFrame gesFrame = new GPSProcessorFrame(gpsProc);
            this.dispose();
            gesFrame.setVisible(true);
        }
    }

    /**
     * Show an exit {@link JDialog} when the Frame is closed.
     */
    private void exit() {
        int answer = JOptionPane
                .showConfirmDialog(
                        this,
                        "Are you sure you want to exit? All found objects will be lost!",
                        "", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

        switch (answer) {
        case JOptionPane.YES_OPTION:
            model.stop();
            this.dispose();
        }
    }

    /**
     * Set orientation of the image.
     * 
     * @param orient
     *            true is landscape orientation.
     */
    private void setImageOrientation(boolean orient) {
        videoPanel.setOrientation(orient);
    }

    /**
     * Set rotation of the image.
     * 
     * @param rotation
     *            false is no rotation. true is rotation of 180° in degree.
     */
    private void setImageRotation(boolean rotation) {
        videoPanel.setRotation(rotation);
    }

    /**
     * Refresh the VideoPanel
     */
    @Override
    public void update(Observable videoProcessor, Object res) {

        if (res instanceof ResultObject) {
            ResultObject result = (ResultObject) res;

            this.videoPanel.setImage(result.getDrawImage());

            // release the image of the result object to prevent Memory Leak
            result.releaseImage();

            LinkedList<StreetObject> list = result.getStreetObjects();

            // check if null or empty
            if (list != null && !list.isEmpty()) {
                for (StreetObject obj : list) {
                    // do not release image here, it's needed later in the GPS Processor
                    this.imageList.addImage(obj.getImage());
                }
            }
            this.progressBar.setValue(model.getProgress());
            startButton.setActionCommand("stop");
            startButton.setText("Stop");
        } else if (res instanceof Boolean) {
            // now the video processor has finished and the start button can be changed
            if ((boolean) res == false) {
                startButton.setActionCommand("next");
                startButton.setText("Next");
            }
        }
    }
}
