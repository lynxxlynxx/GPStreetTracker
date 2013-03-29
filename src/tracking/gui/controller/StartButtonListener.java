package tracking.gui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import tracking.gui.VideoProcessorFrame;

public class StartButtonListener implements ActionListener {

    VideoProcessorFrame frame;

    /**
     * Create new {@link StartButtonListener}.
     * 
     * @param frame
     */
    public StartButtonListener(VideoProcessorFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        String command = button.getActionCommand();
        if (command.equals("start")) {
            frame.startButtonPressed();
        } else if (command.equals("stop")) {
            frame.stopButtonPressed();
        } else if (command.equals("next")) {
            frame.nextButtonPressed();
        }
    }

}
