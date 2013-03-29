package tracking.gui;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateImage;
import static com.googlecode.javacv.cpp.opencv_core.cvSize;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvResize;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableRowSorter;

import tracking.gui.controller.GPSPathFieldListener;
import tracking.gui.controller.GPSProcessorButtonListener;
import tracking.model.GPSProcessor;
import tracking.model.plugins.StreetObject;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

public class GPSProcessorFrame extends JFrame implements Observer {

    /**
     * A serialisation ID.
     */
    private static final long serialVersionUID = 3367428082151964840L;

    /**
     * {@link JFileChooser} to choose the .gpx file.
     */
    private JFileChooser fileChooser;

    /**
     * Initial size for the {@link StreetObject} {@link JTable}.
     */
    private final Dimension STREET_OBJECT_LIST_SIZE = new Dimension(350, 500);

    /**
     * Default text for the Load-GPS {@link TextField}.
     */
    private final String DEFAULT_LOAD_GPS = "Path to .gpx file ...";

    /**
     * Default text for the directory {@link JFileChooser}.
     */
    private final String DEFAULT_LOAD_DIRECTORY = "Save to directory";

    /**
     * Defalut text for the image saveing name.
     */
    private final String DEFAULT_IMAGE_SAVING_NAME = "#date#_#index#";

    /**
     * Default title for the Input {@link JPanel} border.
     */
    private final String TITLE_BORDER_INPUT = "Input";

    /**
     * Default text for the {@link StreetObject} {@link JPanel} border.
     */
    private final String TITLE_BORDER_STREETOBJECTS = "Street Objects";

    /**
     * Default text for the output {@link JPanel} border.
     */
    private final String TITLE_BORDER_SAVINGPROPERTIES = "Output";

    /**
     * ImageSaveName can be changed by user.
     */
    private String imageSaveName = DEFAULT_IMAGE_SAVING_NAME;

    /**
     * {@link TextField} to choose the .gpx file.
     */
    private JTextField filedLoadGpx;
    private JTextField fieldLoadDirectory;
    private JTextField fieldSaveNameProperty;

    /**
     * Defalut {@link JLabel} text.
     */
    private final JLabel LABEL_JPG = new JLabel(".JPG");

    /**
     * Defalut {@link JLabel} text.
     */
    private final JLabel LABEL_SAVE_NAME_PROPERTY = new JLabel(
            "Replacements: #name#, #date#, #type#, #frame# or #index#");

    /**
     * {@link JButton} to open the file chooser.
     */
    private JButton buttonLoadGpx;
    private JButton buttonLoadDirectory;
    private JButton buttonSave;

    private File fileGpx;

    private File fileDirectory;

    /**
     * The main {@link JPanel} in the frame {@link GPSProcessorFrame}.
     */
    private JPanel mainPanel;
    private JPanel gpxInputPanel;
    private JPanel streetObjPanel;
    private JPanel savingPropertiesPanel;
    private JScrollPane streetObjPane;
    private JTable streetObjTable;
    private GPSProcessor processor;
    private boolean setgpx = false;
    private boolean setlocation = false;

    /**
     * Enum {@link Identify} for Button ActionCommands.
     */
    public static enum Identify {
        LOAD_GPX, LOAD_DIRECTORY, SAVING_PROPERTY, SAVE;
    }

    /**
     * GUI for the {@link GPSProcessor} model to manage the found {@link StreetObject}.
     * 
     * @param processor
     *            the {@link GPSProcessor} model.
     */
    public GPSProcessorFrame(GPSProcessor processor) {

        this.processor = processor;
        this.processor.addObserver(this);

        GridBagConstraints gbc = new GridBagConstraints();

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());

        // Setup gpx input panel
        // gpx input
        gpxInputPanel = new JPanel();
        gpxInputPanel.setLayout(new GridBagLayout());
        gpxInputPanel.setBorder(new TitledBorder(TITLE_BORDER_INPUT));

        filedLoadGpx = new JTextField(DEFAULT_LOAD_GPS);
        filedLoadGpx.setPreferredSize(new Dimension(320, 30));
        filedLoadGpx.setActionCommand(Identify.LOAD_GPX.name());
        filedLoadGpx.addActionListener(new GPSPathFieldListener(this));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.WEST;
        gpxInputPanel.add(filedLoadGpx, gbc);

        buttonLoadGpx = new JButton("Load File");
        buttonLoadGpx.setActionCommand(Identify.LOAD_GPX.name());
        buttonLoadGpx.addActionListener(new GPSProcessorButtonListener(this));
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gpxInputPanel.add(buttonLoadGpx, gbc);

