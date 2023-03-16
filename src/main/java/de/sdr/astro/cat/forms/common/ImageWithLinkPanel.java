package de.sdr.astro.cat.forms.common;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.AstroCatGui;
import de.sdr.astro.cat.model.Image;
import de.sdr.astro.cat.model.PathObject;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageWithLinkPanel extends JPanel {
    public ImageWithLinkPanel(Image image, String linkText, PathObject targetObject, Dimension size) {
        setLayout(new BorderLayout());
        setPreferredSize(size);
        if (image != null) {
            ImageDisplayPanel imageDisplayPanel = new ImageDisplayPanel();
            imageDisplayPanel.addMouseListener( new NavigationMouseAdapter(targetObject) );
            add(imageDisplayPanel, BorderLayout.CENTER);
            imageDisplayPanel.setImage(image);
        } else {
            JLabel label = new JLabel(Config.getInstance().getL10n().getString("sessioninfo_no.preview"));
            label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            label.addMouseListener( new NavigationMouseAdapter( targetObject ));
            add(label, BorderLayout.CENTER);

        }
        add(new NavigationLinkLabel(linkText, targetObject), BorderLayout.SOUTH);
    }

    class NavigationMouseAdapter extends MouseAdapter {
        private PathObject targetObject;
        public NavigationMouseAdapter(PathObject targetObject) {
            this.targetObject = targetObject;
        }
        @Override
        public void mouseEntered(MouseEvent e) {
            AstroCatGui.setCursor(Cursor.HAND_CURSOR);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            AstroCatGui.setCursor(Cursor.DEFAULT_CURSOR);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            AstroCatGui.getInstance().selectTreeNodeByNodeObject(targetObject);
        }
    }
}
