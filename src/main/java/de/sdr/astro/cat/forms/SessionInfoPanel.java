package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.common.EditorPanel;
import de.sdr.astro.cat.forms.common.ImageWithLinkPanel;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.model.MapKeyFilterIsoExposureGainBias;
import de.sdr.astro.cat.model.Model;
import de.sdr.astro.cat.model.Session;
import de.sdr.astro.cat.util.Util;
import kotlin.Pair;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

/***
 * The SessionInfoPanel provides a session-summary,
 * on the "Session-info" tab: organizational info like
 * - object name,
 * - capture date and start/stop times
 * - number and type of captured images
 * - total exposure times per image type
 * - summary of image metadata, i.e. # of pictures with same exposure, iso, gain, bias etc.
 * - a preview of a result image (as hyper-link to the actual image view)
 * on the "Notes" tab
 * - a text editor for the assigned readme file, allowing to review and update notes for the corresponding session
 */
public class SessionInfoPanel {
    private JPanel topPanel;
    private JTabbedPane tabbedPane1;
    private JPanel panelSessionInfo;
    private JPanel panelSessionReadme;
    private JLabel lblObject;
    private JLabel lblDate;
    private JLabel lblStart;
    private JLabel lblEnd;
    private JLabel lblTotalCaptureTime;
    private JTable tableImageData;
    private JPanel panelImagePreview;
    private JPanel panelEquipmentPlaceholder;
    private JButton buttonApplyReadmeTemplate;
    private SessionEquipmentPanel sessionEquipmentPanel;
    private EditorPanel editorPanel;

    private TableModel tableModel;

    private Session session;
    private String originalReadmeText;

    public SessionInfoPanel(Session session) {
        initialize(session);
        buttonApplyReadmeTemplate.addActionListener(actionEvent -> {
            // take the text of the readme template from the config folder and replace the placeholders with the corresponding session info
            String newText = applySessionToReadmeTemplate(session);
            // show confirmation dialog and insert text
            editorPanel.insertEditorText(newText);
        });
    }

    public JPanel getTopPanel() {
        return topPanel;
    }


    private void initialize(Session session) {
        this.session = session;

        // init basic session info
        lblDate.setText(session.getDate().toString());
        lblObject.setText(session.getLastPathElement(1));
        lblTotalCaptureTime.setText(Util.formatExposure(session.totalCaptureTime(Model.LIGHTS)) + " min");

        Pair startStopPair = session.startAndStopTime();
        lblStart.setText((startStopPair.getFirst().toString()));
        lblEnd.setText((startStopPair.getSecond().toString()));

        fillImageTable(session);
        initializeSessionEquipment();
        initializeReadmeEditor();

        // change cursor and set status to indicate potentially longer activity (due to loading of the preview)
//        AstroCatGui.setCursor(Cursor.WAIT_CURSOR);
        AstroCatGui.getInstance().setStatus(MessageFormat.format(Config.getInstance().getL10n().getString("sessioninfo_status_loading.session"), session.getAstroObjectName(), session.getName()));

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                initializePreview();
                return null;
            }

