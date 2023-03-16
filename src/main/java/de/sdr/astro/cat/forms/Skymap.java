package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.common.*;

import de.sdr.astro.cat.model.AstroObject;
import de.sdr.astro.cat.model.Model;
import de.sdr.astro.cat.gui.overlays.Overlay;
import de.sdr.astro.cat.gui.overlays.SkymapLabel;
import de.sdr.astro.cat.model.PointDouble;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

/***
 * Skymap displays a panel with a screenshot of the nightsky.
 * It provides interaction elements that allow to place and manage named markers for the astro-object as overlay on that image.
 * TODO: allow custom images instead of the embedded one
 * TODO: add interactivity to the markers --> allow navigation to the corresponding session(s)
 */
public class Skymap {
    private static final String SKYMAP_IMAGE = "/skymap-kstars.png";
    private JPanel topPanel;
    private JPanel panelControls;
    private JComboBox comboBoxAstroObject;
    private JButton btnSave;
    private JPanel panelMap;
    private JButton btnDefine;
    private JButton btnClear;
    private JCheckBox checkBoxShowAll;

    private JDialog parentDialog;
    private boolean editMode = false;
    private boolean changed = false;
    private Map<String, SkymapLabel> mapOverlayInfos = new HashMap();

    private ImageDisplayPanel imageDisplayPanel;
    private Dimension imageSize;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public void setParentDialog(JDialog parentDialog) {
        this.parentDialog = parentDialog;
        if (parentDialog != null) {
            parentDialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosing(e);
                    if (changed) {
                        int answer = JOptionPane.showConfirmDialog(
                                null,
                                "Vor dem Verlassen Speichern?",
                                "Speichern nicht vergessen!",
                                JOptionPane.YES_NO_OPTION);
                        if (answer == 0) {
//                            SwingUtilities.getRoot(getTopPanel()).setVisible(false);
                            saveData();
                        }
                    }
                }
            });
            parentDialog.setSize(imageSize);
        }
    }

    public Skymap() {
        try {
            BufferedImage img = ImageIO.read(this.getClass().getResourceAsStream(SKYMAP_IMAGE));
            imageSize = new Dimension(img.getWidth(), img.getHeight());
            imageDisplayPanel = new ImageDisplayPanel(img);
            panelMap.add(imageDisplayPanel, BorderLayout.CENTER);
        } catch (Exception x) {
            System.err.println(x.getMessage());
        }

        // get known OverlayInfos from the Configuration and transfer it to our internal map-structure
        List<SkymapLabel> oiList = Config.getInstance().getOverlayInfos();
        for (SkymapLabel oi : oiList) {
            mapOverlayInfos.put(oi.getLabel(), oi);
        }

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                saveData();
            }
        });
        btnDefine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                imageDisplayPanel.clearOverlays();
                toggleEditMode();
            }
        });
        imageDisplayPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (editMode) {
                    toggleEditMode();
                    PointDouble p = imageDisplayPanel.translatePanelToRelativeImageCoord(e.getPoint());
                    String label = comboBoxAstroObject.getSelectedItem().toString();
                    SkymapLabel oi = new SkymapLabel(label, p, Color.YELLOW);
                    imageDisplayPanel.addOverlay(oi);
                    mapOverlayInfos.put(label, oi);
                    changed = true;
                }
            }
        });

        comboBoxAstroObject.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                System.out.println("AstroObject selection changed: " + itemEvent.getItem());
                if (comboBoxAstroObject.getItemCount() > 0) {
                    String aoName = ((String) itemEvent.getItem());
                    imageDisplayPanel.clearOverlays();
                    // check if we have an overlay info for that AstroObject name
                    SkymapLabel oi = mapOverlayInfos.get(aoName);
                    if (oi != null) {
                        // if yes, show it as overlay (for now, we are only showing one object at a time)
                        imageDisplayPanel.addOverlay(oi);
                    }
                }
            }
        });
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String aoName = (String) comboBoxAstroObject.getSelectedItem();
                mapOverlayInfos.remove(aoName);
                imageDisplayPanel.clearOverlays();
                changed = true;
            }
        });

        checkBoxShowAll.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (checkBoxShowAll.isSelected()) {
                    // dirty cast to Collection<Overlay>
                    imageDisplayPanel.replaceOverlays((Collection<Overlay>) (Object) mapOverlayInfos.values());
                } else {
                    // remove all
                    imageDisplayPanel.clearOverlays();
                    // only add the currently selected
                    String ao = (String) comboBoxAstroObject.getSelectedItem();
                    imageDisplayPanel.addOverlay(mapOverlayInfos.get(ao));
                }
                btnDefine.setEnabled(!checkBoxShowAll.isSelected());
                btnClear.setEnabled(!checkBoxShowAll.isSelected());
                comboBoxAstroObject.setEnabled(!checkBoxShowAll.isSelected());
            }
        });

    }

    private void saveData() {
        // update Configuration with list of OverlayInfos and save it
        Config.getInstance().updateOverlayInfos(mapOverlayInfos.values());
        Config.getInstance().saveConfig();
        changed = false;
    }

    public void updateDataModel(Model dataModel) {
        comboBoxAstroObject.removeAllItems();
        // fill combobox with all AstroObjects we have sessions for
        for (AstroObject ao : dataModel.getAstroObjects()) {
            addAstroObjectToCombo(comboBoxAstroObject, ao);
        }
    }

    public void selectAstroObject(AstroObject astroObject) {
        checkBoxShowAll.setSelected(false);
        comboBoxAstroObject.setSelectedItem(astroObject.getName());
    }

    private void addAstroObjectToCombo(JComboBox combo, AstroObject astroObject) {
        if (astroObject.hasSessions()) {
            combo.addItem(astroObject.getName());
        }
        for (AstroObject ao : astroObject.getAstroObjects()) {
            addAstroObjectToCombo(combo, ao);
        }
    }

    private void toggleEditMode() {
        editMode = !editMode;
        if (editMode) {
            SwingUtilities.getRoot(getTopPanel()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            SwingUtilities.getRoot(getTopPanel()).setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
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
        topPanel.setPreferredSize(new Dimension(800, 900));
        panelControls = new JPanel();
        panelControls.setLayout(new GridBagLayout());
        topPanel.add(panelControls, BorderLayout.NORTH);
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("l10n", "skymap_choose.object"));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        panelControls.add(label1, gbc);
        comboBoxAstroObject = new JComboBox();
        comboBoxAstroObject.setEditable(false);
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panelControls.add(comboBoxAstroObject, gbc);
        btnDefine = new JButton();
        btnDefine.setLabel("Position festlegen");
        this.$$$loadButtonText$$$(btnDefine, this.$$$getMessageFromBundle$$$("l10n", "skymap_set.position"));
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        panelControls.add(btnDefine, gbc);
        btnClear = new JButton();
        this.$$$loadButtonText$$$(btnClear, this.$$$getMessageFromBundle$$$("l10n", "skymap_clear.position"));
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        panelControls.add(btnClear, gbc);
        btnSave = new JButton();
        this.$$$loadButtonText$$$(btnSave, this.$$$getMessageFromBundle$$$("l10n", "common_save"));
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        panelControls.add(btnSave, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelControls.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelControls.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelControls.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelControls.add(spacer4, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelControls.add(spacer5, gbc);
        checkBoxShowAll = new JCheckBox();
        this.$$$loadButtonText$$$(checkBoxShowAll, this.$$$getMessageFromBundle$$$("l10n", "skymap_show.all"));
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panelControls.add(checkBoxShowAll, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelControls.add(spacer6, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelControls.add(spacer7, gbc);
        panelMap = new JPanel();
        panelMap.setLayout(new BorderLayout(0, 0));
        topPanel.add(panelMap, BorderLayout.CENTER);
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
    private void $$$loadLabelText$$$(JLabel component, String text) {
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
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
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
