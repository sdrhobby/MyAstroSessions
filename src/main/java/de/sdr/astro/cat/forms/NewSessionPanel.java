package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.gui.PathObjectTreeNode;
import de.sdr.astro.cat.model.AstroObject;
import de.sdr.astro.cat.model.Model;
import de.sdr.astro.cat.model.PathObject;
import de.sdr.astro.cat.model.Session;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NewSessionPanel {
    private JPanel topPanel;
    private JPanel panelSessionInfo;
    private JPanel panelEquipmentPlacehoder;
    private JButton btnCreateSession;
    private JButton btnCancel;
    private JPanel panelButtons;
    private JTextField textFieldNamePart1;
    private JTextField textFieldNamePart2;
    private JCheckBox checkLights;
    private JCheckBox checkFlats;
    private JCheckBox checkDarks;
    private JCheckBox checkBiases;
    private JCheckBox checkReadme;

    private SessionEquipmentPanel sessionEquipmentPanel;

    private final PathObject parentObject;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public NewSessionPanel(PathObject parentObject) {
        this.parentObject = parentObject;
        initializeSessionData();
        initializeEquipmentPanel();
        btnCancel.addActionListener(actionEvent -> SwingUtilities.getRoot(getTopPanel()).setVisible(false));
        btnCreateSession.addActionListener(actionEvent -> {
            // create Session-structure in filesystem
            String newSessionPath = createSessionInFilesystem();
            if (newSessionPath != null) {
                // add Session to tree
                Session newSession = addSessionToTree(newSessionPath);
                // fill ReadMe template with info from new Session and write readme.txt to session folder
                writeReadMeFromTemplate(newSession);

                AstroCatGui.getInstance().setStatus(String.format(Config.getInstance().getL10n().getString("newsession_status.done"), newSessionPath));
                SwingUtilities.getRoot(getTopPanel()).setVisible(false);
            }
        });
    }

    private String createSessionInFilesystem() {
        try {
            // create folder with session name below parentObjects path
            String sessionPath = String.format("%s%s%s%s", parentObject.getPath(), File.separator, textFieldNamePart1.getText(), textFieldNamePart2.getText());
            new File(sessionPath).mkdir();
            if (checkLights.isSelected())
                new File(sessionPath + File.separator + Model.LIGHTS).mkdir();
            if (checkDarks.isSelected())
                new File(sessionPath + File.separator + Model.DARKS).mkdir();
            if (checkFlats.isSelected())
                new File(sessionPath + File.separator + Model.FLATS).mkdir();
            if (checkBiases.isSelected())
                new File(sessionPath + File.separator + Model.BIASES).mkdir();

            return sessionPath;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }

    private Session addSessionToTree( String newSessionPath ) {
        // create Session from new created folder
        Session newSession = new Session(newSessionPath);

        // save equipment-info to .astrocat.session file in the new session-folder
        sessionEquipmentPanel.saveSessionEquipment(newSession);

        // add new Session to parentObject
        ((AstroObject) parentObject).getSessions().add(newSession);

        // add new Session object to the tree
        PathObjectTreeNode parentNode = AstroCatGui.getInstance().getTreeNodeByNodeObject(parentObject);
        PathObjectTreeNode newSessionNode = AstroCatGui.getInstance().createSessionNode(newSession);
        AstroCatGui.getInstance().getFolderTreeModel().insertNodeInto( newSessionNode, parentNode, parentNode.getChildCount());

        return newSession;
    }

    private String applySessionToReadmeTemplate(Session newSession ) {
        String readmeStr = Config.getInstance().readReadmeTemplate();
        // replace placeholders with real values
        readmeStr = readmeStr.replaceFirst( "_DATE_", newSession.getDate().toString() );
        readmeStr = readmeStr.replaceFirst( "_OBJECT_", newSession.getAstroObjectName() );
        readmeStr = readmeStr.replaceFirst( "_EQUIPMENT_", sessionEquipmentPanel.getEquipmentAsString() );

        return readmeStr;
    }

    private void writeReadMeFromTemplate( Session newSession ) {
        String readmeStr = applySessionToReadmeTemplate( newSession );
        // write readme.txt to session folder
        try {
            FileWriter fw = new FileWriter(String.format("%s%sreadme.txt", newSession.getPath(), File.separator));
            fw.write(readmeStr);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeSessionData() {
        // pre-fill the current date into part1 of the session name
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now());
        textFieldNamePart1.setText(date);
    }

    private void initializeEquipmentPanel() {
        sessionEquipmentPanel = new SessionEquipmentPanel(null, false);
        panelEquipmentPlacehoder.add(sessionEquipmentPanel.getTopPanel(), BorderLayout.CENTER);
    }
}
