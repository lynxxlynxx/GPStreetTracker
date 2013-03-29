package tracking.gui;

import static com.googlecode.javacv.cpp.opencv_highgui.cvCreateFileCapture;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import tracking.gui.controller.PluginButtonListener;
import tracking.gui.controller.VideoPathFieldListener;
import tracking.model.VideoProcessor;
import tracking.model.plugins.ObjectDetectionPlugin;
import tracking.model.plugins.hough.HoughSignRecognition;

import com.googlecode.javacv.cpp.opencv_highgui.CvCapture;

public class MainFrame extends JFrame {

    /**
     * A serialisation ID.
     */
    private static final long serialVersionUID = 8672857796068876923L;

    private JFileChooser fileChooser;

    /**
     * {@link JTextField} for the viedo file path
     */
    private JTextField videoPathField;

    /**
     * The {@link CvCapture} to capture the video.
     */
    private CvCapture videoCapture = null;

    /**
     * {@link JButton} to open the {@link JFileChooser} for loading a video file
     */
    private JButton videoLoadButton;

    /**
     * {@link JPanel} for listing the plugins
     */
    private PluginPanel pluginPanel;

    /**
     * {@link JScrollPane} contains the {@link JPanel} plugins
     */
    private JScrollPane pluginPane;

    /**
     * {@link JButton}s for adding, removing and edit a plugin and to start the video processing
     */
    private JButton add, remove, edit, start;

    /**
     * This {@link JPanel}s contain the three main gui components and are added to the mainPanel
     */
    private JPanel videoInputPanel, pluginPanelBorder, buttonPanel;

    /**
     * The {@link JPanel} mainPanel contains all components of the mainframe and is added to the
     * MainFrame {@link JFrame}
     */
    private JPanel mainPanel;

    /**
     * {@link LinkedList}s of the available plugins and the {@link LinkedList} of the added plugins
     * for late use in the {@link VideoProcessor}
     */
    private LinkedList<ObjectDetectionPlugin> availablePlugins, addedPlugins;

    /**
     * The chosen video {@link File}.
     */
    private File videoFile;

    /**
     * The PluginID counter.
     */
    private int pluginID = 0;

    /**
     * Enum for the {@link JButton} ActionCommand.
     * 
     * @author Philipp
     */
    public static enum Button {
        LOAD, ADD, REMOVE, EDIT, START;
    }

