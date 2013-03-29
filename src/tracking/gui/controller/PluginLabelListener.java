package tracking.gui.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import tracking.gui.PluginPanel;

public class PluginLabelListener implements ActionListener {

    /**
     * The plugin panel
     */
    private PluginPanel pluginPanel;

    /**
     * Constructor for the {@link PluginLabelListener}
     * 
     * @param panel
     *            the plugin panel in which the button is
     */
    public PluginLabelListener(PluginPanel panel) {
        this.pluginPanel = panel;
    }

    /**
     * Highlight the clicked button
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        pluginPanel.highlightButton((JButton) e.getSource());
    }

}
