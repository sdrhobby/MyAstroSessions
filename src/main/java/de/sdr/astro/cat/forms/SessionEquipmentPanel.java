package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.*;
import de.sdr.astro.cat.model.Session;

import javax.swing.*;

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
     * @param session ... the session for which the Equipment shall be displayed or configured (may be null, if used for a new Session to be created)
     * @param showSaveButton ... shall the save button be shown or not
     */
    public SessionEquipmentPanel(Session session, boolean showSaveButton) {
        this.session = session;
        btnSaveSessionEquipment.setVisible(showSaveButton);

        comboBoxProfil.addItemListener( itemEvent -> {
            System.out.println("profile selection changed: " + itemEvent.getItem());
            if (itemEvent.getItem() instanceof Profile) {
                Profile profile = (Profile) itemEvent.getItem();
                selectComboItemById(comboBoxMainTelescope, profile.getMainTelescopeId());
                selectComboItemById(comboBoxMainCamera, profile.getMainCameraId());
                selectComboItemById(comboBoxGuidingTelescope, profile.getGuidingTelescopeId());
                selectComboItemById(comboBoxGuidingCamera, profile.getGuidingCameraId());
                selectComboItemById(comboBoxMounts, profile.getMountId());
            }
        } );

        btnSaveSessionEquipment.addActionListener( actionEvent -> saveSessionEquipment(session) );

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
            if ( ! cameraStr.isEmpty() )
                // match this string against the keywords of the cameras in the Main Camera combo and select the one with the first match
                selectComboItemByKeywordMatch(comboBoxMainCamera, cameraStr);
        }
    }

    /**
     * The session equipment profile will be saved to a astrocat.session config file in the Session folder.
     * @param session ... the corresponding session (to identify the session-folder)
     */
    public void saveSessionEquipment(Session session ) {
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
                if ( haystack.contains( needle ) ) {
                    combo.setSelectedItem(e);
                    break;
                }

            }
        }
        combo.revalidate();
    }

    public String getEquipmentAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append( Config.getInstance().getL10n().getString("config_profiles_main.camera") );
        sb.append( ": " + comboBoxMainCamera.getSelectedItem().toString() + "\n");
        sb.append( Config.getInstance().getL10n().getString("equipment_main.telescope") );
        sb.append( ": " + comboBoxMainTelescope.getSelectedItem().toString() + "\n");
        sb.append( Config.getInstance().getL10n().getString("equipment_guiding.camera") );
        sb.append( ": " + comboBoxGuidingCamera.getSelectedItem().toString() + "\n");
        sb.append( Config.getInstance().getL10n().getString("equipment_guiding.telescope") );
        sb.append( ": " + comboBoxGuidingTelescope.getSelectedItem().toString() + "\n");
        sb.append( Config.getInstance().getL10n().getString("equipment_mount") );
        sb.append( ": " + comboBoxMounts.getSelectedItem().toString() + "\n");
        return sb.toString();
    }
}