        // Setup panel for street objects
        // this panel includes a JScrollPane and this scroll pane includes the JTable with the
        // street objects
        gbc = new GridBagConstraints();
        String[] names = { "Frame#", "Timestamp", "PluginName", "Image", "Save" };
        boolean[] editable = { false, false, false, false, true };
        LinkedList<StreetObject> objects = processor.getStreetObjects();

        Object[][] data = new Object[objects.size()][names.length];

        for (int i = 0; i < objects.size(); i++) {
            IplImage tmp = cvCreateImage(cvSize(60, 50), IPL_DEPTH_8U, 3);
            StreetObject obj = objects.get(i);
            data[i][0] = obj.getFrame();
            data[i][1] = obj.getTimestampDouble();
            data[i][2] = obj.getPluginName();
            cvResize(obj.getImage(), tmp);
            data[i][3] = new ImageIcon(tmp.getBufferedImage());
            data[i][4] = true;
        }

        CustomTableModel ctm = new CustomTableModel(data, names, editable);

        streetObjTable = new JTable();
        streetObjTable.setModel(ctm);
        streetObjTable.setRowHeight(50);
        streetObjTable.getTableHeader().setReorderingAllowed(false);

        TableRowSorter<CustomTableModel> sorter = new TableRowSorter<>();
        streetObjTable.setRowSorter(sorter);
        sorter.setModel(ctm);

        // int selectedRow = sorter.modelIndex(table.getSelectedRow()); Gibt die ausgewählte zeile
        // zurück

        // streetObjTable.getColumnModel().getColumn(0).setHeaderValue("Name");
        // streetObjTable.getColumnModel().getColumn(1).setHeaderValue("Number");
        // TableColumn column0 = streetObjTable.getColumnModel().getColumn(0);
        // column0.setPreferredWidth(250);
        // TableColumn column1 = streetObjTable.getColumnModel().getColumn(1);
        // column1.setPreferredWidth(50);

        // table.addMouseListener(new MouseAdapter() {
        // public void mouseClicked(MouseEvent e) {
        // if (e.getClickCount() == 2) {
        // JTable target = (JTable)e.getSource();
        // int row = target.getSelectedRow();
        // int column = target.getSelectedColumn();
        // // do some action if appropriate column
        // }
        // }
        // });

        streetObjPane = new JScrollPane(streetObjTable);
        streetObjPane.setPreferredSize(STREET_OBJECT_LIST_SIZE);
        streetObjPanel = new JPanel();
        streetObjPanel.setBorder(new TitledBorder(TITLE_BORDER_STREETOBJECTS));
        streetObjPanel.add(streetObjPane);

        // ###########Setup saving properties panel
        gbc = new GridBagConstraints();
        savingPropertiesPanel = new JPanel();
        savingPropertiesPanel.setLayout(new GridBagLayout());
        savingPropertiesPanel.setBorder(new TitledBorder(
                TITLE_BORDER_SAVINGPROPERTIES));

        // directory
        fieldLoadDirectory = new JTextField(DEFAULT_LOAD_DIRECTORY);
        fieldLoadDirectory.setPreferredSize(new Dimension(320, 30));
        fieldLoadDirectory.setActionCommand(Identify.LOAD_DIRECTORY.name());
        fieldLoadDirectory.addActionListener(new GPSPathFieldListener(this));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        savingPropertiesPanel.add(fieldLoadDirectory, gbc);

        buttonLoadDirectory = new JButton("Choose Directory");
        buttonLoadDirectory.setActionCommand(Identify.LOAD_DIRECTORY.name());
        buttonLoadDirectory.addActionListener(new GPSProcessorButtonListener(
                this));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        savingPropertiesPanel.add(buttonLoadDirectory, gbc);

        // name
        fieldSaveNameProperty = new JTextField(DEFAULT_IMAGE_SAVING_NAME);
        fieldSaveNameProperty.setPreferredSize(new Dimension(320, 30));
        fieldSaveNameProperty.setActionCommand(Identify.SAVING_PROPERTY.name());
        fieldSaveNameProperty.addActionListener(new GPSPathFieldListener(this));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        savingPropertiesPanel.add(fieldSaveNameProperty, gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        savingPropertiesPanel.add(LABEL_JPG, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        savingPropertiesPanel.add(LABEL_SAVE_NAME_PROPERTY, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(180, 0, 170, 0);
        savingPropertiesPanel.add(new JLabel(), gbc);

        buttonSave = new JButton("Save");
        buttonSave.setEnabled(false);
        buttonSave.setActionCommand(Identify.SAVE.name());
        buttonSave.addActionListener(new GPSProcessorButtonListener(this));
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);
        savingPropertiesPanel.add(buttonSave, gbc);

        // Add all panels to main panel

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.BASELINE;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(streetObjPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        mainPanel.add(gpxInputPanel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        mainPanel.add(savingPropertiesPanel, gbc);

        this.add(mainPanel);
        this.pack();
        this.setTitle("GPStreetTracking");
        this.setLocation(MainFrame.getCenterScreenPosition(this.getSize()));
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                exit();
            }
        });
    }

