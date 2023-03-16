package de.sdr.astro.cat.gui.overlays

import de.sdr.astro.cat.model.PointDouble
import java.awt.*

class SkymapLabel(val label: String, point: PointDouble, color: Color) : Overlay(point, color) {

    fun toConfigLine(): String {
        return "overlayinfo:" + label + ";" + point.x + ";" + point.y
    }

    companion object {
        @JvmStatic
        fun fromConfigLine(line: String): SkymapLabel {
            val parts1 = line.split(':')
            val parts2 = parts1[1].split(';')
            val name = parts2[0]
            val p = PointDouble(parts2[1].toDouble(), parts2[2].toDouble())
            return SkymapLabel(name, p, Color.yellow)
        }
    }

    /**
     * paint this overlay using the given Graphics object
     * Since the own point coordinates are only relative to the size of the container, the dimension of the container is expected
     * @param g: Graphics ... the Graphics object to be used for painting
     * @param xOff, yOff ... potential offsets to apply for pointing actions
     * @param cDim: Dimension ... the current dimension of the object to paint into
     */

    override fun paint(g: Graphics, cDim: Dimension ) {
        val p : Point = translateRelToContainerCoords( point, cDim)
        val g2 = g as Graphics2D
        g2.setColor(color);
        g2.setStroke( BasicStroke(3F));
        g2.drawOval( p.x, p.y, 10, 10);
        g2.drawString( label, p.x + 15, p.y );
    }
}