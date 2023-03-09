package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.metadata.ExifData;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

public class ImageMetadataPanel {

    private Image image;
    private JPanel topPanel;
    private JPanel panelBaseData;
    private JPanel panelDetailData;
    private JTable tableBaseData;
    private JTable tableDetailData;
    private JScrollPane scrollPaneDetailData;
    private JScrollPane scrollPaneBaseData;
    private JTextArea textAreaInfo;
    private JLabel lblName;
    private JLabel lblDateTime;
    private JSplitPane splitPaneImageMetadata;

    private ExifData exiftool;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public void setImage(Image image) {
        this.image = image;
        lblName.setText( String.format( "%s (%s)", image.getName(), image.getType() ) );
        lblDateTime.setText( String.format( Config.getInstance().getL10n().getString("metadatapanel_capture.time"), image.getMetadata().getDate(), image.getMetadata().getTime() ) );
        intitializeBaseDataTable();
        intitializeDetailDataTable();
    }

    private void intitializeBaseDataTable() {
        String[] columnNames = {
                Config.getInstance().getL10n().getString("common_property"),
                Config.getInstance().getL10n().getString("common_value"),
                Config.getInstance().getL10n().getString("common_description")
        };

        List<List<String>> rows = image.getMetadata().baseDataAs2DArray();

        TableModel dataModel = new TableModelMetadata(columnNames, rows);
        setTableFormatting(tableBaseData);
        tableBaseData.setModel(dataModel);
    }

    private void intitializeDetailDataTable() {
        String[] columnNames = {
                Config.getInstance().getL10n().getString("common_property"),
                Config.getInstance().getL10n().getString("common_value"),
                Config.getInstance().getL10n().getString("common_description")
        };

        List<List<String>> rows = image.getMetadata().detailDataAs2DArray();

        TableModel dataModel = new TableModelMetadata(columnNames, rows);
        setTableFormatting(tableDetailData);
        tableDetailData.setModel(dataModel);
    }

    private void setTableFormatting(JTable table) {
        Font font = table.getTableHeader().getFont();
        table.getTableHeader().setFont(font.deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

}

class TableModelMetadata extends AbstractTableModel {

    private final String[] columnNames;
    private final List<List<String>> rows;

    public TableModelMetadata(String[] columnNames, List<List<String>> rows) {
        this.columnNames = columnNames;
        this.rows = rows;
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int row, int column) {
        return rows.get(row).get(column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
}
