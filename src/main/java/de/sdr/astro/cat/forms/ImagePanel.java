package de.sdr.astro.cat.forms;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.common.ImageDisplayPanel;
import de.sdr.astro.cat.model.Image;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

public class ImagePanel {
    private JTabbedPane tabbedPaneImage;
    private JPanel topPanel;
    private final ImageMetadataPanel imageMetadataPanel;
    private final ImageDisplayPanel imageDisplayPanel;

    private Image image;

    public JPanel getTopPanel() {
        return topPanel;
    }

    public ImagePanel() {
        imageDisplayPanel = new ImageDisplayPanel(true);
        imageMetadataPanel = new ImageMetadataPanel();
        tabbedPaneImage.addTab("ImageDisplay", imageDisplayPanel.getTopPanel());
        tabbedPaneImage.addTab(Config.getInstance().getL10n().getString("imagepanel_metadata"), imageMetadataPanel.getTopPanel());

        tabbedPaneImage.addChangeListener(changeEvent -> {
            switch (tabbedPaneImage.getSelectedIndex()) {
                case 0:
                    imageDisplayPanel.setImage(image);
                    break;
                case 1:
                    imageMetadataPanel.setImage(image);
                    break;
            }
        });
    }

    public void initialize(Image image) {
        this.image = image;
//        AstroCatGui.setCursor(Cursor.WAIT_CURSOR);
        AstroCatGui.getInstance().setStatus(MessageFormat.format(Config.getInstance().getL10n().getString("imagepanel_loading"), image.getPath()));
        System.out.printf( "ImagePanel loading image: %s%n", image.getPath() );
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                imageDisplayPanel.setImage(image);
                imageMetadataPanel.setImage(image);
                return null;
            }

            @Override
            protected void done() {
                System.out.println("ImagePanel - DONE ");
                AstroCatGui.getInstance().setStatus("");
                super.done();
            }
        }.execute();
    }

}
