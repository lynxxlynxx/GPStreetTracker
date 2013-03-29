package tracking.gui.plugins.hough.colorfilter;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import com.googlecode.javacv.CameraDevice.Settings;

import tracking.gui.CustomTableModel;
import tracking.model.plugins.hough.HoughSignRecognition;

public class SettingsPanel extends JPanel {

    /**
     * A serialisation ID.
     */
    private static final long serialVersionUID = 2146165007600641906L;

    /*
     * The following are default values to build the tables for the settings parameters.
     */
    private final int ROW_HEIGHT = 20;

    private JTable cvContourBlur, trackCircleBlur, trackCircle,
            trackPolygonBlur, trackPolygon, signTracker, shapeDetection,
            blobDetection;

    private final boolean[] editable = { false, true };

    private final String[] names = { "Name", "Value" };

    private JPanel panelCvContourBlur, panelTrackCircleBlur, panelTrackCircle,
            panelTrackPolygonBlur, panelTrackPolygon, panelSignTracker,
            panelShapeDetection, panelBlobDetection;

    private final String nameCvContourBlur = "Contour detection",
            nameTrackCircleBlur = "Circle detection blur",
            nameTrackCircle = "Circle detection",
            nameTrackPolygonBlur = "Polygon detection blur",
            nameTrackPolygon = "Polygon detection",
            nameSignTracker = "Sign tracker",
            nameShapeDetection = "Shape detection",
            nameBlobDetection = "Blob detection";

    private HoughSignRecognition model;

