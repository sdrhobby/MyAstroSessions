package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.*;
import de.sdr.astro.cat.model.Session;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ResourceBundle;

/***
 * The SessionEquipmentPanel allows to display and assign Equipment belonging to a dedicated Session.
 * This Panel is used as part of the SessionInfoPanel as well as in the dialog for the setup of a new Session.
 *
 */
public class SessionEquipmentPanel {

    private JComboBox comboBoxMainCamera;
    private JComboBox comboBoxGuidingCamera;
    private JComboBox comboBoxGuidingTelescope;
    private JComboBox comboBoxMounts;
    private JComboBox comboBoxMainTelescope;
    private JButton btnSaveSessionEquipment;
    private JComboBox comboBoxProfil;
    private JPanel topPanel;
    private JPanel panelImagePreview;

    private final Session session;

    public JPanel getTopPanel() {
        return topPanel;
    }

    /**
     * Creates a new SessionEquipmentPanel for the given Session.
     * It will attempt to read a .astrocat.session file (a hidden file in Linux) in the Session folder and to display the stored equipment in the comboboxes.
     *
     * @param session        ... the session for which the Equipment shall be displayed or configured (may be null, if used for a new Session to be created)
     * @param showSaveButton ... shall the save button be shown or not
     */
    public SessionEquipmentPanel(Session session, boolean showSaveButton) {
        this.session = session;
        btnSaveSessionEquipment.setVisible(showSaveButton);

        comboBoxProfil.addItemListener(itemEvent -> {
            System.out.println("profile selection changed: " + itemEvent.getItem());
            if (itemEvent.getItem() instanceof Profile) {
                Profile profile = (Profile) itemEvent.getItem();
                selectComboItemById(comboBoxMainTelescope, profile.getMainTelescopeId());
                selectComboItemById(comboBoxMainCamera, profile.getMainCameraId());
                selectComboItemById(comboBoxGuidingTelescope, profile.getGuidingTelescopeId());
                selectComboItemById(comboBoxGuidingCamera, profile.getGuidingCameraId());
                selectComboItemById(comboBoxMounts, profile.getMountId());
            }
        });

        btnSaveSessionEquipment.addActionListener(actionEvent -> saveSessionEquipment(session));

        initializeEquipmentCombos();
    }

    private void initializeEquipmentCombos() {

        // clear all and fill fresh, in case that something has changed on the configured Equipment
        comboBoxProfil.removeAllItems();
        comboBoxMainCamera.removeAllItems();
        comboBoxGuidingCamera.removeAllItems();
        comboBoxMainTelescope.removeAllItems();
        comboBoxGuidingTelescope.removeAllItems();
        comboBoxMounts.removeAllItems();

        comboBoxProfil.addItem("-");
        comboBoxMainCamera.addItem("-");
        comboBoxGuidingCamera.addItem("-");
        comboBoxMainTelescope.addItem("-");
        comboBoxGuidingTelescope.addItem("-");
        comboBoxMounts.addItem("-");

        for (Profile profile : Config.getInstance().getProfiles()) {
            comboBoxProfil.addItem(profile);
        }
        for (Camera camera : Config.getInstance().getCameras()) {
            comboBoxMainCamera.addItem(camera);
            comboBoxGuidingCamera.addItem(camera);
        }
        for (Telescope telescope : Config.getInstance().getTelescopes()) {
            comboBoxMainTelescope.addItem(telescope);
            comboBoxGuidingTelescope.addItem(telescope);
        }
        for (Mount mount : Config.getInstance().getMounts()) {
            comboBoxMounts.addItem(mount);
        }

        // this panel might be used for creation of session - in this case session is still null
        // if we already have a session, read potentially assigned profile
        if (session != null) {
            Profile profile = Config.getInstance().readSessionProfile(session);
            if (profile != null) {
                selectComboItemById(comboBoxMainCamera, profile.getMainCameraId());
                selectComboItemById(comboBoxMainTelescope, profile.getMainTelescopeId());
                selectComboItemById(comboBoxGuidingCamera, profile.getGuidingCameraId());
                selectComboItemById(comboBoxGuidingTelescope, profile.getGuidingTelescopeId());
                selectComboItemById(comboBoxMounts, profile.getMountId());
            }

            // get camera entry from metadata of the lights
            String cameraStr = session.getCameraEntryFromMetadata();
            if (!cameraStr.isEmpty())
                // match this string against the keywords of the cameras in the Main Camera combo and select the one with the first match
                selectComboItemByKeywordMatch(comboBoxMainCamera, cameraStr);
        }
    }