    /**
     * In the {@link MainFrame} the user can load a video file and setup the plugins for object
     * tracking. The {@link MainFrame} includes the main method.
     */
    public MainFrame() {

        GridBagConstraints gbc = new GridBagConstraints();

        /*
         * initialize plugin lists
         */
        availablePlugins = new LinkedList<ObjectDetectionPlugin>();
        addedPlugins = new LinkedList<ObjectDetectionPlugin>();

        /*
         * initialize all panels
         */
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        videoInputPanel = new JPanel();
        videoInputPanel.setBorder(new TitledBorder("Input Video"));
        pluginPanelBorder = new JPanel();
        pluginPanelBorder.setBorder(new TitledBorder("Plugins"));
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        pluginPanel = new PluginPanel();

        /*
         * setup components for videoInputPanel
         */
        videoPathField = new JTextField("Path to video file ...");
        videoPathField.setPreferredSize(new Dimension(320, 30));
        videoPathField.addActionListener(new VideoPathFieldListener(this));
        videoInputPanel.add(videoPathField);

        videoLoadButton = new JButton("Load Video");
        videoLoadButton.setActionCommand(Button.LOAD.name());
        videoLoadButton.addActionListener(new PluginButtonListener(this));
        videoInputPanel.add(videoLoadButton);

        /*
         * setup components for pluginPanel
         */
        pluginPane = new JScrollPane(pluginPanel);
        pluginPane.setPreferredSize(new Dimension(300, 200));
        pluginPanelBorder.add(pluginPane);

        /*
         * setup components for buttonPanel
         */
        gbc.anchor = GridBagConstraints.LINE_END;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        add = new JButton("Add Plugin");
        add.setActionCommand(Button.ADD.name());
        add.addActionListener(new PluginButtonListener(this));
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(add, gbc);

        remove = new JButton("Remove Plugin");
        remove.setActionCommand(Button.REMOVE.name());
        remove.addActionListener(new PluginButtonListener(this));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        buttonPanel.add(remove, gbc);

        edit = new JButton("Edit");
        edit.setActionCommand(Button.EDIT.name());
        edit.addActionListener(new PluginButtonListener(this));
        gbc.gridx = 0;
        gbc.gridy = 2;
        buttonPanel.add(edit, gbc);

        start = new JButton("Start");
        start.setActionCommand(Button.START.name());
        start.addActionListener(new PluginButtonListener(this));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.insets = new Insets(80, 0, 0, 0);
        buttonPanel.add(start, gbc);
        gbc.insets = new Insets(0, 0, 0, 0);

        // ####LinePainter

        // fileChooser = new JFileChooser();
        // fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //
        // videoInputPanel.add(videoFileUrl);
        // videoInputPanel.add(videoLoadButton);

        /*
         * add all panels to main panel
         */
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(videoInputPanel, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        mainPanel.add(pluginPanelBorder, gbc);

        gbc.gridx = 1;
        mainPanel.add(buttonPanel, gbc);

        /*
         * add main panel to the frame and setup frame settings
         */
        this.add(mainPanel);

        this.setTitle("GPStreetTracking");
        this.setSize(new Dimension(470, 340));
        this.setLocation(getCenterScreenPosition(this.getSize()));
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                exit();
            }
        });

    }

    /**
     * Get the center position of the frame in the screen.
     * 
     * @param frameDimension
     *            the {@link Dimension} of the frame
     * @return the {@link Point} of position
     */
    public static Point getCenterScreenPosition(Dimension frameDimension) {
        int x = (int) (frameDimension.getWidth() / 2);
        int y = (int) (frameDimension.getHeight() / 2);
        int v = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
        int w = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
        return new Point((v - x), (w - y));

    }

    /**
     * Add a Plugin to the availablePlugins list.
     * 
     * @param plugin
     *            the plugin to add.
     */
    private void addAvailablePlugin(ObjectDetectionPlugin plugin) {
        availablePlugins.add(plugin);
    }

    /**
     * Add a Plugin to the added plugins list.
     * 
     * @param plugin
     *            the plugin to add.
     */
    private void addToAddedPluginsList(ObjectDetectionPlugin plugin) {
        plugin.setPluginID(pluginID);
        addedPlugins.add(plugin);
        pluginPanel.addPlugin(plugin.getPluginName(), plugin.getPluginID());
        pluginID++;
    }

    /**
     * Open the {@link AddPluginDialog} Dialog. Called from the Add button listener in the
     * {@link PluginButtonListener} class.
     */
    public void openAddDialog() {

        // Initialize a new AddPluginDialog
        AddPluginDialog dialog = new AddPluginDialog(availablePlugins);
        dialog.setTitle("GPStreetTracking - Add Plugin");

        // set it modal, so the program stops until an ImageObjectTracker is returned
        dialog.setModal(true);
        ObjectDetectionPlugin tmp = dialog.getImageObjectTracker();

        // if nothing is chosen by the user, then the return is null
        if (tmp != null) {
            addToAddedPluginsList(tmp);
        }
    }

    public void openEditorDialog() {

        if (pluginPanel.getID() != -1) {
            System.out.println("edit plugin: plugin ID = "
                    + pluginPanel.getID());
            for (int i = 0; i < addedPlugins.size(); i++) {
                if (addedPlugins.get(i).getPluginID() == pluginPanel.getID()) {
                    JDialog editor = addedPlugins.get(i).getEditorDialog();
                    editor.setModal(true);
                    editor.setVisible(true);
                    // return;
                }
            }
        } else {
            System.out.println("nothing highlighted");
        }
    }

    /**
     * Remove the highlighted Plugin from the addedPlugins {@link LinkedList}. Called from the
     * Remove button listener in the {@link PluginButtonListener} class.
     */
    public void removePluginFromAddedPluglinsList() {
        if (pluginPanel.getID() != -1) {
            for (int i = 0; i < addedPlugins.size(); i++) {
                if (addedPlugins.get(i).getPluginID() == pluginPanel.getID()) {
                    System.out.println("remove plugin: "
                            + addedPlugins.get(i).getPluginName()
                            + ", plugin ID = " + pluginPanel.getID());
                    addedPlugins.remove(i);
                    pluginPanel.removeHighlightedPlugin();
                    return;
                }
            }
        } else {
            System.out.println("nothing highlighted");
        }
    }

    /**
     * Open the JFileChooser to choose a video file
     */
    public void openFileChooser() {

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {

            // Only show .mp4 / .mpg files
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()
                        || file.getName().toLowerCase().endsWith(".mp4")
                        || file.getName().toLowerCase().endsWith(".mpg")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return ".mp4 / .mpg files";
            }
        });

        // check if ok button was pressed
        int answer = fileChooser.showOpenDialog(this);
        if (answer == JFileChooser.APPROVE_OPTION) {
            setVideoFile(fileChooser.getSelectedFile());
        } // otherwise do nothing

    }

    /**
     * Set the video {@link File}
     * 
     * @param file
     *            the file
     * @return {@code true}, if the file's path is correct; {@code false} otherwise
     */
    public boolean setVideoFile(File file) {
        if (!file.isDirectory()
                && file.exists()
                && (file.getName().toLowerCase().endsWith(".mp4") || file
                        .getName().toLowerCase().endsWith(".mpg"))) {
            videoFile = file;
            videoPathField.setText(file.getPath());

            // Create the CvCapture here, because it can only be initialized once.
            videoLoadButton.setEnabled(false);
            videoPathField.setEditable(false);
            videoCapture = new CvCapture();
            videoCapture = cvCreateFileCapture(file.getPath());

            return true;
        } else {
            // when the path is not correct, reset videoPathField and videoFile
            videoPathField.setText("Path to video file ...");
            videoFile = null;
            return false;
        }
    }

    /**
     * return the CvCapture videoCapture.
     */
    public CvCapture getVideoCapture() {
        return videoCapture;
    }

    // public File getVideoFile() { Return a CvGrabber instead of the VideoFile, because the Grabber
    // could only be initialised once.
    // return videoFile;
    // }

    /**
     * Starts the {@link VideoProcessor} if at least one plugin and a videofile are selected.
     */
    public void startProcessing() {
        if (!addedPlugins.isEmpty() && videoFile != null) { // if video and plugins are set

            /*
             * create a VideoProcessorFrame and a VideoProcessor instance and dispose the MainFrame
             */
            VideoProcessorFrame frame = new VideoProcessorFrame(
                    new VideoProcessor(videoCapture, addedPlugins));
            this.dispose();
            frame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    "You have to select a video file and at least one plugin.");
        }
    }

    /**
     * The Main-method. Add your plugins here.
     * 
     * @param args
     */
    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                MainFrame mframe = new MainFrame();

                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                /*
                 * Add your Plugins here
                 */
                mframe.addAvailablePlugin(new HoughSignRecognition(mframe));

                // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                mframe.setVisible(true);

            }
        });
    }

    /**
     * Show an exit {@link JDialog} when the Frame is closed.
     */
    private void exit() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to exit?", "",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        switch (answer) {
        case JOptionPane.YES_OPTION:
            this.dispose();
        }
    }

}
