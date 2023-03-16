package de.sdr.astro.cat.model.overlays

import de.sdr.astro.cat.model.PointDouble
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point

/***
 * @param point: PointDouble
 */
open class Overlay(val point: PointDouble, val color: Color) {

    open fun translateRelToContainerCoords(p: PointDouble, xOff: Int, yOff: Int, cDim: Dimension): Point {
        return Point( xOff + (p.x * cDim.width / 100).toInt(), yOff + (p.y * cDim.height / 100).toInt() );
    }


    /**
     * paint this overlay using the given Graphics object
     * Since the own point coordinates are only relative to the size of the container, the dimension of the container is expected
     * @param g: Graphics ... the Graphics object to be used for painting
     * @param xOff, yOff ... potential offsets to apply for pointing actions
     * @param cDim: Dimension ... the current dimension of the object to paint into
     */
    open fun paint( g : Graphics, cDim : Dimension, xOff: Int = 0, yOff: Int = 0 ) {
        val p : Point = translateRelToContainerCoords( point, xOff, yOff, cDim)
        g.setColor( color )
        g.drawOval(p.x, p.y, 10, 10)
    }
}