package de.sdr.astro.cat.config

import java.util.*

data class Telescope(val id: Int, val name : String, val focalLength: Int, val aperture : Int, val keywords : String) : Equipment{
    fun toConfigLine(): String {
        return "telescope_$id;$name;$focalLength;$aperture;$keywords"
    }
   override fun toString(): String {
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
        fun fromConfigLine(line : String) : Telescope {
            val parts = line.split(';')
            val t = parts[0]
            val id = t.substring(t.indexOf('_') + 1)
            return Telescope(id.toInt(), parts[1], parts[2].toInt(), parts[3].toInt(), parts[4] )
        }

        @JvmStatic
        fun fromVector(v : Vector<Any>) : Telescope = Telescope(
            v[0] as Int,
            v[1] as String,
            v[2].toString().toInt(),
            v[3].toString().toInt(),
            v[4] as String
            )
    }
}
