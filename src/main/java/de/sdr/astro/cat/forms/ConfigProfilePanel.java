package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ConfigProfilePanel {
    private static final int STATE_DISPLAY_NO_PROFILE_SELECTED = 0;
    private static final int STATE_DISPLAY_PROFILE_SELECTED = 1;
    private static final int STATE_EDIT = 2;
    private static final int STATE_NEW = 3;
    private int currentState;

    private JComboBox comboBoxProfiles;
    private JButton btnAdd;
    private JButton btnSave;
    private JButton btnDelete;
    private JComboBox comboBoxMainTelescope;
    private JComboBox comboBoxMainCamera;
    private JPanel panelImagingEqu;
    private JPanel panelGuidingEqu;
    private JComboBox comboBoxGuidingTelescope;
    private JComboBox comboBoxGuidingCamera;
    private JPanel panelMount;
    private JComboBox comboBoxMounts;
    private JButton btnProfileEdit;
    private JTextField textFieldNewProfileName;
    private JPanel topPanel;
    private JButton btnCancel;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public ConfigProfilePanel() {

        btnAdd.addActionListener(actionEvent -> switchState(STATE_NEW));
        btnProfileEdit.addActionListener(actionEvent -> switchState(STATE_EDIT));
        btnDelete.addActionListener(actionEvent -> {
            // TODO: delete profile
            Profile profile = (Profile) comboBoxProfiles.getSelectedItem();
            int answer = JOptionPane.showConfirmDialog(
                    null,
                    String.format(Config.getInstance().getL10n().getString("config_profile.confirm.msg"), profile),
                    Config.getInstance().getL10n().getString("config_profil.confirm.title"),
                    JOptionPane.YES_NO_OPTION);
            if (answer == 0) {
                // delete from combo box
                comboBoxProfiles.removeItem(profile);
                saveProfilesToConfig();
            }
        });
        btnSave.addActionListener(actionEvent -> {
            if (currentState == STATE_NEW) {
                comboBoxProfiles.addItem(createProfileFromSelections());
            } else {
                Profile changedProfile = createProfileFromSelections();
                comboBoxProfiles.removeItem(comboBoxProfiles.getSelectedItem());
                comboBoxProfiles.addItem(changedProfile);
            }
            saveProfilesToConfig();
            switchState(STATE_DISPLAY_PROFILE_SELECTED);
        });
        comboBoxProfiles.addItemListener(itemEvent -> {
            System.out.printf("profile selection changed: %s%n", itemEvent.getItem());
            if (comboBoxProfiles.getItemCount() > 0) {
                handleProfileSelectionChanged((Profile) itemEvent.getItem());
            }
        });
        btnCancel.addActionListener(actionEvent -> {
            // restore selection for currently selected Profile and switch back to display state
            handleProfileSelectionChanged((Profile) comboBoxProfiles.getSelectedItem());
            switchState(STATE_DISPLAY_PROFILE_SELECTED);
        });
    }

    void activatePanel() {
        initializeProfileCombos();
        switchState(STATE_DISPLAY_PROFILE_SELECTED);
        if (comboBoxProfiles.getItemCount() > 0) {
            handleProfileSelectionChanged((Profile) comboBoxProfiles.getItemAt(0));
        }
    }

    private void handleProfileSelectionChanged(Profile profile) {
        if (profile == null)
            return;
        selectComboItemById(comboBoxMainTelescope, profile.getMainTelescopeId());
        selectComboItemById(comboBoxMainCamera, profile.getMainCameraId());
        selectComboItemById(comboBoxGuidingTelescope, profile.getGuidingTelescopeId());
        selectComboItemById(comboBoxGuidingCamera, profile.getGuidingCameraId());
        selectComboItemById(comboBoxMounts, profile.getMountId());
    }

    private Profile createProfileFromSelections() {
        return new Profile(
                textFieldNewProfileName.getText(),
                getSelectedObjectId(comboBoxMainTelescope),
                getSelectedObjectId(comboBoxMainCamera),
                getSelectedObjectId(comboBoxGuidingTelescope),
                getSelectedObjectId(comboBoxGuidingCamera),
                getSelectedObjectId(comboBoxMounts)
        );
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

    private void saveProfilesToConfig() {
        // extract Profile instances from profiles-combobox
        List<Profile> profiles = new ArrayList<>();
        for (int i = 0; i < comboBoxProfiles.getItemCount(); i++) {
            Object o = comboBoxProfiles.getItemAt(i);
            if (o instanceof Profile) {
                profiles.add((Profile) o);
            }
        }
        // update and save config
        Config.getInstance().updateProfiles(profiles);
        Config.getInstance().saveConfig();
    }

    private void initializeProfileCombos() {
        comboBoxProfiles.removeAllItems();
        comboBoxMainCamera.removeAllItems();
        comboBoxMainTelescope.removeAllItems();
        comboBoxGuidingCamera.removeAllItems();
        comboBoxGuidingTelescope.removeAllItems();
        comboBoxMounts.removeAllItems();


        comboBoxMainCamera.addItem("-");
        comboBoxGuidingCamera.addItem("-");
        comboBoxMainTelescope.addItem("-");
        comboBoxGuidingTelescope.addItem("-");
        comboBoxMounts.addItem("-");
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

        for (Profile profile : Config.getInstance().getProfiles()) {
            comboBoxProfiles.addItem(profile);
        }
    }

    private void switchState(int newState) {
        switch (newState) {
            case STATE_DISPLAY_NO_PROFILE_SELECTED:
                comboBoxMainCamera.setEnabled(false);
                comboBoxMainTelescope.setEnabled(false);
                comboBoxGuidingCamera.setEnabled(false);
                comboBoxGuidingTelescope.setEnabled(false);
                comboBoxMounts.setEnabled(false);

                btnAdd.setEnabled(true);
                btnDelete.setEnabled(false);
                btnProfileEdit.setEnabled(false);
                btnSave.setEnabled(false);
                btnCancel.setEnabled(false);

                textFieldNewProfileName.setEnabled(false);
                break;
            case STATE_DISPLAY_PROFILE_SELECTED:
                comboBoxMainCamera.setEnabled(false);
                comboBoxMainTelescope.setEnabled(false);
                comboBoxGuidingCamera.setEnabled(false);
                comboBoxGuidingTelescope.setEnabled(false);
                comboBoxMounts.setEnabled(false);

                btnAdd.setEnabled(true);
                btnDelete.setEnabled(true);
                btnProfileEdit.setEnabled(true);
                btnSave.setEnabled(false);
                btnCancel.setEnabled(false);

                textFieldNewProfileName.setEnabled(false);
                break;
            case STATE_EDIT:
                textFieldNewProfileName.setText(comboBoxProfiles.getSelectedItem().toString());
            case STATE_NEW:
                comboBoxMainCamera.setEnabled(true);
                comboBoxMainTelescope.setEnabled(true);
                comboBoxGuidingCamera.setEnabled(true);
                comboBoxGuidingTelescope.setEnabled(true);
                comboBoxMounts.setEnabled(true);

                btnAdd.setEnabled(false);
                btnDelete.setEnabled(false);
                btnProfileEdit.setEnabled(false);
                btnSave.setEnabled(true);
                btnCancel.setEnabled(true);

                textFieldNewProfileName.setEnabled(true);
                if (newState == STATE_NEW) {
                    comboBoxMainTelescope.setSelectedItem("-");
                    comboBoxMainCamera.setSelectedItem("-");
                    comboBoxGuidingCamera.setSelectedItem("-");
                    comboBoxGuidingTelescope.setSelectedItem("-");
                    comboBoxMounts.setSelectedItem("-");
                }
                break;
            default:
                throw new IllegalStateException(String.format("Unexpected value: %d", newState));
        }
        currentState = newState;

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
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("l10n", "common_profiles"));
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(label1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer2, gbc);
        comboBoxProfiles = new JComboBox();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(comboBoxProfiles, gbc);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 11;
        gbc.weightx = 0.2;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer4, gbc);
        panelImagingEqu = new JPanel();
        panelImagingEqu.setLayout(new GridBagLayout());
        Font panelImagingEquFont = this.$$$getFont$$$(null, Font.BOLD, -1, panelImagingEqu.getFont());
        if (panelImagingEquFont != null) panelImagingEqu.setFont(panelImagingEquFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.weightx = 0.2;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panelImagingEqu, gbc);
        panelImagingEqu.setBorder(BorderFactory.createTitledBorder(null, this.$$$getMessageFromBundle$$$("l10n", "config_profiles_image.capture"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("l10n", "config_profiles_telescope"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelImagingEqu.add(label2, gbc);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelImagingEqu.add(spacer5, gbc);
        comboBoxMainTelescope = new JComboBox();
        comboBoxMainTelescope.setEditable(false);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        comboBoxMainTelescope.setModel(defaultComboBoxModel1);
        comboBoxMainTelescope.setToolTipText("Optische Konfiguration für die Bildaufnahme");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelImagingEqu.add(comboBoxMainTelescope, gbc);
        comboBoxMainCamera = new JComboBox();
        comboBoxMainCamera.setToolTipText("Hauptkamera für die Bildaufnahme");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelImagingEqu.add(comboBoxMainCamera, gbc);
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("l10n", "config_profiles_main.camera"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panelImagingEqu.add(label3, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelImagingEqu.add(spacer6, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 10;
        panelImagingEqu.add(spacer7, gbc);
        panelGuidingEqu = new JPanel();
        panelGuidingEqu.setLayout(new GridBagLayout());
        Font panelGuidingEquFont = this.$$$getFont$$$(null, Font.BOLD, -1, panelGuidingEqu.getFont());
        if (panelGuidingEquFont != null) panelGuidingEqu.setFont(panelGuidingEquFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 5;
        gbc.weightx = 0.2;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panelGuidingEqu, gbc);
        panelGuidingEqu.setBorder(BorderFactory.createTitledBorder(null, "Guiding", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label4 = new JLabel();
        label4.setText("Teleskop / Optik:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelGuidingEqu.add(label4, gbc);
        final JPanel spacer8 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelGuidingEqu.add(spacer8, gbc);
        final JPanel spacer9 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelGuidingEqu.add(spacer9, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("Kamera:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panelGuidingEqu.add(label5, gbc);
        comboBoxGuidingTelescope = new JComboBox();
        comboBoxGuidingTelescope.setToolTipText("Optische Konfiguration für das Guiding");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelGuidingEqu.add(comboBoxGuidingTelescope, gbc);
        comboBoxGuidingCamera = new JComboBox();
        comboBoxGuidingCamera.setToolTipText("Kamera für das Guiding");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelGuidingEqu.add(comboBoxGuidingCamera, gbc);
        final JPanel spacer10 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.ipadx = 10;
        panelGuidingEqu.add(spacer10, gbc);
        panelMount = new JPanel();
        panelMount.setLayout(new GridBagLayout());
        Font panelMountFont = this.$$$getFont$$$(null, Font.BOLD, -1, panelMount.getFont());
        if (panelMountFont != null) panelMount.setFont(panelMountFont);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 5;
        gbc.weightx = 0.2;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(panelMount, gbc);
        panelMount.setBorder(BorderFactory.createTitledBorder(null, "Montierung", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel spacer11 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMount.add(spacer11, gbc);
        comboBoxMounts = new JComboBox();
        comboBoxMounts.setToolTipText("die verwendete Montierung");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelMount.add(comboBoxMounts, gbc);
        final JPanel spacer12 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer12, gbc);
        final JPanel spacer13 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer13, gbc);
        final JPanel spacer14 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer14, gbc);
        btnDelete = new JButton();
        this.$$$loadButtonText$$$(btnDelete, this.$$$getMessageFromBundle$$$("l10n", "config_profiles_delete"));
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(btnDelete, gbc);
        btnSave = new JButton();
        this.$$$loadButtonText$$$(btnSave, this.$$$getMessageFromBundle$$$("l10n", "common_save"));
        gbc = new GridBagConstraints();
        gbc.gridx = 11;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(btnSave, gbc);
        btnProfileEdit = new JButton();
        this.$$$loadButtonText$$$(btnProfileEdit, this.$$$getMessageFromBundle$$$("l10n", "config_profiles_edit"));
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(btnProfileEdit, gbc);
        final JPanel spacer15 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer15, gbc);
        final JPanel spacer16 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 12;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer16, gbc);
        final JPanel spacer17 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer17, gbc);
        final JPanel spacer18 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 6;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer18, gbc);
        textFieldNewProfileName = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(textFieldNewProfileName, gbc);
        final JPanel spacer19 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer19, gbc);
        final JPanel spacer20 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer20, gbc);
        btnCancel = new JButton();
        this.$$$loadButtonText$$$(btnCancel, this.$$$getMessageFromBundle$$$("l10n", "common_cancel"));
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(btnCancel, gbc);
        btnAdd = new JButton();
        this.$$$loadButtonText$$$(btnAdd, this.$$$getMessageFromBundle$$$("l10n", "common_new"));
        gbc = new GridBagConstraints();
        gbc.gridx = 7;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(btnAdd, gbc);
        final JLabel label6 = new JLabel();
        this.$$$loadLabelText$$$(label6, this.$$$getMessageFromBundle$$$("l10n", "config_profiles_profile.name"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        topPanel.add(label6, gbc);
        final JPanel spacer21 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 8;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer21, gbc);
        final JPanel spacer22 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer22, gbc);
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
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
