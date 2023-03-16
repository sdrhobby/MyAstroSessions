package de.sdr.astro.cat.config

interface Equipment {
    fun getEquipmentId() : Int

    fun getEquipmentName() : String

    fun getEquipmentKeywords() : String
}