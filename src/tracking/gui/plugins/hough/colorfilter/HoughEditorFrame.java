package tracking.gui.plugins.hough.colorfilter;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tracking.gui.MainFrame;
import tracking.gui.layouts.VerticalFlowLayout;
import tracking.model.plugins.hough.HoughSignRecognition;

public class HoughEditorFrame extends JDialog {

    private JButton cancel, ok;

    /**
     * A serialisation ID.
     */
    private static final long serialVersionUID = 1486269332739910279L;

    /**
     * The {@link JTabbedPane} to switch between the {@link ColorFilterPanel} and the
     * {@link SettingsPanel}.
     */
    private JTabbedPane tabbedPane;

    private JPanel mainPanel;

    private ColorFilterPanel colorFilterPanel;

    private SettingsPanel settingsPanel;

    /**
     * Constructor for the {@link HoughEditorFrame}. The {@link HoughEditorFrame} is a
     * {@link JDialog} which shows the {@link ColorFilterPanel} and the {@link SettingsPanel} to the
     * user.
     * 
     * @param model
     *            the plugin {@link HoughEditorFrame}.
     */
    public HoughEditorFrame(HoughSignRecognition model) {

        if (model.getVideoCapture() != null) {

            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();

            colorFilterPanel = new ColorFilterPanel(model);
            settingsPanel = new SettingsPanel(model);

            tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Color Filter", null, colorFilterPanel,
                    "Add or remove scalar values for the color filters");

            tabbedPane.addTab("Settings", null, settingsPanel,
                    "Set values for the HoughSignRecognition tracking methods");

            gbc.weightx = 0.5;
            gbc.weighty = 0.5;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            mainPanel.add(tabbedPane, gbc);

            // cancel Button
            cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    cancelDialog();
                }
            });

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 450, 5, 5);

            mainPanel.add(cancel, gbc);

            // ok Button
            ok = new JButton("Ok");
            ok.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // Notify the editor panels, that the ok button was pressed and the data can be
                    // saved.
                    colorFilterPanel.okButtonPressed();
                    settingsPanel.okButtonPressed();
                    colorFilterPanel.releaseCapture();
                    dispose();
                }
            });

            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 0, 5, 0);

            mainPanel.add(ok, gbc);

            JMenuBar menubar = new JMenuBar();
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

            this.setJMenuBar(menubar);
            this.add(mainPanel);
            this.pack();
            this.setResizable(false); // set true can cause troube with the settings table
            // minimum size for a correct working grid bag layout
            this.setMinimumSize(new Dimension(829, 544));
            this.setLocation(MainFrame.getCenterScreenPosition(this.getSize()));
            // windows listener for exit
            this.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    // release the videoFrameGrabber capture
                    colorFilterPanel.releaseCapture();
                    dispose();
                }
            });
            // component listener for resizing event
            this.addComponentListener(new ComponentListener() {

                @Override
                public void componentShown(ComponentEvent arg0) {}

                @Override
                public void componentResized(ComponentEvent arg0) {
                    JDialog frame = (JDialog) arg0.getSource();

                    // set Image panels to 40% of the frame size default 2.4
                    int width = (int) (frame.getSize().getWidth() / 2.4);
                    colorFilterPanel.setImagePanelSize(width);

                }

                @Override
                public void componentMoved(ComponentEvent arg0) {}

                @Override
                public void componentHidden(ComponentEvent arg0) {}
            });

        } else { // No video file is selected, can not start preview

            /*
             * Make a JDialog similar to a JOptionPane to show the user a warning, that no video is
             * selected
             */
            JPanel panel = new JPanel();
            panel.setLayout(new VerticalFlowLayout());
            JLabel label = new JLabel("You must select a video file first!");
            panel.add(label);
            JButton ok = new JButton("OK");
            ok.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            panel.add(ok);
            this.add(panel);
            this.pack();
            this.setResizable(false);
            this.setLocation(MainFrame.getCenterScreenPosition(this.getSize()));
        }
    }

    /**
     * Set the image orientation of the Images in {@link ColorFilterPanel}
     * 
     * @param landscape
     *            true is landscape mode.
     */
    private void setImageOrientation(boolean landscape) {
        colorFilterPanel.setImagePanelOrientation(landscape);
    }

    /**
     * Set the rotation of the images in {@link ColorFilterPanel}.
     * 
     * @param rotation
     */
    private void setImageRotation(boolean rotation) {
        colorFilterPanel.setImagePanelRotation(rotation);
    }

    /**
     * Open the Cancel Dialog.
     */
    private void cancelDialog() {
        int answer = JOptionPane
                .showConfirmDialog(
                        this,
                        "Do you really want to cancel the editor? Changes in scalar values will not be saved.",
                        "Cancel Editor", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.OK_OPTION) {
            colorFilterPanel.releaseCapture();
            dispose();
        }
    }

}
