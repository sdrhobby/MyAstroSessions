package de.sdr.astro.cat.gui.overlays

import de.sdr.astro.cat.model.PointDouble
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Point

/***
 * @param point: PointDouble
 */
open class Overlay(val point: PointDouble, var color: Color) {

    /**
     * translates the relative (percentage) x/y coordinates into real Coordinates for the current container size
     */
    open fun translateRelToContainerCoords(p: PointDouble, cDim: Dimension): Point {
        return Point( (p.x * cDim.width / 100).toInt(), (p.y * cDim.height / 100).toInt() );
    }


    /**
     * paint this overlay using the given Graphics object
     * Since the own point coordinates are only relative to the size of the container, the dimension of the container is expected
     * @param g: Graphics ... the Graphics object to be used for painting
     * @param cDim: Dimension ... the current dimension of the object to paint into
     */
    open fun paint( g : Graphics, cDim : Dimension  ) {
        val p : Point = translateRelToContainerCoords( point, cDim)
        g.setColor( color )
        g.drawOval(p.x, p.y, 10, 10)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Overlay

        if (point != other.point) return false
        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        var result = point.hashCode()
        result = 31 * result + color.hashCode()
        return result
    }


}