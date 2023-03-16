package de.sdr.astro.cat.forms.common;

import de.sdr.astro.cat.model.overlays.Overlay;
import de.sdr.astro.cat.model.overlays.SkymapLabel;
import de.sdr.astro.cat.model.PointDouble;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OverlayImageDisplayPanel extends JPanel {
    private BufferedImage img = null;
    private int scaledW;
    private int scaledH;
    private int xOff;
    private int yOff;

    private List<Overlay> overlays;

    public OverlayImageDisplayPanel(BufferedImage img) {
        this.img = img;
        this.overlays = new ArrayList<>();
    }

    private void refresh() {
        this.revalidate();
        this.repaint();
    }

    public void addOverlay(Overlay overlayInfo ) {
        if ( overlayInfo != null ) {
            this.overlays.add(overlayInfo);
            this.refresh();
        }
    }

    public void setOverlays(Collection<Overlay> overlays) {
        this.overlays.addAll(overlays);
        this.refresh();
    }

    public void clearOverlays() {
        this.overlays.clear();
        this.refresh();
    }

    public Dimension getPreferredSize() {
        return new Dimension(scaledW, scaledH);
    }

    public PointDouble translatePanelToRelativeImageCoord(Point p ) {
        return new PointDouble( (p.x - xOff) * 100 / scaledW, (p.y - yOff) * 100 / scaledH );
    }

    /**
     * get the offset of the upper left corner of the image relative to the overall Panel space
     * @return
     */
    public Point getImageOffset() {
        return new Point(xOff, yOff);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // get container size
        int cw = getParent().getWidth();
        int ch = getParent().getHeight();
        // get image size
        int iw = img.getWidth();
        int ih = img.getHeight();
        // scale to container width
        scaledW = cw;
        scaledH = (ih * scaledW) / iw;
        // if to high for container, scale to container height now
        if (scaledH > ch) {
            scaledW = (scaledW * ch) / scaledH;
            scaledH = ch;
        }
        xOff = (cw - scaledW) / 2;
        yOff = (ch - scaledH) / 2;
        // calc top/left to center image
        g2.drawImage(img, xOff, yOff, scaledW, scaledH, this);

        for (Overlay overlay : overlays ) {
            overlay.paint(g, new Dimension(scaledW, scaledH), xOff, yOff);
        }
    }

}
