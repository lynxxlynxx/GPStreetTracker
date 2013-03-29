package tracking.gui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import tracking.gui.MainFrame;

/**
 * Listener for the pluigin Add, Remove, Edit buttons and the Start button.
 * 
 * @author Philipp
 */
public class PluginButtonListener implements ActionListener {

    MainFrame frame;

    /**
     * Create a new {@link PluginButtonListener}.
     * @param frame the {@link MainFrame}.
     */
    public PluginButtonListener(MainFrame frame) {
        this.frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        String name = button.getActionCommand();

        if (name == MainFrame.Button.LOAD.name()) {
            frame.openFileChooser();
        } else if (name == MainFrame.Button.ADD.name()) {
            frame.openAddDialog();
        } else if (name == MainFrame.Button.REMOVE.name()) {
            frame.removePluginFromAddedPluglinsList();
        } else if (name == MainFrame.Button.EDIT.name()) {
            frame.openEditorDialog();
        } else if (name == MainFrame.Button.START.name()) {
            frame.startProcessing();
        }
    }

}
