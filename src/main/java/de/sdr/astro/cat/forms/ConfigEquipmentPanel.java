package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Camera;
import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.config.Mount;
import de.sdr.astro.cat.config.Telescope;
import de.sdr.astro.cat.forms.common.TableModelEquipment;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Vector;

public class ConfigEquipmentPanel {
    private JPanel topPanel;
    private JPanel panelTelescopes;
    private JPanel panelMounts;
    private JPanel panelCameras;
    private JTabbedPane tabbedPaneEquipment;
    private JPanel Equipment;
    private JTable tableTelescopes;
    private JButton btnNewTelescope;
    private JButton btnDeleteTelescope;
    private JTable tableCameras;
    private JTable tableMounts;
    private JButton btnAddCamera;
    private JButton btnDeleteCamera;
    private JButton btnAddMount;
    private JButton btnDeleteMount;
    private JButton btnSaveEquipment;
    private JScrollPane jScrollPaneCameras;
    private JButton btnCancel;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public ConfigEquipmentPanel() {

        btnSaveEquipment.addActionListener( actionEvent -> {
            // transfer table contents to Config
            Config.getInstance().updateTelescopes(((TableModelEquipment) tableTelescopes.getModel()).getTelescopeList());
            Config.getInstance().updateCameras(((TableModelEquipment) tableCameras.getModel()).getCameraList());
            Config.getInstance().updateMounts(((TableModelEquipment) tableMounts.getModel()).getMountList());

            // save Config
            Config.getInstance().saveConfig();
        } );
        btnNewTelescope.addActionListener( actionEvent -> {
            ((TableModelEquipment) tableTelescopes.getModel()).addRow();
            tableTelescopes.revalidate();
        } );
        btnAddCamera.addActionListener( actionEvent -> {
            ((TableModelEquipment) tableCameras.getModel()).addRow();
            tableCameras.revalidate();
        } );
        btnAddMount.addActionListener( actionEvent -> {
            ((TableModelEquipment) tableMounts.getModel()).addRow();
            tableMounts.revalidate();
        } );
        btnCancel.addActionListener( actionEvent -> {
            // two levels up shall be the dialog
            SwingUtilities.getRoot(getTopPanel()).setVisible(false);
        } );
    }

    void activatePanel() {
        inititalizeTables();
    }

    private void inititalizeTables() {
        intitializeTelescopeTable();
        intitializeCamerasTable();
        intitializeMountsTable();
    }

    private void intitializeTelescopeTable() {
        String[] columnNames = {
                "ID",
                Config.getInstance().getL10n().getString("common_name"),
                Config.getInstance().getL10n().getString("common_focallength"),
                Config.getInstance().getL10n().getString("common_aperture"),
                Config.getInstance().getL10n().getString("common_keywords")
        };
        Vector<Vector> rows = new Vector<>();
        for (Telescope t : Config.getInstance().getTelescopes()) {
            Vector row = new Vector();
            row.add(t.getId());
            row.add(t.getName());
            row.add(t.getFocalLength());
            row.add(t.getAperture());
            row.add(t.getKeywords());
            rows.add(row);
        }
        TableModel dataModel = new TableModelEquipment(columnNames, rows);
        setTableFormatting(tableTelescopes);
        tableTelescopes.setModel(dataModel);
    }

    private void intitializeCamerasTable() {
        String[] columnNames = {"ID", "Name", "xRes", "yRes", "xPixel-Größe", "yPixel-Größe", "Schlüsselwörter"};
        Vector<Vector> rows = new Vector<>();
        for (Camera c : Config.getInstance().getCameras()) {
            Vector row = new Vector();
            row.add(c.getId());
            row.add(c.getName());
            row.add(c.getXRes());
            row.add(c.getYRes());
            row.add(c.getPixXSize());
            row.add(c.getPixYSize());
            row.add(c.getKeywords());
            rows.add(row);
        }
        setTableFormatting(tableCameras);
        TableModel dataModel = new TableModelEquipment(columnNames, rows);
        tableCameras.setModel(dataModel);
    }

    private void intitializeMountsTable() {
        String[] columnNames = {"ID", "Name", "Schlüsselwörter"};
        Vector<Vector> rows = new Vector<>();
        for (Mount m : Config.getInstance().getMounts()) {
            Vector row = new Vector();
            row.add(m.getId());
            row.add(m.getName());
            row.add(m.getKeywords());
            rows.add(row);
        }
        setTableFormatting(tableMounts);
        TableModel dataModel = new TableModelEquipment(columnNames, rows);
        tableMounts.setModel(dataModel);
    }

    private void setTableFormatting(JTable table) {
        Font font = table.getTableHeader().getFont();
        table.getTableHeader().setFont(font.deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }
}
