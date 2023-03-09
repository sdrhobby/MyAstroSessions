package de.sdr.astro.cat.model

import java.io.File

class Model(folder: String) {

    private val topLevelFolder = folder
    val astroObjects: MutableList<AstroObject> = mutableListOf()

    init {
        // using extension function walk
        File(topLevelFolder)
            .walk(FileWalkDirection.TOP_DOWN)
            .maxDepth(1)
            .onEnter { folder -> shallBeEntered(folder) }
            .sorted()
            .forEach {
                val subPath = it.path.substring(topLevelFolder.length)
                if (subPath.isNotEmpty()) {
                    val astroObject = AstroObject(it.path, true)
                    astroObjects.add(astroObject)
                }
            }
    }

    private fun shallBeEntered(folder: File): Boolean {
        return !folder.path.lowercase().endsWith("masters")
    }

    companion object {
        const val SCAFFOLD: String = "SCAFFOLD"
        const val ASTRO_OBJECT: String = "ASTRO_OBJECT"
        const val SESSION: String = "SESSION"
        // groups of images
        const val LIGHTS: String = "lights"
        const val FILTER: String = "FILTER"
        const val FLATS: String = "flats"
        const val DARKS: String = "darks"
        const val DARKFLATS: String = "darkflats"
        const val BIASES: String = "biases"
        const val RESULTS: String = "results"
        // individual images
        const val LIGHT: String = "light"
        const val FLAT: String = "flat"
        const val DARK: String = "dark"
        const val DARKFLAT: String = "darkflat"
        const val BIAS: String = "bias"
        const val RESULT: String = "result"
        const val YEAR: String = "year"
        const val MONTH: String = "month"

        @JvmStatic
        fun deriveImageTypeFromParentFolderName(parent: String): String {
            when (parent) {
                LIGHTS -> return LIGHT
                FLATS -> return FLAT
                DARKS -> return DARK
                BIASES -> return BIAS
                RESULTS -> return RESULT
            }
            return ""
        }
    }

}