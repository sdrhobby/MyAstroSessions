package de.sdr.astro.cat.gui.overlays

import de.sdr.astro.cat.model.PointDouble
import java.awt.*

class TextOverlay( var text : String, point : PointDouble, color: Color ) : Overlay(point, color) {
    public var relFontSize = 2.0
    var font: Font = Font("Serif", Font.PLAIN, 20);

    /**
     * paint this overlay using the given Graphics object
     * Since the own point coordinates are only relative to the size of the container, the dimension of the container is expected
     * @param g: Graphics ... the Graphics object to be used for painting
     * @param cDim: Dimension ... the current dimension of the object to paint into
     */
    override fun paint(g : Graphics, cDim : Dimension ) {
        val p : Point = translateRelToContainerCoords( position, cDim)
        g.setColor( color )
        g.font = font
        // apply font size as approx 3 percent of image heigth
        val fontSize = cDim.height * relFontSize / 100
        g.font = g.font.deriveFont( fontSize.toFloat() )
        g.drawString(text, p.x, p.y)
    }

}