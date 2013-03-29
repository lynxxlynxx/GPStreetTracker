package tracking.gui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import tracking.gui.GPSProcessorFrame;

public class GPSProcessorButtonListener implements ActionListener {

    private GPSProcessorFrame frame;

    /**
     * Create a new {@link GPSProcessorButtonListener}.
     * 
     * @param frame
     *            the  {@link GPSProcessorFrame}.
     */
    public GPSProcessorButtonListener(GPSProcessorFrame frame) {
        this.frame = frame;

    }

    @Override
    public void actionPerformed(ActionEvent event) {

        JButton button = (JButton) event.getSource();
        String name = button.getActionCommand();

        if (name == GPSProcessorFrame.Identify.LOAD_GPX.name()) {
            frame.openGPXFileChooser();
        } else if (name == GPSProcessorFrame.Identify.LOAD_DIRECTORY.name()) {
            frame.openLocationFileChooser();
        } else if (name == GPSProcessorFrame.Identify.SAVE.name()) {
            frame.saveButtonPressed();
        }
    }

}
