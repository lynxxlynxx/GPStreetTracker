package tracking.gui.plugins.hough.colorfilter.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSlider;
import javax.swing.JTextField;

import tracking.gui.plugins.hough.colorfilter.ColorFilterPanel;
import tracking.model.plugins.hough.colorfilter.ColorFilter;

public class ColorFilterTextFieldListener implements ActionListener {

    private JSlider value;
    private ColorFilterPanel.Slider type;
    private ColorFilter cfilter;

    /**
     * {@link ActionListener} for {@link JTextField} with a {@link JSlider}
     * 
     * @param cfilter
     *            the {@link ColorFilter}
     * @param value
     *            the {@link JSlider} shows the value of the {@link JTextField}
     * @param slider
     *            the {@link FilterSlider} type
     */
    public ColorFilterTextFieldListener(ColorFilter cfilter, JSlider value,
            ColorFilterPanel.Slider type) {
        this.value = value;
        this.cfilter = cfilter;
        this.type = type;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextField field = (JTextField) e.getSource();

        try {
            Double val = new Double(Double.parseDouble(e.getActionCommand()));

            if (val < 0) {
                val = 0.0;
                value.setValue(0);
                field.setText("" + 0);
            } else if (val > 255) {
                val = 255.0;
                value.setValue(255);
                field.setText("" + 255);
            }
            value.setValue(val.intValue());

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
        } catch (NumberFormatException nfe) {
            field.setText("" + value.getValue());
        }

    }
}
