package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

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

        btnAdd.addActionListener( actionEvent -> switchState(STATE_NEW) );
        btnProfileEdit.addActionListener( actionEvent -> switchState(STATE_EDIT) );
        btnDelete.addActionListener( actionEvent -> {
            // TODO: delete profile
            Profile profile = (Profile) comboBoxProfiles.getSelectedItem();
            int answer = JOptionPane.showConfirmDialog(
                    null,
                    String.format( Config.getInstance().getL10n().getString("config_profile.confirm.msg"), profile ),
                    Config.getInstance().getL10n().getString("config_profil.confirm.title"),
                    JOptionPane.YES_NO_OPTION);
            if ( answer == 0 ) {
                // delete from combo box
                comboBoxProfiles.removeItem(profile);
                saveProfilesToConfig();
            }
        } );
        btnSave.addActionListener( actionEvent -> {
            if (currentState == STATE_NEW) {
                comboBoxProfiles.addItem(createProfileFromSelections());
            } else {
                Profile changedProfile = createProfileFromSelections();
                comboBoxProfiles.removeItem(comboBoxProfiles.getSelectedItem());
                comboBoxProfiles.addItem(changedProfile);
            }
            saveProfilesToConfig();
            switchState(STATE_DISPLAY_PROFILE_SELECTED);
        } );
        comboBoxProfiles.addItemListener( itemEvent -> {
            System.out.printf( "profile selection changed: %s%n", itemEvent.getItem() );
            if (comboBoxProfiles.getItemCount() > 0) {
                handleProfileSelectionChanged((Profile) itemEvent.getItem());
            }
        } );
        btnCancel.addActionListener( actionEvent -> {
            // restore selection for currently selected Profile and switch back to display state
            handleProfileSelectionChanged( (Profile) comboBoxProfiles.getSelectedItem() );
            switchState( STATE_DISPLAY_PROFILE_SELECTED);
        } );
    }

    void activatePanel() {
        initializeProfileCombos();
        switchState(STATE_DISPLAY_PROFILE_SELECTED);
        if (comboBoxProfiles.getItemCount() > 0) {
            handleProfileSelectionChanged((Profile) comboBoxProfiles.getItemAt(0));
        }
    }

    private void handleProfileSelectionChanged(Profile profile) {
        if ( profile == null )
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
                textFieldNewProfileName.setText( comboBoxProfiles.getSelectedItem().toString());
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
                if ( newState == STATE_NEW ) {
                    comboBoxMainTelescope.setSelectedItem("-");
                    comboBoxMainCamera.setSelectedItem("-");
                    comboBoxGuidingCamera.setSelectedItem("-");
                    comboBoxGuidingTelescope.setSelectedItem("-");
                    comboBoxMounts.setSelectedItem("-");
                }
                break;
            default:
                throw new IllegalStateException( String.format( "Unexpected value: %d", newState ) );
        }
        currentState = newState;

    }
}
