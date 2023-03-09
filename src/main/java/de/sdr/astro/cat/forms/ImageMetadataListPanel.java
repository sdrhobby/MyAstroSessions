package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.model.LightImage;
import de.sdr.astro.cat.model.Model;
import de.sdr.astro.cat.util.Util;
import de.sdr.astro.cat.model.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ImageMetadataListPanel {
    private JPanel topPanel;
    private JLabel lblObjectName;
    private JLabel lblImageType;
    private JTable tableImageList;
    private JLabel lblSessionName;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public void initialize(Session session, String imageType, String filter) {
        lblObjectName.setText(session.getAstroObjectName());
        lblSessionName.setText(session.getName());
        String type = imageType + (( filter.isEmpty() ) ? "" : ": " + filter);
        lblImageType.setText(type);

        fillImageListTable(session, imageType, filter);
    }

    private void fillImageListTable(Session session, String imageType, String filter) {
        ArrayList<String> columnNameList = new ArrayList<>();
        columnNameList.add(Config.getInstance().getL10n().getString("common_name"));
        columnNameList.add(Config.getInstance().getL10n().getString("common_width"));
        columnNameList.add(Config.getInstance().getL10n().getString("common_height"));
        if (Model.LIGHTS.equals(imageType)) {
            columnNameList.add("Filter");
        }
        columnNameList.add("Exposure");
        columnNameList.add("ISO");
        columnNameList.add("Gain");
        columnNameList.add("Bias");
        String[] columnNames = columnNameList.toArray(new String[0]);

        Vector<Vector<String>> rows = new Vector<>();

        fillImageInfo(session, imageType, rows, filter);

        TableModel dataModel = new DefaultTableModel() {
            @Override
            public String getColumnName(int column) { return columnNames[column]; }

            public int getColumnCount() { return columnNames.length; }

            public int getRowCount() {  return rows.size();  }

            public Object getValueAt(int row, int col) {
                return rows.get(row).get(col);
            }

            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        Font font = tableImageList.getTableHeader().getFont();
        tableImageList.getTableHeader().setFont(font.deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) tableImageList.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        tableImageList.setModel(dataModel);
    }

    private void fillImageInfo(Session session, String imageType, Vector<Vector<String>> rows, String filter) {
        // take full list per imageType as default
        List<Image> images = session.getImageMap().get(imageType);

        // if imageType == LIGHTS and filter is given, then only take the images belonging to that filter
        if ( Model.LIGHTS.equals( imageType ) && ! filter.isEmpty() ) {
            images = session.createLightFiltersMap().get(filter);
        }

        images.forEach((image) -> {
            final Vector<String> r = new Vector<>();
            r.add(image.getName());
            r.add("" + image.getMetadata().getWidth());
            r.add("" + image.getMetadata().getHeight());
            if (Model.LIGHTS.equals(imageType))
                r.add("" + ((image instanceof LightImage) ? ((LightImage) image).getFilter() : ""));
            r.add(Util.formatExposure(image.getMetadata().getExposure()) + " s");
            r.add("" + nonNull( image.getMetadata().getIso() ));
            r.add("" + nonNull( image.getMetadata().getGain() ));
            r.add("" + nonNull( image.getMetadata().getBias() ));
            rows.add(r);
        });
    }

    private String nonNull(Object o) {
        return o != null ? o.toString() : "";
    }

}