    /**
     * Open the JFileChooser to choose a .gpx file
     */
    public void openGPXFileChooser() {

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {

            // Only show .gpx files
            @Override
            public boolean accept(File file) {
                if (file.isDirectory()
                        || file.getName().toLowerCase().endsWith(".gpx")) {
                    return true;
                }
                return false;
            }

            @Override
            public String getDescription() {
                return ".gpx files";
            }
        });

        // check if ok button was pressed
        int answer = fileChooser.showOpenDialog(this);
        if (answer == JFileChooser.APPROVE_OPTION) {
            setGPXFile(fileChooser.getSelectedFile());
        } // otherwise do nothing

    }

    /**
     * Open a {@link JFileChooser} to choose a directory for saving gpx data and images.
     */
    public void openLocationFileChooser() {

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // check if ok button was pressed
        int answer = fileChooser.showOpenDialog(this);
        if (answer == JFileChooser.APPROVE_OPTION) {
            setDirectoryFile(fileChooser.getSelectedFile());
        } // otherwise do nothing

    }

    /**
     * Set directory as {@link File}. The data of the GPSProcesser, like the gpx file and images
     * from the {@link StreetObject}s, is saved here.
     * 
     * @param file
     *            the path of the directory as file
     * @return <code>true</code> if path was set correct, <code>false</code> otherwise.
     */
    public boolean setDirectoryFile(File file) {
        if (file.isDirectory() && file.exists()) {
            fileDirectory = file;
            fieldLoadDirectory.setText(file.getPath());
            setlocation = true;
            buttonSave.setEnabled(setlocation & setgpx);
            return true;
        } else {
            // when the path is not correct, reset filedLoadDirectory and gpxFile
            fieldLoadDirectory.setText(DEFAULT_LOAD_DIRECTORY);
            fileDirectory = null;
            setlocation = false;
            buttonSave.setEnabled(setlocation & setgpx);
            return false;
        }
    }

    /**
     * Set the gpx file with the gps location data according to the video.
     * 
     * @param file
     *            the .gpx file.
     * @return <code>true</code> if path was set correct, <code>false</code> otherwise.
     */
    public boolean setGPXFile(File file) {
        if (!file.isDirectory() && file.exists()
                && file.getName().toLowerCase().endsWith(".gpx")) {
            fileGpx = file;
            filedLoadGpx.setText(file.getPath());
            setgpx = true;
            buttonSave.setEnabled(setlocation & setgpx);
            return true;
        } else {
            // when the path is not correct, reset gpxPathField and gpxFile
            filedLoadGpx.setText(DEFAULT_LOAD_GPS);
            fileGpx = null;
            setgpx = false;
            buttonSave.setEnabled(setlocation & setgpx);
            return false;
        }
    }

    /**
     * Set the image save name for later use.
     * 
     * @param imageSaveName
     */
    public void setImageSaveName(String imageSaveName) {
        this.imageSaveName = imageSaveName;
    }

    /**
     * Called from the {@link GPSProcessorButtonListener} when the save button is pressed.
     */
    public void saveButtonPressed() {
        boolean[] sObj = new boolean[streetObjTable.getRowCount()];
        for (int i = 0; i < sObj.length; i++) {
            sObj[i] = (boolean) streetObjTable.getModel().getValueAt(i,
                    streetObjTable.getColumnCount() - 1);

        }
        processor.save(fileGpx, fileDirectory, imageSaveName, sObj);

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
            this.dispose();
        }
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        // arg1[0] == 0 : error message
        // arg1[0] == 1 : success message
        Object[] array = (Object[]) arg1;
        int type = (int) array[0];
        String message = (String) array[1];

        if (type == 0) {
            JOptionPane.showMessageDialog(this, message, "Error!",
                    JOptionPane.ERROR_MESSAGE);
        } else if (type == 1) {
            JOptionPane.showMessageDialog(this, message, "Success!",
                    JOptionPane.INFORMATION_MESSAGE);
        }

    }
}
