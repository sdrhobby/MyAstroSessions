package de.sdr.astro.cat.config

import java.util.*

data class Camera(val id: Int, val name : String, val xRes: Int, val yRes : Int, val pixXSize: Float, val pixYSize : Float, val keywords : String) : Equipment {
    fun toConfigLine(): String {
        return "camera_$id;$name;$xRes;$yRes;$pixXSize;$pixYSize;$keywords"
    }

    override fun toString() : String {
        return name
    }

    override fun getEquipmentId(): Int {
        return id
    }

    override fun getEquipmentName(): String {
        return name
    }

    override fun getEquipmentKeywords(): String {
        return keywords
    }

    companion object {
        @JvmStatic
        fun fromConfigLine(line : String) : Camera {
            val parts = line.split(';')
            val c = parts[0]
            val id = c.substring(c.indexOf('_') + 1)
            return Camera(id.toInt(), parts[1], parts[2].toInt(), parts[3].toInt(), parts[4].toFloat(), parts[5].toFloat(), parts[6])
        }
        @JvmStatic
        fun fromVector(v : Vector<Any>) : Camera = Camera(
            v[0] as Int,
            v[1] as String,
            v[2].toString().toInt(),
            v[3].toString().toInt(),
            v[4].toString().replace(',','.').toFloat(),
            v[5].toString().replace(',','.').toFloat(),
            v[6] as String
        )

    }
}
