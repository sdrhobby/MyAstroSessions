package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.gui.PathObjectTreeNode;
import de.sdr.astro.cat.model.AstroObject;
import de.sdr.astro.cat.model.Model;
import de.sdr.astro.cat.model.PathObject;
import de.sdr.astro.cat.model.Session;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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

    private Session addSessionToTree(String newSessionPath) {
        // create Session from new created folder
        Session newSession = new Session(newSessionPath);

        // save equipment-info to .astrocat.session file in the new session-folder
        sessionEquipmentPanel.saveSessionEquipment(newSession);

        // add new Session to parentObject
        ((AstroObject) parentObject).getSessions().add(newSession);

        // add new Session object to the tree
//        PathObjectTreeNode parentNode = AstroCatGui.getInstance().getTreeNodeByNodeObject(parentObject);
        // TODO: do this for all trees
        AstroCatGui gui = AstroCatGui.getInstance();
        PathObjectTreeNode parentNode = gui.getNodeByNodeObject((PathObjectTreeNode) gui.getFolderTreeRoot(), parentObject);
        PathObjectTreeNode newSessionNode = AstroCatGui.getInstance().createSessionNode(newSession);
        AstroCatGui.getInstance().getFolderTreeModel().insertNodeInto(newSessionNode, parentNode, parentNode.getChildCount());

        return newSession;
    }

    private String applySessionToReadmeTemplate(Session newSession) {
        String readmeStr = Config.getInstance().readReadmeTemplate();
        // replace placeholders with real values
        readmeStr = readmeStr.replaceFirst("_DATE_", newSession.getDate().toString());
        readmeStr = readmeStr.replaceFirst("_OBJECT_", newSession.getAstroObjectName());
        readmeStr = readmeStr.replaceFirst("_EQUIPMENT_", sessionEquipmentPanel.getEquipmentAsString());

        return readmeStr;
    }

    private void writeReadMeFromTemplate(Session newSession) {
        String readmeStr = applySessionToReadmeTemplate(newSession);
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
        panelSessionInfo = new JPanel();
        panelSessionInfo.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 0.2;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(panelSessionInfo, gbc);
        panelSessionInfo.setBorder(BorderFactory.createTitledBorder(null, this.$$$getMessageFromBundle$$$("l10n", "common_sessioninfo"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("l10n", "newsession_session.name"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelSessionInfo.add(label1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelSessionInfo.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        panelSessionInfo.add(spacer2, gbc);
        textFieldNamePart1 = new JTextField();
        textFieldNamePart1.setColumns(10);
        textFieldNamePart1.setEditable(false);
        textFieldNamePart1.setToolTipText("Session Namen beginnen mit dem Datum der Session");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelSessionInfo.add(textFieldNamePart1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("+");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panelSessionInfo.add(label2, gbc);
        textFieldNamePart2 = new JTextField();
        textFieldNamePart2.setColumns(10);
        textFieldNamePart2.setToolTipText("optionaler Prefix für den Session Namen");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelSessionInfo.add(textFieldNamePart2, gbc);
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("l10n", "newsession_create"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panelSessionInfo.add(label3, gbc);
        checkLights = new JCheckBox();
        checkLights.setSelected(true);
        checkLights.setText("lights");
        checkLights.setToolTipText("Lege Ordner \"lights\" an");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panelSessionInfo.add(checkLights, gbc);
        checkFlats = new JCheckBox();
        checkFlats.setSelected(true);
        checkFlats.setText("flats");
        checkFlats.setToolTipText("lege Ordner \"flats\" an");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panelSessionInfo.add(checkFlats, gbc);
        checkDarks = new JCheckBox();
        checkDarks.setSelected(true);
        checkDarks.setText("darks");
        checkDarks.setToolTipText("lege Ordner \"darks\" an");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panelSessionInfo.add(checkDarks, gbc);
        checkBiases = new JCheckBox();
        checkBiases.setSelected(true);
        checkBiases.setText("biases");
        checkBiases.setToolTipText("lege Ordner \"biases\" an");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panelSessionInfo.add(checkBiases, gbc);
        checkReadme = new JCheckBox();
        checkReadme.setSelected(true);
        checkReadme.setText("Readme.txt");
        checkReadme.setToolTipText("lege Readme.txt für Session-Notizen an");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panelSessionInfo.add(checkReadme, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer4, gbc);
        panelEquipmentPlacehoder = new JPanel();
        panelEquipmentPlacehoder.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.weightx = 0.2;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(panelEquipmentPlacehoder, gbc);
        panelEquipmentPlacehoder.setBorder(BorderFactory.createTitledBorder(null, this.$$$getMessageFromBundle$$$("l10n", "newsession_equipment"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer5, gbc);
        panelButtons = new JPanel();
        panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(panelButtons, gbc);
        btnCancel = new JButton();
        btnCancel.setText("Abbrechen");
        panelButtons.add(btnCancel);
        btnCreateSession = new JButton();
        btnCreateSession.setText("Session anlegen");
        panelButtons.add(btnCreateSession);
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
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }
}
