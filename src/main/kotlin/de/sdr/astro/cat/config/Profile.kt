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

    fun getCameraById( id : Int) : Camera?{
        return getEquipmentById( Config.getInstance().cameras as List<Equipment>, id) as Camera
    }

    fun getTelescopeById( id : Int) : Telescope?{
        return getEquipmentById( Config.getInstance().telescopes as List<Equipment>, id) as Telescope
    }

   fun getMountById( id : Int) : Mount?{
        return getEquipmentById( Config.getInstance().mounts as List<Equipment>, id) as Mount
    }

    private fun getEquipmentById( list : List<Equipment>, id : Int ) : Equipment? {
        list.forEach{
            if (it.getEquipmentId() == id )
                return it
        }
        return null
    }
}
