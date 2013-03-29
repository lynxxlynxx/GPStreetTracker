package tracking.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import tracking.model.plugins.ObjectDetectionPlugin;

/**
 * Dialog to choose a plugin from the list of available plugins.
 * @author Philipp
 *
 */
public class AddPluginDialog extends JDialog {

    /**
     * A serialization id
     */
    private static final long serialVersionUID = 855384296891615149L;

    /**
     * ComboBox to choose a plugin
     */
    private JComboBox<String> pluginBox;

    /**
     * Panel of the Dialog
     */
    private JPanel panel;

    /**
     * Add {@link JButton} to add a plugin
     */
    private JButton add;

    /**
     * The chosen plugin
     */
    private ObjectDetectionPlugin plugin = null;

    /**
     * {@link ObjectDetectionPlugin} {@link LinkedList} of the available plugins
     */
    private LinkedList<ObjectDetectionPlugin> availablePlugins;

    /**
     * {@link JDialog} for choosing a {@link ObjectDetectionPlugin} plugin from a
     * {@link ObjectDetectionPlugin} List
     * 
     * @param pluginList
     *            the {@link ObjectDetectionPlugin} {@link LinkedList}
     */
    public AddPluginDialog(LinkedList<ObjectDetectionPlugin> pluginList) {

        this.availablePlugins = pluginList;

        /*
         * initialize the panel for the dialog
         */
        this.panel = new JPanel();
        this.panel.setLayout(new FlowLayout());

        /*
         * fill the names of the available plugins into the JComboBox
         */
        String[] list = new String[availablePlugins.size()];

        for (int i = 0; i < availablePlugins.size(); i++) {
            list[i] = availablePlugins.get(i).getPluginName();
        }

        pluginBox = new JComboBox<String>(list);
        pluginBox.setEditable(false);

        panel.add(pluginBox);

        /*
         * Add the AddButton an set the ActionListener
         */
        this.add = new JButton("Add");

        add.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setPlugin(availablePlugins.get(pluginBox.getSelectedIndex())
                        .clonePlugin());

            }
        });

        panel.add(add);

        /*
         * Add all components to the Dialog Frame and set properties
         */
        this.add(panel);
        this.pack();
        this.setResizable(false);
        this.setLocation(MainFrame.getCenterScreenPosition(this.getSize()));
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    }

    /**
     * Set the chosen {@link ObjectDetectionPlugin} Plugin.
     * 
     * @param plugin
     */
    public void setPlugin(ObjectDetectionPlugin plugin) {
        this.plugin = plugin;
        this.dispose();
    }

    /**
     * Open the {@link AddPluginDialog} {@link JDialog} for choosing a plugin from the handed
     * {@link ObjectDetectionPlugin} plugin list in the constructor of {@link AddPluginDialog}.
     * 
     * @return the chosen plugin
     */
    public ObjectDetectionPlugin getImageObjectTracker() {
        this.setVisible(true);

        // the program stops here until the AddPluginDialog is disposed. This is done by the
        // Add button listener.
        return plugin;
    }

}
