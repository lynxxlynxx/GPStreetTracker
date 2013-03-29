package tracking.gui.plugins.hough.colorfilter.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JSlider;
import javax.swing.JTextField;

import tracking.gui.plugins.hough.colorfilter.ColorFilterPanel;
import tracking.model.plugins.hough.colorfilter.ColorFilter;

public class ColorFilterSliderListener implements MouseMotionListener {

    private JTextField value;
    private ColorFilterPanel.Slider type;
    private ColorFilter cfilter;

    /**
     * {@link MouseMotionListener} for a {@link JSlider} with a {@link JTextField}
     * 
     * @param cfilter
     *            the {@link ColorFilter}
     * @param value
     *            the {@link JTextField} shows the value of the {@link JSlider}
     * @param slider
     *            the {@link FilterSlider} type
     */
    public ColorFilterSliderListener(ColorFilter cfilter, JTextField value,
            ColorFilterPanel.Slider type) {
        this.cfilter = cfilter;
        this.value = value;
        this.type = type;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        JSlider setslider = (JSlider) e.getSource();
        double val = setslider.getValue();

        value.setText("" + val);

        switch (type) {
        case LOWER_BLUE:
            cfilter.setLowBlue(val);
            break;
        case LOWER_GREEN:
            cfilter.setLowGreen(val);
            break;
        case LOWER_RED:
            cfilter.setLowRed(val);
            break;
        case LOWER_ALPHA:
            cfilter.setLowAlpha(val);
            break;
        case UPPER_BLUE:
            cfilter.setHighBlue(val);
            break;
        case UPPER_GREEN:
            cfilter.setHighGreen(val);
            break;
        case UPPER_RED:
            cfilter.setHighRed(val);
            break;
        case UPPER_ALPHA:
            cfilter.setHighAlpha(val);
            break;
        default:
            break;
        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {}

}
