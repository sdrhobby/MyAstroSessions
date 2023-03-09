package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.common.ImageWithLinkPanel;
import de.sdr.astro.cat.forms.common.WrapLayout;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.model.*;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ObjectLinksOverviewPanel {
    private JPanel topPanel;
    private JPanel panelPreviews;
    private JLabel lblCaption;
    private AstroObject astroObject;
    private JButton btnSkymap;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public ObjectLinksOverviewPanel(PathObject pathObject) {
        panelPreviews.setLayout(new WrapLayout());
        panelPreviews.removeAll();
        if (pathObject instanceof AstroObject)
            setAstroObject((AstroObject) pathObject);
        else if (pathObject instanceof Session) {
            setSessionResultImages((Session) pathObject);
        }
        btnSkymap.addActionListener(actionEvent -> AstroCatGui.getInstance().showSkymapDialog(astroObject));
    }

    private void setAstroObject(AstroObject astroObject) {
        this.astroObject = astroObject;

        int num = astroObject.getSessions().size();
        String txt = num == 1 ? Config.getInstance().getL10n().getString("objectoverview_single.session") : MessageFormat.format(Config.getInstance().getL10n().getString("objectoverview_n.sessions"), num);
        lblCaption.setText(String.format(Config.getInstance().getL10n().getString("objectoverview_caption.sessions"), astroObject.getName(), txt));
        AstroCatGui.setCursor(Cursor.WAIT_CURSOR);
        AstroCatGui.getInstance().setStatus(String.format(Config.getInstance().getL10n().getString("objectoverview_status.sessions"), astroObject.getName()));

        new SwingWorker<List<ImageWithLinkPanel>, ImageWithLinkPanel>() {

            @Override
            protected List<ImageWithLinkPanel> doInBackground() {
                if (astroObject.hasSessions()) {
                    // add a preview image with results from each session belonging to that object
                    List<Session> sessions = astroObject.getSessions();
                    for (Session session : sessions) {
                        ImageWithLinkPanel previewPanel = new ImageWithLinkPanel(session.guessBestResultImage(), String.format("%s : %s", session.getAstroObjectName(), session.getName()), session, new Dimension(200, 150));
                        publish(previewPanel);
                    }
                }
                return null;
            }

            @Override
            protected void process(List<ImageWithLinkPanel> chunks) {
                chunks.forEach(panel -> panelPreviews.add(panel));
                panelPreviews.revalidate();
                panelPreviews.repaint();
            }

            @Override
            protected void done() {
                AstroCatGui.setCursor(Cursor.DEFAULT_CURSOR);
                AstroCatGui.getInstance().setStatus("");
            }
        }.execute();
    }

    private void setSessionResultImages(Session session) {


        List<Image> resultImages = session.getImageMap().get(Model.RESULTS);
        lblCaption.setText(String.format(Config.getInstance().getL10n().getString("objectoverview_caption.resultimages"), session.getName(), session.getAstroObjectName()));

        AstroCatGui.setCursor(Cursor.WAIT_CURSOR);
        AstroCatGui.getInstance().setStatus(String.format(Config.getInstance().getL10n().getString("objectoverview_status.resultimages"), session.getName(), session.getAstroObjectName()));


        new SwingWorker<List<ImageWithLinkPanel>, ImageWithLinkPanel>() {

            @Override
            protected List<ImageWithLinkPanel> doInBackground() {
                List<ImageWithLinkPanel> panels = new ArrayList<>();
                if (session.hasResultImages()) {
                    // add a preview image with results from each session belonging to that object
                    for (Image image : resultImages) {
                        if (image.isJpegTiffPng()) {
                            ImageWithLinkPanel previewPanel = new ImageWithLinkPanel(image, String.format("%s : %s", session.getName(), image.getName()), image, new Dimension(200, 150));
                            publish(previewPanel);
                            panels.add(previewPanel);
                        }
                    }
                }
                return panels;
            }

            @Override
            protected void process(List<ImageWithLinkPanel> chunks) {
                chunks.forEach(panel -> panelPreviews.add(panel));
                panelPreviews.revalidate();
                panelPreviews.repaint();
            }

            @Override
            protected void done() {
                AstroCatGui.setCursor(Cursor.DEFAULT_CURSOR);
                AstroCatGui.getInstance().setStatus("");
            }
        }.execute();
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
        topPanel.setAutoscrolls(true);
        final JScrollPane scrollPane1 = new JScrollPane();
        topPanel.add(scrollPane1, BorderLayout.CENTER);
        panelPreviews = new JPanel();
        panelPreviews.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        scrollPane1.setViewportView(panelPreviews);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridBagLayout());
        topPanel.add(panel1, BorderLayout.NORTH);
        lblCaption = new JLabel();
        lblCaption.setText("Label");
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel1.add(lblCaption, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer2, gbc);
        final JPanel spacer3 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(spacer3, gbc);
        btnSkymap = new JButton();
        this.$$$loadButtonText$$$(btnSkymap, this.$$$getMessageFromBundle$$$("l10n", "skymap_show.on.skymap"));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(btnSkymap, gbc);
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
