package tracking.gui;

import java.awt.Color;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import tracking.gui.controller.PluginLabelListener;
import tracking.gui.layouts.VerticalFlowLayout;

public class PluginPanel extends JPanel {

    /**
     * A Serial Version UID.
     */
    private static final long serialVersionUID = -6755198760363131056L;

    /**
     * The {@link LinkedList} buttons contains the buttons for choosing the plugins.
     */
    private LinkedList<JButton> buttons;

    /**
     * The actual highlighted {@link JButton}. Same id as the plugin.
     */
    private JButton selecedButton = null;

    /**
     * The last highlighted button
     */
    private JButton lastHighlighted = null;

    /**
     * Create a new Plugin panel.
     */
    public PluginPanel() {
        this.setLayout(new VerticalFlowLayout(2, VerticalFlowLayout.BOTH));
        this.setBackground(Color.white);
        buttons = new LinkedList<JButton>();
    }

    /**
     * Add a plugin to the plugin panel
     * 
     * @param name
     *            {@link String} of the plugin shown in the panel
     * @param id
     *            {@link Integer} to identify the plugin
     */
    public void addPlugin(String name, int id) {

        /*
         * create a new JButton that looks like a JLabel, beacuse JButtons have ActionCommands
         */
        JButton button = new JButton(name);
        button.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        button.setBorderPainted(false);
        button.setBackground(Color.white);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.setActionCommand("" + id);

        buttons.add(button);
        button.addActionListener(new PluginLabelListener(this));

        this.add(button);
        this.updateUI();
    }

    /**
     * Remove the highlighted plugin.
     */
    public void removeHighlightedPlugin() {
        for (int i = 0; i < buttons.size(); i++) {
            if (selecedButton.getActionCommand() == buttons.get(i)
                    .getActionCommand()) {
                buttons.remove(i);
                this.rebuid();
                return;
            }
        }

    }

    /**
     * Highlight a button. The id of the button is set as the selectedButton id.
     * 
     * @param button
     *            the {@link JButton}.
     */
    public void highlightButton(JButton button) {
        selecedButton = button;

        button.setBackground(Color.blue);
        button.setForeground(Color.white);
        if (lastHighlighted != null && button != lastHighlighted) {
            lastHighlighted.setBackground(Color.white);
            lastHighlighted.setForeground(Color.black);
        }
        lastHighlighted = button;
    }

    /**
     * Returns the number of the selected plugin
     * 
     * @return the number of the plugin as {@link Integer}, <i>-1</i> if no Plugin is selected.
     */
    public int getID() {
        if (selecedButton != null) {
            return Integer.parseInt(selecedButton.getActionCommand());
        } else {
            return -1;
        }
    }

    /**
     * Update the view of the plugin panel.
     */
    private void rebuid() {
        this.removeAll();
        for (int i = 0; i < buttons.size(); i++) {
            this.add(buttons.get(i));
        }

        selecedButton = null;

        this.updateUI();
    }

}
