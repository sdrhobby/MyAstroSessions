package de.sdr.astro.cat.model

data class OverlayInfo(val label: String, val point: PointDouble) {

    fun toConfigLine(): String {
        return "overlayinfo:" + label + ";" + point.x + ";" + point.y
    }

    companion object {
        @JvmStatic
        fun fromConfigLine(line: String): OverlayInfo {
            val parts1 = line.split(':')
            val parts2 = parts1[1].split(';')
            val name = parts2[0]
            val p = PointDouble(parts2[1].toDouble(), parts2[2].toDouble())
            return OverlayInfo(name, p)
        }
    }
}