    /**
     * Constructor for the {@link SettingsPanel}. In the {@link Settings}, the user can set all
     * parameters for the plugin {@link HoughSignRecognition}.
     * 
     * @param model
     *            the plugin HoughSignRecognition.
     */
    public SettingsPanel(HoughSignRecognition model) {

        this.model = model;

        int[] cvContourBlurValue = model.getCvContourValue();
        final Object[][] dataCvContourBlur = {
                { "Kernel size x", cvContourBlurValue[0] },
                { "Kernel size y", cvContourBlurValue[1] } };

        int[] trackCircleBlurValue = model.getTrackCircleBlurValue();
        final Object[][] dataTrackCircleBlur = {
                { "Kernel size x", trackCircleBlurValue[0] },
                { "Kernel size y", trackCircleBlurValue[1] } };

        int[] trackCircleValue = model.getTrackCircleValue();
        final Object[][] dataTrackCircle = {
                { "Akkumulator size. 1 is full size, 2 is half size, etc.",
                        trackCircleValue[0] },
                { "Minimum distance between centers of detected circles",
                        trackCircleValue[1] },
                {
                        "Higher threshold for canny edge detector,\nlower one is set twice smaller",
                        trackCircleValue[2] },
                {
                        "Accumulator threshold for the circle centers at the detection stage",
                        trackCircleValue[3] },
                { "Minimum circle radius", trackCircleValue[4] },
                { "Maximum circle radius", trackCircleValue[5] } };

        int[] trackPolygonBlurValue = model.getTrackPolygonBlurValue();
        final Object[][] dataTrackPolygonBlur = {
                { "Kernel size x", trackPolygonBlurValue[0] },
                { "Kernel size y", trackPolygonBlurValue[1] } };

        int[] trackPolygonValue = model.getTrackPolygonValue();
        final Object[][] dataTrackPolygon = {
                { "Line detection threshold", trackPolygonValue[0] },
                { "Lower threshold for canny edge hysteresis procedure",
                        trackPolygonValue[1] },
                { "Higher threshold for canny edge hysteresis procedure",
                        trackPolygonValue[2] } };

        int[] signTrackerValue = model.getSignTrackerValue();
        final Object[][] dataSignTracker = {
                { "Maximum line distance", signTrackerValue[0] },
                { "Maximum point distance", signTrackerValue[1] },
                { "Maximum frame delay", signTrackerValue[2] } };

        double[] shapeDetectionValue = model.getShapeDetectionValue();
        final Object[][] dataShapeDetection = {
                { "Verification angle threshold", shapeDetectionValue[0] },
                { "CleanLines angle threshold", shapeDetectionValue[1] },
                { "CleanLines distance threshold", shapeDetectionValue[2] } };

        int[] blobDetectionValue = model.getBlobDetectionValue();
        final Object[][] dataBlobDetection = {
                { "Maximum blob count", (int) blobDetectionValue[0] },
                { "Minimum blob size in pixel", (int) blobDetectionValue[1] },
                { "Maximum blob size in pixel", (int) blobDetectionValue[2] } };

        CustomTableModel modelCvContourBlur = new CustomTableModel(
                dataCvContourBlur, names, editable,
                createMinMax(model.getCvContourMinMax()));
        cvContourBlur = new JTable(modelCvContourBlur);
        cvContourBlur.setRowHeight(ROW_HEIGHT);
        cvContourBlur.getTableHeader().setReorderingAllowed(false);
        panelCvContourBlur = new JPanel(new BorderLayout());
        panelCvContourBlur.setBorder(new TitledBorder(nameCvContourBlur));
        panelCvContourBlur.add(cvContourBlur.getTableHeader(),
                BorderLayout.PAGE_START);
        panelCvContourBlur.add(cvContourBlur, BorderLayout.CENTER);

        CustomTableModel modelTrackCircleBlur = new CustomTableModel(
                dataTrackCircleBlur, names, editable,
                createMinMax(model.getTrackCircleBlurMinMax()));
        trackCircleBlur = new JTable(modelTrackCircleBlur);
        trackCircleBlur.setRowHeight(ROW_HEIGHT);
        trackCircleBlur.getTableHeader().setReorderingAllowed(false);
        panelTrackCircleBlur = new JPanel(new BorderLayout());
        panelTrackCircleBlur.setBorder(new TitledBorder(nameTrackCircleBlur));
        panelTrackCircleBlur.add(trackCircleBlur.getTableHeader(),
                BorderLayout.PAGE_START);
        panelTrackCircleBlur.add(trackCircleBlur, BorderLayout.CENTER);

        CustomTableModel modelTrackCircle = new CustomTableModel(
                dataTrackCircle, names, editable,
                createMinMax(model.getTrackCircleMinMax()));
        trackCircle = new JTable(modelTrackCircle);
        trackCircle.setRowHeight(ROW_HEIGHT);
        trackCircle.getTableHeader().setReorderingAllowed(false);
        panelTrackCircle = new JPanel(new BorderLayout());
        panelTrackCircle.setBorder(new TitledBorder(nameTrackCircle));
        panelTrackCircle.add(trackCircle.getTableHeader(),
                BorderLayout.PAGE_START);
        panelTrackCircle.add(trackCircle, BorderLayout.CENTER);

        CustomTableModel modelTrackPolygonBlur = new CustomTableModel(
                dataTrackPolygonBlur, names, editable,
                createMinMax(model.getTrackPolygonBlurMinMax()));
        trackPolygonBlur = new JTable(modelTrackPolygonBlur);
        trackPolygonBlur.setRowHeight(ROW_HEIGHT);
        trackPolygonBlur.getTableHeader().setReorderingAllowed(false);
        panelTrackPolygonBlur = new JPanel(new BorderLayout());
        panelTrackPolygonBlur.setBorder(new TitledBorder(nameTrackPolygonBlur));
        panelTrackPolygonBlur.add(trackPolygonBlur.getTableHeader(),
                BorderLayout.PAGE_START);
        panelTrackPolygonBlur.add(trackPolygonBlur, BorderLayout.CENTER);

        CustomTableModel modelTrackPolygon = new CustomTableModel(
                dataTrackPolygon, names, editable,
                createMinMax(model.getTrackPolygonMinMax()));
        trackPolygon = new JTable(modelTrackPolygon);
        trackPolygon.setRowHeight(ROW_HEIGHT);
        trackPolygon.getTableHeader().setReorderingAllowed(false);
        panelTrackPolygon = new JPanel(new BorderLayout());
        panelTrackPolygon.setBorder(new TitledBorder(nameTrackPolygon));
        panelTrackPolygon.add(trackPolygon.getTableHeader(),
                BorderLayout.PAGE_START);
        panelTrackPolygon.add(trackPolygon, BorderLayout.CENTER);

        CustomTableModel modelSignTracker = new CustomTableModel(
                dataSignTracker, names, editable,
                createMinMax(model.getSignTrackerMinMax()));
        signTracker = new JTable(modelSignTracker);
        signTracker.setRowHeight(ROW_HEIGHT);
        signTracker.getTableHeader().setReorderingAllowed(false);
        panelSignTracker = new JPanel(new BorderLayout());
        panelSignTracker.setBorder(new TitledBorder(nameSignTracker));
        panelSignTracker.add(signTracker.getTableHeader(),
                BorderLayout.PAGE_START);
        panelSignTracker.add(signTracker, BorderLayout.CENTER);

        CustomTableModel modelShapeDetection = new CustomTableModel(
                dataShapeDetection, names, editable,
                createMinMax(model.getShapeDetectionMinMax()));
        shapeDetection = new JTable(modelShapeDetection);
        shapeDetection.setRowHeight(ROW_HEIGHT);
        shapeDetection.getTableHeader().setReorderingAllowed(false);
        panelShapeDetection = new JPanel(new BorderLayout());
        panelShapeDetection.setBorder(new TitledBorder(nameShapeDetection));
        panelShapeDetection.add(shapeDetection.getTableHeader(),
                BorderLayout.PAGE_START);
        panelShapeDetection.add(shapeDetection, BorderLayout.CENTER);

        CustomTableModel modelBlobDetection = new CustomTableModel(
                dataBlobDetection, names, editable,
                createMinMax(model.getBlobDetectionMinMax()));
        blobDetection = new JTable(modelBlobDetection);
        blobDetection.setRowHeight(ROW_HEIGHT);
        blobDetection.getTableHeader().setReorderingAllowed(false);

        panelBlobDetection = new JPanel(new BorderLayout());
        panelBlobDetection.setBorder(new TitledBorder(nameBlobDetection));
        panelBlobDetection.add(blobDetection.getTableHeader(),
                BorderLayout.PAGE_START);
        panelBlobDetection.add(blobDetection, BorderLayout.CENTER);

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.5;
        gbc.weighty = 0.5;

        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(panelCvContourBlur, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        this.add(panelBlobDetection, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        this.add(panelSignTracker, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        this.add(panelShapeDetection, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(panelTrackCircleBlur, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        this.add(panelTrackCircle, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        this.add(panelTrackPolygonBlur, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        this.add(panelTrackPolygon, gbc);

    }

    public void okButtonPressed() {
        model.setCvContourValue(
                (int) cvContourBlur.getModel().getValueAt(0, 1),
                (int) cvContourBlur.getModel().getValueAt(1, 1));

        model.setTrackCicleBlurValue((int) trackCircleBlur.getModel()
                .getValueAt(0, 1),
                (int) trackCircleBlur.getModel().getValueAt(1, 1));

        model.setTrackCircleValue(
                (int) trackCircle.getModel().getValueAt(0, 1),
                (int) trackCircle.getModel().getValueAt(1, 1),
                (int) trackCircle.getModel().getValueAt(2, 1),
                (int) trackCircle.getModel().getValueAt(3, 1),
                (int) trackCircle.getModel().getValueAt(4, 1),
                (int) trackCircle.getModel().getValueAt(5, 1));

        model.setTrackPolygonBlurValue((int) trackPolygonBlur.getModel()
                .getValueAt(0, 1), (int) trackPolygonBlur.getModel()
                .getValueAt(1, 1));

        model.setTrackPolygonValue(
                (int) trackPolygon.getModel().getValueAt(0, 1),
                (int) trackPolygon.getModel().getValueAt(1, 1),
                (int) trackPolygon.getModel().getValueAt(2, 1));

        model.setSignTrackerValue(
                (int) signTracker.getModel().getValueAt(0, 1),
                (int) signTracker.getModel().getValueAt(1, 1),
                (int) signTracker.getModel().getValueAt(2, 1));

        model.setShapeDetectionValue((double) shapeDetection.getModel()
                .getValueAt(0, 1), (double) shapeDetection.getModel()
                .getValueAt(1, 1), (double) shapeDetection.getModel()
                .getValueAt(2, 1));

        model.setBlobDetectionValue(
                (int) blobDetection.getModel().getValueAt(0, 1),
                (int) blobDetection.getModel().getValueAt(1, 1),
                (int) blobDetection.getModel().getValueAt(2, 1));
    }

    /**
     * Creates the matrix for the min and max values of a table.
     * 
     * @param minmax
     *            the matrix for the min max values of a column
     * @return the new 3D matrix with the table min max values.
     */
    private double[][][] createMinMax(double[][] minmax) {
        double[][][] ret = new double[minmax.length][2][2];
        for (int i = 0; i < minmax.length; i++) {
            ret[i][1][0] = minmax[i][0];
            ret[i][1][1] = minmax[i][1];
        }
        return ret;
    }

    /**
     * Creates the matrix for the min and max values of a table.
     * 
     * @param minmax
     *            the matrix for the min max values of a column
     * @return the new 3D matrix with the table min max values.
     */
    private int[][][] createMinMax(int[][] minmax) {
        int[][][] ret = new int[minmax.length][2][2];
        for (int i = 0; i < minmax.length; i++) {
            ret[i][1][0] = minmax[i][0];
            ret[i][1][1] = minmax[i][1];
        }
        return ret;
    }
}
