package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.common.*;

import de.sdr.astro.cat.model.AstroObject;
import de.sdr.astro.cat.model.Model;
import de.sdr.astro.cat.model.OverlayInfo;
import de.sdr.astro.cat.model.PointDouble;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Map<String, OverlayInfo> mapOverlayInfos = new HashMap();

    private OverlayImageDisplayPanel imageDisplayPanel;
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
            imageDisplayPanel = new OverlayImageDisplayPanel(img);
            panelMap.add(imageDisplayPanel, BorderLayout.CENTER);
        } catch (Exception x) {
            System.err.println(x.getMessage());
        }

        // get known OverlayInfos from the Configuration and transfer it to our internal map-structure
        List<OverlayInfo> oiList = Config.getInstance().getOverlayInfos();
        for (OverlayInfo oi : oiList) {
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
                    OverlayInfo oi = new OverlayInfo(label, p);
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
                    imageDisplayPanel.clearOverlays();
                    String aoName = ((String) itemEvent.getItem());
                    // check if we have an overlay info for that AstroObject name
                    OverlayInfo oi = mapOverlayInfos.get(aoName);
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
                    imageDisplayPanel.setOverays(mapOverlayInfos.values());
                } else {
                    // remove all
                    imageDisplayPanel.clearOverlays();
                    // only add the currently selected
                    String ao = (String) comboBoxAstroObject.getSelectedItem();
                    imageDisplayPanel.addOverlay(mapOverlayInfos.get(ao));
                }
                btnDefine.setEnabled(! checkBoxShowAll.isSelected());
                btnClear.setEnabled(! checkBoxShowAll.isSelected());
                comboBoxAstroObject.setEnabled(! checkBoxShowAll.isSelected() );
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

    public void selectAstroObject( AstroObject astroObject ) {
        checkBoxShowAll.setSelected(false);
        comboBoxAstroObject.setSelectedItem( astroObject.getName() );
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
}
