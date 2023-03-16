package de.sdr.astro.cat.forms.common;

import de.sdr.astro.cat.config.Config;
import de.sdr.astro.cat.forms.AstroCatGui;
import de.sdr.astro.cat.model.PointDouble;
import de.sdr.astro.cat.gui.overlays.Overlay;
import de.sdr.astro.cat.util.BufferedImageFactory;
import de.sdr.astro.cat.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ImageDisplayPanel extends JPanel {
    private JLabel lblWaiting = new JLabel(Config.getInstance().getL10n().getString("common_loading"));

    private BufferedImage currentBufferedImage;

    // this will serve as a copy of the original BufferedImage when we have to draw some overlays
    // these overloys will be drawn into that copy, which will be refreshed everytime some overlay info changes
    // this way we avoid scaling issues and artifacts due to scaling of the image container panel
    // the actual panel.paintComponent will then only draw the buffered Image including the overlays already
    private BufferedImage doubleBuffer;
    private int scaledW;
    private int scaledH;
    private int xOff;
    private int yOff;

    private List<Overlay> overlays;

    public ImageDisplayPanel() {
        initialize();
    }
    public ImageDisplayPanel( BufferedImage image ) {
        setBufferedImage( image );
        initialize();
    }

    private void initialize() {
        setLayout( new BorderLayout() );
        this.overlays = new ArrayList<>();
    }

    public void setImage(String imagePath) {

        add(lblWaiting, BorderLayout.CENTER);
        AstroCatGui.setCursor(Cursor.WAIT_CURSOR);

        new SwingWorker<BufferedImage, Void>() {

            @Override
            protected BufferedImage doInBackground() {
                return getBufferedImage(imagePath);
            }

            @Override
            protected void done() {
                remove(lblWaiting);
                try {
                    setBufferedImage(get());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
                revalidate();
                repaint();
                overlaysChanged();
                AstroCatGui.setCursor(Cursor.DEFAULT_CURSOR);
            }
        }.execute();
    }

    synchronized void setBufferedImage(BufferedImage image) {
        this.currentBufferedImage = image;
        // as long as we dont do any overlays, we don't need a real copy of the original image - we just let the double buffer
        // point to the original image
        doubleBuffer = currentBufferedImage;
    }

    public static BufferedImage copyImage(BufferedImage source){
        if ( source == null )
            return null;
        BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
        Graphics2D g = b.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return b;
    }

    private BufferedImage getBufferedImage(String imagePath ) {
        BufferedImage image = null;
        if (Util.isFits(imagePath) || Util.isJpegTiffPng(imagePath)) {
            image = BufferedImageFactory.getImage(imagePath);
        }
        return image;
    }

    public synchronized void overlaysChanged() {
        if (overlays != null && ! overlays.isEmpty())
        repaintOverlays();
    }

    /**
     * this method will be invoked whenever something changes on the overlays
     * It will create a fresh copy of the original BufferedImage and paint the overlays into that copy.
     * then it will cause a repaint of the container, painting the scaled doubleBuffer to the container
     */
    void repaintOverlays() {
        if ( currentBufferedImage == null )
            return;
        // now we need to create a real copy of the buffered image and paint the overlays into it
        doubleBuffer = copyImage( currentBufferedImage );
        Graphics2D g = doubleBuffer.createGraphics();

        // paint all types of added overlays
        for (Overlay overlay : overlays ) {
            overlay.paint(g, new Dimension(doubleBuffer.getWidth(), doubleBuffer.getHeight()));
        }
        // cause a repaint
        refresh();
    }

    void refresh() {
        this.revalidate();
        this.repaint();
    }

    public void addOverlay(Overlay overlay ) {
        if ( overlay != null ) {
            overlays.add(overlay);
            repaintOverlays();
        }
    }

    public void removeOverlay(Overlay overlay) {
        if ( overlay != null ) {
            overlays.remove(overlay);
            repaintOverlays();
        }
    }

    public void replaceOverlays(Collection<Overlay> newOverlays) {
        ArrayList<Overlay> list = new ArrayList<>();
        for (Overlay newOverlay : newOverlays) {
            list.add(newOverlay);
        }
        overlays = list;
        repaintOverlays();
    }

    public void clearOverlays() {
        overlays.clear();
        // when there are no overlays we can reset the double Buffer to the original image and just repaint
        doubleBuffer = currentBufferedImage;
        refresh();
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

        // don't paint until we have an assigned bufferedImage
        if ( doubleBuffer == null )
            return;

        Graphics2D g2 = (Graphics2D) g;
        // get container size
        int cw = getParent().getWidth();
        int ch = getParent().getHeight();
        // get image size
        int iw = doubleBuffer.getWidth();
        int ih = doubleBuffer.getHeight();
        // scale to container width
        scaledW = cw;
        scaledH = (ih * scaledW) / iw;
        // if to high for container, scale to container height now
        if (scaledH > ch) {
            scaledW = (scaledW * ch) / scaledH;
            scaledH = ch;
        }
        // calc top/left to center image
        xOff = (cw - scaledW) / 2;
        yOff = (ch - scaledH) / 2;
        g2.drawImage(doubleBuffer, xOff, yOff, scaledW, scaledH, this);
    }

}
