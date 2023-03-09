package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.common.ImageWithLinkPanel;
import de.sdr.astro.cat.forms.common.WrapLayout;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.model.*;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
                        if ( image.isJpegTiffPng() ) {
                            ImageWithLinkPanel previewPanel = new ImageWithLinkPanel(image, String.format( "%s : %s", session.getName(), image.getName() ), image, new Dimension(200, 150));
                            publish( previewPanel );
                            panels.add( previewPanel );
                        }
                    }
                }
                return panels;
            }

            @Override
            protected void process( List<ImageWithLinkPanel> chunks ) {
                chunks.forEach( panel -> panelPreviews.add( panel ) );
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

}
