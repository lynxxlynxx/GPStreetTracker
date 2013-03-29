package tracking.gui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JTextField;

import tracking.gui.GPSProcessorFrame;

/**
 * {@link ActionListener} for the GPSPathField.
 * @author Philipp
 *
 */
public class GPSPathFieldListener implements ActionListener {

    private GPSProcessorFrame frame;

    /**
     * Create a new {@link GPSPathFieldListener}.
     * @param frame
     */
    public GPSPathFieldListener(GPSProcessorFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField field = (JTextField) e.getSource();

        if (e.getActionCommand() == GPSProcessorFrame.Identify.LOAD_GPX.name()) {
            frame.setGPXFile(new File(field.getText()));
        } else if (e.getActionCommand() == GPSProcessorFrame.Identify.LOAD_DIRECTORY
                .name()) {
            frame.setDirectoryFile(new File(field.getText()));
        } else if (e.getActionCommand() == GPSProcessorFrame.Identify.SAVING_PROPERTY
                .name()) {
            frame.setImageSaveName(field.getText());
        }
    }

}
