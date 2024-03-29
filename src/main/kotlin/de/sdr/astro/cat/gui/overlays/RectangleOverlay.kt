package de.sdr.astro.cat.gui.overlays

import de.sdr.astro.cat.model.PointDouble
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point

class RectangleOverlay( point : PointDouble, val point2 : PointDouble, color: Color) : Overlay(point, color){

    /**
     * paint this overlay using the given Graphics object
     * Since the own point coordinates are only relative to the size of the container, the dimension of the container is expected
     * @param g: Graphics ... the Graphics object to be used for painting
     * @param cDim: Dimension ... the current dimension of the object to paint into
     */
    override  fun paint(g : Graphics, cDim : Dimension ) {
        val p : Point = translateRelToContainerCoords( position, cDim)
        val p2 : Point = translateRelToContainerCoords( point2, cDim)
        g.setColor( color )
        g.drawRect(p.x, p.y, p2.x - p.x, p2.y - p.y)
    }
}