package de.sdr.astro.cat.model

data class PointDouble(val x: Double, val y: Double) {
    fun add( xDelta: Double, yDelta: Double) : PointDouble {
        return PointDouble( x + xDelta, y + yDelta)
    }
}