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
        System.out.printf("ImagePanel loading image: %s%n", image.getPath());
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
        tabbedPaneImage = new JTabbedPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        topPanel.add(tabbedPaneImage, gbc);
        final JPanel spacer1 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        topPanel.add(spacer1, gbc);
        final JPanel spacer2 = new JPanel();
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.VERTICAL;
        topPanel.add(spacer2, gbc);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return topPanel;
    }
}
