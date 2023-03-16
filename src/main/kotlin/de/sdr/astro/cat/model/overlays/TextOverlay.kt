package de.sdr.astro.cat.model.overlays

import de.sdr.astro.cat.model.PointDouble
import java.awt.*

class TextOverlay( val text : String, point : PointDouble, color: Color ) : Overlay(point, color) {
    var relTextSize = 0.0
    var font : Font? = null

    constructor( text: String, point: PointDouble, relTextSize: Double, color: Color ) : this(text, point, color) {
        this.relTextSize = relTextSize
    }

    /**
     * paint this overlay using the given Graphics object
     * Since the own point coordinates are only relative to the size of the container, the dimension of the container is expected
     * @param g: Graphics ... the Graphics object to be used for painting
     * @param xOff, yOff ... potential offsets to apply for pointing actions
     * @param cDim: Dimension ... the current dimension of the object to paint into
     */
    override  fun paint(g : Graphics, cDim : Dimension, xOff: Int, yOff: Int ) {
        val p : Point = translateRelToContainerCoords( point, xOff, yOff, cDim)
        g.setColor( color )
        g.drawString(text, p.x, p.y)
    }

}