package tracking.gui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JTextField;

import tracking.gui.MainFrame;

public class VideoPathFieldListener implements ActionListener {

    private MainFrame frame;

    /**
     * Constructor for the {@link VideoPathFieldListener}.
     * 
     * @param frame
     *            the {@link MainFrame}
     */
    public VideoPathFieldListener(MainFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField field = (JTextField) e.getSource();
        frame.setVideoFile(new File(field.getText()));
    }

}
