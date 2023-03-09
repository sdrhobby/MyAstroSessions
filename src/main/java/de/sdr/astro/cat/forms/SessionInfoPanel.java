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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
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

        fillImageTable( session );
        initializeSessionEquipment();
        initializeReadmeEditor();

        // change cursor and set status to indicate potentially longer activity (due to loading of the preview)
//        AstroCatGui.setCursor(Cursor.WAIT_CURSOR);
        AstroCatGui.getInstance().setStatus(MessageFormat.format(Config.getInstance().getL10n().getString("sessioninfo_status_loading.session"), session.getAstroObjectName(),session.getName()));

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
            public boolean isCellEditable(int row, int column) { return false; }
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
        ImageWithLinkPanel imageWithLinkPanel = new ImageWithLinkPanel( preview, preview.getName(), preview, new Dimension(200, 150));
        panelImagePreview.add( imageWithLinkPanel );
    }

    private String applySessionToReadmeTemplate(Session session ) {
        String readmeStr = Config.getInstance().readReadmeTemplate();
        // replace template placeholders with real values
        readmeStr = readmeStr.replaceFirst( "_DATE_", session.getDate().toString() );
        readmeStr = readmeStr.replaceFirst( "_OBJECT_", session.getAstroObjectName() );
        readmeStr = readmeStr.replaceFirst( "_EQUIPMENT_", sessionEquipmentPanel.getEquipmentAsString() );

        return readmeStr;
    }

}
