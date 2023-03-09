package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;

import javax.swing.*;

public class ConfigPanel {

    private JPanel topPanel;
    private JTabbedPane tabbedPaneEquipment;
    private final ConfigEquipmentPanel configEquipmentPanel;
    private final ConfigProfilePanel configProfilePanel;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public ConfigPanel() {
        configEquipmentPanel = new ConfigEquipmentPanel();
        configProfilePanel = new ConfigProfilePanel();
        tabbedPaneEquipment.addTab( Config.getInstance().getL10n().getString("common_my.equipment"), configEquipmentPanel.getTopPanel());
        tabbedPaneEquipment.addTab( Config.getInstance().getL10n().getString("common_profiles"), configProfilePanel.getTopPanel());

        configEquipmentPanel.activatePanel();
        tabbedPaneEquipment.addChangeListener( changeEvent -> {
            switch (tabbedPaneEquipment.getSelectedIndex()) {
                case 0:
                    configEquipmentPanel.activatePanel();
                    break;
                case 1:
                    configProfilePanel.activatePanel();
                    break;

            }
        } );
    }
}
