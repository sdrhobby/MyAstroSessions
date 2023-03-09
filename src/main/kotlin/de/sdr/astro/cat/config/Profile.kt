package de.sdr.astro.cat.config

data class Profile(val name: String, val mainTelescopeId: Int, val mainCameraId: Int, val guidingTelescopeId: Int, val guidingCameraId: Int, val mountId: Int) {
    fun toConfigLine(): String {
        return "profile_$name;$mainTelescopeId;$mainCameraId;$guidingTelescopeId;$guidingCameraId;$mountId;"
    }

    override fun toString() : String {
        return name
    }


    companion object {
        @JvmStatic
        fun fromConfigLine(line: String): Profile {
            val parts = line.split(';')
            val c = parts[0]
            val name = c.substring(c.indexOf('_') + 1)
            return Profile(
                name,
                parts[1].toInt(),
                parts[2].toInt(),
                parts[3].toInt(),
                parts[4].toInt(),
                parts[5].toInt()
            )
        }
    }

}
