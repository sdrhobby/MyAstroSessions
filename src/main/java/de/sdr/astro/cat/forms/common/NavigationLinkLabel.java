package de.sdr.astro.cat.forms.common;

import de.sdr.astro.cat.forms.AstroCatGui;
import de.sdr.astro.cat.model.PathObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NavigationLinkLabel extends JLabel {

    PathObject target;

    public NavigationLinkLabel(String text, PathObject pathObject) {
        super(text);
        target = pathObject;

        // let it look like a Hyperlink
        setForeground(Color.BLUE.darker());
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                System.out.printf( "navigating to: %s%n", target );
                AstroCatGui.getInstance().selectTreeNodeByNodeObject(target);
            }
        });
    }
}
