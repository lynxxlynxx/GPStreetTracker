package tracking.model.plugins;

import javax.swing.JDialog;

import com.googlecode.javacv.cpp.opencv_core.IplImage;

/**
 * Interface for ObjectDetectionPlugins used in the VideoProcessor.
 * 
 * @author Philipp
 */
public interface ObjectDetectionPlugin {

    /**
     * This number is for identifiing the {@link ObjectDetectionPlugin} Plugin if more than one
     * plugin is used
     * 
     * @return the {@link Integer} number of the Plugin, <i>-1 </i> if not set
     */
    public int getPluginID();

    /**
     * This number is for identifiing the {@link ObjectDetectionPlugin} Plugin if more than one
     * plugin is used
     */
    public void setPluginID(int id);

    /**
     * The {@link String} name is shown in the plugin selection menu
     * 
     * @return the {@link String} name of the plugin
     */
    public String getPluginName();

    /**
     * Create a new instance of the {@link ObjectDetectionPlugin} Object.
     * 
     * @return the clone
     */
    public ObjectDetectionPlugin clonePlugin();

    /**
     * Return if the {@link ObjectDetectionPlugin} Instance is editable or not.
     * 
     * @return true if editable, false if not editable
     */
    public boolean isEditable();

    /**
     * Get the editor {@link JDialog} of the {@link ObjectDetectionPlugin}.
     * 
     * @return the editor {@link JDialog}.
     */
    public JDialog getEditorDialog();

    /**
     * Process the image. Track objects and return them in a {@link ResultObject} which contains a
     * list of {@link StreetObject}s and an {@link IplImage} where you can draw whatever you want
     * for user information.
     * 
     * @return the {@link ResultObject}.
     */
    public ResultObject process(IplImage original, IplImage drawable);

    /**
     * Set the {@link Boolean} that allows the plugin to draw into an {@link IplImage}.
     * 
     * @param draw
     *            if <code> false </code> the plugin should return the original image.
     */
    public void setDrawIntoImage(boolean draw);

    /**
     * Returns, if plugin is drawing in to image or not.
     * 
     * @return <code> true </code> if plugin is drawing into image, <code> false </code> otherwise.
     */
    public boolean getDrawIntoImage();

}
