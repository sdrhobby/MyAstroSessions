package de.sdr.astro.cat.forms.common;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.AstroCatGui;
import de.sdr.astro.cat.model.AstroObject;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.util.BufferedImageFactory;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ExecutionException;

public class ImageDisplayPanel extends JPanel {
    private JLabel lblWaiting = new JLabel(Config.getInstance().getL10n().getString("common_loading"));

    //    private JPanel currentDisplayPanel;
    private JComponent currentDisplayPanel;
    private String currentImagePath;

    public ImageDisplayPanel() {
        setLayout( new BorderLayout() );
    }

    public void setImage(Image image) {
        // don't paint current image a second time
//        if (image.getPath().equals(currentImagePath))
//            return;
        // remember path for the next difference check
        currentImagePath = image.getPath();

        if (currentDisplayPanel != null)
            remove(currentDisplayPanel);

        add(lblWaiting, BorderLayout.CENTER);
        AstroCatGui.setCursor(Cursor.WAIT_CURSOR);

        new SwingWorker<JComponent, Void>() {

            @Override
            protected JComponent doInBackground() {
                return getDisplayPanel(image);
            }

            @Override
            protected void done() {
                remove(lblWaiting);
                try {
                    setCurrentDisplayPanel(get());
                    add(currentDisplayPanel);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
                revalidate();
                repaint();
                AstroCatGui.setCursor(Cursor.DEFAULT_CURSOR);
            }
        }.execute();
    }

    synchronized void setCurrentDisplayPanel(JComponent displayPanel) {
        this.currentDisplayPanel = displayPanel;
    }

    private JComponent getDisplayPanel(Image image) {
        JComponent displayPanel = null;
        if (image.isFits() || image.isJpegTiffPng()) {
            displayPanel = new OverlayImageDisplayPanel(BufferedImageFactory.getImage(image.getPath()));
        } else {
            displayPanel = new JPanel();
            JLabel label = new JLabel(String.format("Image format '%s' is not supported yet! You may open it in an external viewer.", image.getExtension()));
            label.setAlignmentY(JLabel.CENTER_ALIGNMENT);
            displayPanel.add(label, BorderLayout.CENTER);
        }
        return displayPanel;
    }
}
