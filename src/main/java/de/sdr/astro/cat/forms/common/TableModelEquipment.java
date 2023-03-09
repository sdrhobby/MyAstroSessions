package de.sdr.astro.cat.forms.common;

import de.sdr.astro.cat.config.Camera;
import de.sdr.astro.cat.config.Mount;
import de.sdr.astro.cat.config.Telescope;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TableModelEquipment extends AbstractTableModel {

    private final Vector<Vector> rows;
    private final String[] columnNames;

    public TableModelEquipment(String[] columnNames, Vector<Vector> rows) {
        this.rows = rows;
        this.columnNames = columnNames;
    }

    private Telescope getTelescopeFromRow(int rowIndex) {
        return Telescope.fromVector(rows.get(rowIndex));
    }
    private Camera getCameraFromRow(int rowIndex) {
        return Camera.fromVector(rows.get(rowIndex));
    }
    private Mount getMountFromRow(int rowIndex) {
        return Mount.fromVector(rows.get(rowIndex));
    }

    public List<Telescope> getTelescopeList() {
        List<Telescope> list = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            list.add(getTelescopeFromRow(i));
        }
        return list;
    }

    public List<Camera> getCameraList() {
        List<Camera> list = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            list.add(getCameraFromRow(i));
        }
        return list;
    }

    public List<Mount> getMountList() {
        List<Mount> list = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            list.add(getMountFromRow(i));
        }
        return list;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return rows.size();
    }

    public Object getValueAt(int row, int col) {
        return rows.get(row).get(col);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        rows.get(rowIndex).set(columnIndex, aValue);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column > 0;
    }

    public void addRow() {
        Vector newRow = new Vector();
        // init new row
        for (int i = 0; i < getColumnCount(); i++) {
            newRow.add("");
        }
        newRow.set(0, getRowCount()+1);
        rows.add(newRow);
    }

}
