package tracking.gui;

import javax.swing.table.DefaultTableModel;

public class CustomTableModel extends DefaultTableModel {

    /**
     * A serialisazion ID
     */
    private static final long serialVersionUID = -3924996961185277043L;

    /**
     * Number of rows.
     */
    private int row;

    /**
     * Number of colums.
     */
    private int col;

    /**
     * Types of the columns.
     */
    private Class<?>[] types;

    /**
     * Boolean if a column is editable.
     */
    private boolean[] columnIsEditable;

    /**
     * Min and max value of every field in a column.
     */
    private double[][][] minMaxValue;

    /**
     * Min and max value of every field in a column.
     */
    private int[][][] minMaxValueInt;

    private boolean intValue;
    private boolean[][] hasMinMax;

    /**
     * Create a table model, including table content and column identifiers. With min-max double
     * value for editable fields.
     * 
     * @param dataVector
     *            the data of the Table.
     * @param columnIdentifiers
     *            names of the columns.
     * @param columnIsEditable
     *            tells if a column is editable.
     * @param minMaxValue
     *            min and max values for the fields in the table. it's [row][column][x] min value
     *            for x=0 and max value for x=1. array must have same size as dataVector
     */
    public CustomTableModel(Object[][] dataVector, Object[] columnIdentifiers,
            boolean[] columnIsEditable, double[][][] minMaxValue) {
        this.row = dataVector.length;
        this.col = dataVector[0].length;
        setDataVector(dataVector, columnIdentifiers);
        this.columnIsEditable = columnIsEditable;
        this.types = new Class<?>[dataVector[0].length];
        for (int i = 0; i < dataVector[0].length; i++) {
            this.types[i] = dataVector[0][i].getClass();
        }
        this.minMaxValue = minMaxValue;
        this.intValue = false;

        hasMinMax = new boolean[dataVector.length][dataVector[0].length];
        // set hasMinMax.
        for (int i = 0; i < dataVector.length; i++) {
            for (int j = 0; j < dataVector[0].length; j++) {
                if (minMaxValue[i][j].length == 2
                        && minMaxValue[i][j][0] != minMaxValue[i][j][1]) {
                    hasMinMax[i][j] = true;
                } else {
                    hasMinMax[i][j] = false;
                }
            }
        }
    }

    /**
     * Create a table model, including table content and column identifiers. With min-max double
     * value for editable fields.
     * 
     * @param dataVector
     *            the data of the Table.
     * @param columnIdentifiers
     *            names of the columns.
     * @param columnIsEditable
     *            tells if a column is editable.
     * @param minMaxValue
     *            min and max values for the fields in the table. it's [row][column][x] min value
     *            for x=0 and max value for x=1. array must have same size as dataVector
     */
    public CustomTableModel(Object[][] dataVector, Object[] columnIdentifiers,
            boolean[] columnIsEditable, int[][][] minMaxValue) {
        this.row = dataVector.length;
        this.col = dataVector[0].length;
        setDataVector(dataVector, columnIdentifiers);
        this.columnIsEditable = columnIsEditable;
        this.types = new Class<?>[dataVector[0].length];
        for (int i = 0; i < dataVector[0].length; i++) {
            this.types[i] = dataVector[0][i].getClass();
        }
        this.minMaxValueInt = minMaxValue;
        this.intValue = true;

        hasMinMax = new boolean[dataVector.length][dataVector[0].length];
        // set hasMinMax.
        for (int i = 0; i < dataVector.length; i++) {
            for (int j = 0; j < dataVector[0].length; j++) {
                if (minMaxValue[i][j].length == 2
                        && minMaxValue[i][j][0] != minMaxValue[i][j][1]) {
                    hasMinMax[i][j] = true;
                } else {
                    hasMinMax[i][j] = false;
                }
            }
        }
    }

    /**
     * Create a table model, including table content and column identifiers.
     * 
     * @param dataVector
     *            the content vector to fill the table
     * @param columnIdentifiers
     *            names of the columns
     * @param types
     *            class type of the columns
     * @param columnIsEditable
     *            boolean for each column, if it's editable or not.
     */
    public CustomTableModel(Object[][] dataVector, Object[] columnIdentifiers,
            boolean[] columnIsEditable) {
        this.row = dataVector.length;
        this.col = dataVector[0].length;
        setDataVector(dataVector, columnIdentifiers);
        this.columnIsEditable = columnIsEditable;
        this.types = new Class<?>[dataVector[0].length];
        for (int i = 0; i < dataVector[0].length; i++) {
            this.types[i] = dataVector[0][i].getClass();
        }
        this.intValue = false;
        hasMinMax = new boolean[dataVector.length][dataVector[0].length]; // must not be set.
                                                                          // boolean is false by
                                                                          // default.
    }


    @Override
    public boolean isCellEditable(int row, int column) {
        return columnIsEditable[column];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

        if (hasMinMax[rowIndex][columnIndex]) {
            if (aValue instanceof Integer || aValue instanceof Double) {
                if (intValue) {

                    if ((int) aValue < minMaxValueInt[rowIndex][columnIndex][0]) {
                        super.setValueAt(
                                minMaxValueInt[rowIndex][columnIndex][0],
                                rowIndex, columnIndex);
                    } else if ((int) aValue > minMaxValueInt[rowIndex][columnIndex][1]) {
                        super.setValueAt(
                                minMaxValueInt[rowIndex][columnIndex][1],
                                rowIndex, columnIndex);
                    } else {
                        super.setValueAt(aValue, rowIndex, columnIndex);
                    }
                } else {
                    if ((double) aValue < minMaxValue[rowIndex][columnIndex][0]) {
                        super.setValueAt(minMaxValue[rowIndex][columnIndex][0],
                                rowIndex, columnIndex);
                    } else if ((double) aValue > minMaxValue[rowIndex][columnIndex][1]) {
                        super.setValueAt(minMaxValue[rowIndex][columnIndex][1],
                                rowIndex, columnIndex);
                    } else {
                        super.setValueAt(aValue, rowIndex, columnIndex);
                    }
                }
            }
        } else {
            super.setValueAt(aValue, rowIndex, columnIndex);
        }

    }

    @Override
    public int getColumnCount() {
        return col;
    }

    @Override
    public int getRowCount() {
        return row;
    }

    @Override
    public Class<?> getColumnClass(int arg0) {
        return types[arg0];
    }

}
