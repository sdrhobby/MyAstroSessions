package de.sdr.astro.cat.forms;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.sdr.astro.cat.config.Camera;
import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.config.Mount;
import de.sdr.astro.cat.config.Telescope;
import de.sdr.astro.cat.forms.common.TableModelEquipment;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
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

        btnSaveEquipment.addActionListener(actionEvent -> {
            // transfer table contents to Config
            Config.getInstance().updateTelescopes(((TableModelEquipment) tableTelescopes.getModel()).getTelescopeList());
            Config.getInstance().updateCameras(((TableModelEquipment) tableCameras.getModel()).getCameraList());
            Config.getInstance().updateMounts(((TableModelEquipment) tableMounts.getModel()).getMountList());

            // save Config
            Config.getInstance().saveConfig();
        });
        btnNewTelescope.addActionListener(actionEvent -> {
            ((TableModelEquipment) tableTelescopes.getModel()).addRow();
            tableTelescopes.revalidate();
        });
        btnAddCamera.addActionListener(actionEvent -> {
            ((TableModelEquipment) tableCameras.getModel()).addRow();
            tableCameras.revalidate();
        });
        btnAddMount.addActionListener(actionEvent -> {
            ((TableModelEquipment) tableMounts.getModel()).addRow();
            tableMounts.revalidate();
        });
        btnCancel.addActionListener(actionEvent -> {
            // two levels up shall be the dialog
            SwingUtilities.getRoot(getTopPanel()).setVisible(false);
        });
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

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout(0, 0));
        Equipment = new JPanel();
        Equipment.setLayout(new GridBagLayout());
        topPanel.add(Equipment, BorderLayout.CENTER);
        panelTelescopes = new JPanel();
        panelTelescopes.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        Equipment.add(panelTelescopes, gbc);
        panelTelescopes.setBorder(BorderFactory.createTitledBorder(null, this.$$$getMessageFromBundle$$$("l10n", "equipment_caption.optics"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelTelescopes.add(scrollPane1, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        tableTelescopes = new JTable();
        tableTelescopes.setAutoResizeMode(4);
        scrollPane1.setViewportView(tableTelescopes);
        btnDeleteTelescope = new JButton();
        btnDeleteTelescope.setEnabled(false);
        this.$$$loadButtonText$$$(btnDeleteTelescope, this.$$$getMessageFromBundle$$$("l10n", "equipment_delete.selected"));
        panelTelescopes.add(btnDeleteTelescope, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnNewTelescope = new JButton();
        this.$$$loadButtonText$$$(btnNewTelescope, this.$$$getMessageFromBundle$$$("l10n", "equipment_new.telescope"));
        panelTelescopes.add(btnNewTelescope, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelCameras = new JPanel();
        panelCameras.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        Equipment.add(panelCameras, gbc);
        panelCameras.setBorder(BorderFactory.createTitledBorder(null, this.$$$getMessageFromBundle$$$("l10n", "common_cameras"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        jScrollPaneCameras = new JScrollPane();
        panelCameras.add(jScrollPaneCameras, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableCameras = new JTable();
        jScrollPaneCameras.setViewportView(tableCameras);
        btnAddCamera = new JButton();
        this.$$$loadButtonText$$$(btnAddCamera, this.$$$getMessageFromBundle$$$("l10n", "equipment_new.camera"));
        panelCameras.add(btnAddCamera, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnDeleteCamera = new JButton();
        btnDeleteCamera.setEnabled(false);
        this.$$$loadButtonText$$$(btnDeleteCamera, this.$$$getMessageFromBundle$$$("l10n", "equipment_delete.selected"));
        panelCameras.add(btnDeleteCamera, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelMounts = new JPanel();
        panelMounts.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        Equipment.add(panelMounts, gbc);
        panelMounts.setBorder(BorderFactory.createTitledBorder(null, this.$$$getMessageFromBundle$$$("l10n", "equipment_mounts"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JScrollPane scrollPane2 = new JScrollPane();
        panelMounts.add(scrollPane2, new GridConstraints(0, 0, 2, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableMounts = new JTable();
        scrollPane2.setViewportView(tableMounts);
        btnAddMount = new JButton();
        this.$$$loadButtonText$$$(btnAddMount, this.$$$getMessageFromBundle$$$("l10n", "equipment_new.mount"));
        panelMounts.add(btnAddMount, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnDeleteMount = new JButton();
        btnDeleteMount.setEnabled(false);
        this.$$$loadButtonText$$$(btnDeleteMount, this.$$$getMessageFromBundle$$$("l10n", "equipment_delete.selected"));
        panelMounts.add(btnDeleteMount, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnSaveEquipment = new JButton();
        this.$$$loadButtonText$$$(btnSaveEquipment, this.$$$getMessageFromBundle$$$("l10n", "common_save"));
        panelMounts.add(btnSaveEquipment, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        btnCancel = new JButton();
        this.$$$loadButtonText$$$(btnCancel, this.$$$getMessageFromBundle$$$("l10n", "common_cancel"));
        panelMounts.add(btnCancel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        Equipment.add(spacer1, gbc);
    }

    private static Method $$$cachedGetBundleMethod$$$ = null;

    private String $$$getMessageFromBundle$$$(String path, String key) {
        ResourceBundle bundle;
        try {
            Class<?> thisClass = this.getClass();
            if ($$$cachedGetBundleMethod$$$ == null) {
                Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
                $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
            }
            bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle(path);
        }
        return bundle.getString(key);
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }

}
