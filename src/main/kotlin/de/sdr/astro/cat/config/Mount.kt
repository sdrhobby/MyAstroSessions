package de.sdr.astro.cat.config

import java.util.*

data class Mount(val id: Int, val name: String, val keywords: String) : Equipment {
    fun toConfigLine(): String {
        return "mount_$id;$name;$keywords"
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
        fun fromConfigLine(line: String): Mount {
            val parts = line.split(';')
            val t = parts[0]
            val id = t.substring(t.indexOf('_') + 1)
            return Mount(id.toInt(), parts[1], parts[2])
        }

        @JvmStatic
        fun fromVector(v: Vector<Any>): Mount = Mount (
            v[0] as Int,
            v[1] as String,
            v[2] as String
        )

    }
}
