package tracking.gui.plugins.hough.colorfilter.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JSlider;

import tracking.gui.plugins.hough.colorfilter.ColorFilterPanel;

public class FrameGrabberSliderListener implements MouseMotionListener {

    ColorFilterPanel frame;

    /**
     * Create a new {@link FrameGrabberSliderListener}.
     * 
     * @param frame
     *            the {@link ColorFilterPanel} in which the Listener can set the values.
     */
    public FrameGrabberSliderListener(ColorFilterPanel frame) {
        this.frame = frame;

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        JSlider slider = (JSlider) e.getSource();
        frame.setFilterImageAtFrame(slider.getValue());

    }

    @Override
    public void mouseMoved(MouseEvent e) {}

}