            @Override
            protected void done() {
                AstroCatGui.getInstance().setStatus("");
            }
        }.execute();
    }

    private void fillImageTable(Session session) {
        String[] columnNames = {
                Config.getInstance().getL10n().getString("sessioninfo_image.type"),
                Config.getInstance().getL10n().getString("common_total"),
                "Subs", "Exposure", "Filter", "ISO", "Gain", "Bias"
        };
        Vector<Vector> rows = new Vector<>();
        fillImageInfo(session, Model.LIGHTS, rows);
        fillImageInfo(session, Model.FLATS, rows);
        fillImageInfo(session, Model.DARKS, rows);
        fillImageInfo(session, Model.BIASES, rows);

        TableModel dataModel = new DefaultTableModel() {
            @Override
            public String getColumnName(int column) {
                return columnNames[column];
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public int getRowCount() {
                return rows.size();
            }

            public Object getValueAt(int row, int col) {
                return rows.get(row).get(col);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        Font font = tableImageData.getTableHeader().getFont();
        tableImageData.getTableHeader().setFont(font.deriveFont(Font.BOLD));
        ((DefaultTableCellRenderer) tableImageData.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);
        tableImageData.setModel(dataModel);

    }

    private void initializeSessionEquipment() {
        sessionEquipmentPanel = new SessionEquipmentPanel(session, true);
        panelEquipmentPlaceholder.add(sessionEquipmentPanel.getTopPanel(), BorderLayout.CENTER);
    }

    private void initializeReadmeEditor() {
        // guess the readme-name
        String readmeFile = session.determineReadmeFileName();
        editorPanel = new EditorPanel(session.getPath() + File.separator + readmeFile);
        panelSessionReadme.add(editorPanel.getTopPanel());
    }

    private static void fillImageInfo(Session session, String imageType, Vector<Vector> rows) {
        // set summed up total info per basic image type (light, dark etc.)
        Vector row = new Vector();
        row.add(imageType);
        row.add(session.getImageMap().get(imageType).size());
        row.add("");
        // total capture time
        row.add(imageType == Model.LIGHTS ? Util.formatExposure(session.totalCaptureTime(imageType)) + " min" : "");
        row.add("");
        row.add("");
        row.add("");
        row.add("");
        rows.add(row);
        // TODO: add support for different filters (filters should be one conceptual level above exp., iso, gain, bias
        // add details for the distribution of images to different capture params like (exposure, iso, gain, bias)
        Map mapIsoExposure = session.aggregateIsoExposureGainBias(imageType);
        mapIsoExposure.forEach((key, value) -> {
            final Vector r = new Vector();
            r.add("");
            r.add("");
            r.add(value);
            r.add(Util.formatExposure(((MapKeyFilterIsoExposureGainBias) key).getExposure()) + " s");
            r.add(((MapKeyFilterIsoExposureGainBias) key).getFilter());
            r.add(((MapKeyFilterIsoExposureGainBias) key).getIso());
            r.add(((MapKeyFilterIsoExposureGainBias) key).getGain());
            r.add(((MapKeyFilterIsoExposureGainBias) key).getBias());
            rows.add(r);
        });
    }

    private void initializePreview() {
        Image preview = session.guessBestResultImage();
        // use a ImageWithLinkPanel --> image will become hyperlink to the corresponding image
        ImageWithLinkPanel imageWithLinkPanel = new ImageWithLinkPanel(preview, preview.getName(), preview, new Dimension(200, 150));
        panelImagePreview.add(imageWithLinkPanel);
    }

    private String applySessionToReadmeTemplate(Session session) {
        String readmeStr = Config.getInstance().readReadmeTemplate();
        // replace template placeholders with real values
        readmeStr = readmeStr.replaceFirst("_DATE_", session.getDate().toString());
        readmeStr = readmeStr.replaceFirst("_OBJECT_", session.getAstroObjectName());
        readmeStr = readmeStr.replaceFirst("_EQUIPMENT_", sessionEquipmentPanel.getEquipmentAsString());

        return readmeStr;
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
        topPanel.setMinimumSize(new Dimension(800, 600));
        tabbedPane1 = new JTabbedPane();
        topPanel.add(tabbedPane1, BorderLayout.CENTER);
        panelSessionInfo = new JPanel();
        panelSessionInfo.setLayout(new GridBagLayout());
        panelSessionInfo.setPreferredSize(new Dimension(800, 600));
        tabbedPane1.addTab(this.$$$getMessageFromBundle$$$("l10n", "common_sessioninfo"), panelSessionInfo);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.BOTH;
        panelSessionInfo.add(panel1, gbc);
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), this.$$$getMessageFromBundle$$$("l10n", "sessioninfo_capture.settings"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("l10n", "common_object"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label1, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer1, gbc);
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("l10n", "common_date"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label2, gbc);
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("l10n", "sessioninfo_capture.time"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label3, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 8;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer3, gbc);
        final JPanel spacer4 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer4, gbc);
        lblStart = new JLabel();
        Font lblStartFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblStart.getFont());
        if (lblStartFont != null) lblStart.setFont(lblStartFont);
        lblStart.setText("start");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(lblStart, gbc);
        lblObject = new JLabel();
        Font lblObjectFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblObject.getFont());
        if (lblObjectFont != null) lblObject.setFont(lblObjectFont);
        lblObject.setText("object");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(lblObject, gbc);
        lblDate = new JLabel();
        Font lblDateFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblDate.getFont());
        if (lblDateFont != null) lblDate.setFont(lblDateFont);
        lblDate.setText("date");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(lblDate, gbc);
        lblEnd = new JLabel();
        Font lblEndFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblEnd.getFont());
        if (lblEndFont != null) lblEnd.setFont(lblEndFont);
        lblEnd.setText("end");
        gbc = new GridBagConstraints();
        gbc.gridx = 5;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(lblEnd, gbc);
        final JScrollPane scrollPane1 = new JScrollPane();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 9;
        gbc.weightx = 0.2;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        panel1.add(scrollPane1, gbc);
        tableImageData = new JTable();
        scrollPane1.setViewportView(tableImageData);
        final JPanel spacer5 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 9;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer5, gbc);
        final JPanel spacer6 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer6, gbc);
        lblTotalCaptureTime = new JLabel();
        Font lblTotalCaptureTimeFont = this.$$$getFont$$$(null, Font.BOLD, -1, lblTotalCaptureTime.getFont());
        if (lblTotalCaptureTimeFont != null) lblTotalCaptureTime.setFont(lblTotalCaptureTimeFont);
        lblTotalCaptureTime.setText("Label");
        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(lblTotalCaptureTime, gbc);
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, this.$$$getMessageFromBundle$$$("l10n", "sessioninfo_total.exposure"));
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(label4, gbc);
        final JLabel label5 = new JLabel();
        label5.setText("-");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 5;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 0, 5);
        panel1.add(label5, gbc);
        panelImagePreview = new JPanel();
        panelImagePreview.setLayout(new BorderLayout(0, 0));
        panelImagePreview.setMinimumSize(new Dimension(200, 150));
        panelImagePreview.setPreferredSize(new Dimension(200, 150));
        gbc = new GridBagConstraints();
        gbc.gridx = 9;
        gbc.gridy = 1;
        gbc.gridheight = 7;
        gbc.anchor = GridBagConstraints.EAST;
        panel1.add(panelImagePreview, gbc);
        final JPanel spacer7 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 10;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer7, gbc);
        panelEquipmentPlaceholder = new JPanel();
        panelEquipmentPlaceholder.setLayout(new BorderLayout(0, 0));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        panelSessionInfo.add(panelEquipmentPlaceholder, gbc);
        panelEquipmentPlaceholder.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), this.$$$getMessageFromBundle$$$("l10n", "sessioninfo_equipment.label"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panelSessionReadme = new JPanel();
        panelSessionReadme.setLayout(new BorderLayout(0, 0));
        tabbedPane1.addTab(this.$$$getMessageFromBundle$$$("l10n", "sessioninfo_notes"), panelSessionReadme);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        panelSessionReadme.add(panel2, BorderLayout.NORTH);
        buttonApplyReadmeTemplate = new JButton();
        this.$$$loadButtonText$$$(buttonApplyReadmeTemplate, this.$$$getMessageFromBundle$$$("l10n", "sessioninfo_notes_apply.template"));
        buttonApplyReadmeTemplate.setToolTipText("");
        panel2.add(buttonApplyReadmeTemplate);
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