    /**
     * The session equipment profile will be saved to a astrocat.session config file in the Session folder.
     *
     * @param session ... the corresponding session (to identify the session-folder)
     */
    public void saveSessionEquipment(Session session) {
        Profile profile = new Profile(
                session.getAstroObjectName() + "-" + session.getName(),
                getSelectedObjectId(comboBoxMainTelescope),
                getSelectedObjectId(comboBoxMainCamera),
                getSelectedObjectId(comboBoxGuidingTelescope),
                getSelectedObjectId(comboBoxGuidingCamera),
                getSelectedObjectId(comboBoxMounts)
        );
        Config.getInstance().saveSessionProfile(session, profile);
    }

    private int getSelectedObjectId(JComboBox combo) {
        return combo.getSelectedItem() instanceof Equipment ? ((Equipment) combo.getSelectedItem()).getEquipmentId() : -1;
    }

    private void selectComboItemById(JComboBox combo, int equipmentId) {
        if (equipmentId == -1) {
            combo.setSelectedItem("-");
        } else {
            for (int i = 1; i < combo.getItemCount(); i++) {
                Equipment e = ((Equipment) combo.getItemAt(i));
                if (e.getEquipmentId() == equipmentId) {
                    combo.setSelectedItem(e);
                    break;
                }

            }
        }
        combo.revalidate();
    }

    private void selectComboItemByKeywordMatch(JComboBox combo, String keyword) {
        if (keyword.isEmpty()) {
            combo.setSelectedItem("-");
        } else {
            for (int i = 1; i < combo.getItemCount(); i++) {
                Equipment e = ((Equipment) combo.getItemAt(i));
                String needle = e.getEquipmentKeywords().toLowerCase();
                String haystack = keyword.toLowerCase();
                if (haystack.contains(needle)) {
                    combo.setSelectedItem(e);
                    break;
                }

            }
        }
        combo.revalidate();
    }

    public String getEquipmentAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Config.getInstance().getL10n().getString("config_profiles_main.camera"));
        sb.append(": " + comboBoxMainCamera.getSelectedItem().toString() + "\n");
        sb.append(Config.getInstance().getL10n().getString("equipment_main.telescope"));
        sb.append(": " + comboBoxMainTelescope.getSelectedItem().toString() + "\n");
        sb.append(Config.getInstance().getL10n().getString("equipment_guiding.camera"));
        sb.append(": " + comboBoxGuidingCamera.getSelectedItem().toString() + "\n");
        sb.append(Config.getInstance().getL10n().getString("equipment_guiding.telescope"));
        sb.append(": " + comboBoxGuidingTelescope.getSelectedItem().toString() + "\n");
        sb.append(Config.getInstance().getL10n().getString("equipment_mount"));
        sb.append(": " + comboBoxMounts.getSelectedItem().toString() + "\n");
        return sb.toString();
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
        topPanel.setLayout(new GridBagLayout());
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("l10n", "equipment_main.camera"));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(label1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer2, gbc);
        comboBoxMainCamera = new JComboBox();
        comboBoxMainCamera.setToolTipText("Die Kamera wird aus den Bild-Metadaten automatisch anhand der konfiguierten Keywörter ermittelt (siehe File->Ausrüstung)");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(comboBoxMainCamera, gbc);
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("l10n", "equipment_guiding.camera"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(label2, gbc);
        comboBoxGuidingCamera = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 4;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(comboBoxGuidingCamera, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer3, gbc);
        comboBoxGuidingTelescope = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(comboBoxGuidingTelescope, gbc);
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("l10n", "equipment_guiding.telescope"));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(label3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer4, gbc);
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, this.$$$getMessageFromBundle$$$("l10n", "equipment_mount"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(label4, gbc);
        comboBoxMounts = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 6;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(comboBoxMounts, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer6, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 4;
        gbc.weightx = 0.1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer7, gbc);
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, this.$$$getMessageFromBundle$$$("l10n", "equipment_main.telescope"));
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(label5, gbc);
        comboBoxMainTelescope = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(comboBoxMainTelescope, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer8, gbc);
        btnSaveSessionEquipment = new JButton();
        this.$$$loadButtonText$$$(btnSaveSessionEquipment, this.$$$getMessageFromBundle$$$("l10n", "common_save"));
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(btnSaveSessionEquipment, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer9, gbc);
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$(label6, this.$$$getMessageFromBundle$$$("l10n", "common_profile"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(label6, gbc);
        comboBoxProfil = new JComboBox();
        comboBoxProfil.setToolTipText("Das Profile dient hier lediglich als Ausfüllhilfe. Gespeichert werden die einzelen Einträge.");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(comboBoxProfil, gbc);
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
