package de.sdr.astro.cat.metadata

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExifData(val path: String) {

    private val relevantTags = mapOf(
        "camera" to "Model",
        "width" to "Image Width",
        "height" to "Image Height",
        "exposure" to "Exposure Time",
        "iso" to "ISO Speed Ratings",
        "gain" to "GAIN",   // TODO: check
        "bias" to "BIAS",   // TODO: check

        "cfa" to "CFA Pattern",
        "sensing-method" to "Sensing Method",
        "file-size" to "File Size",
        "make" to "Make",
        "date" to "Date/Time",
        "time" to "Date/Time"
    )
    private val relevantInfo: MutableMap<String, String> = mutableMapOf()
    var metadata : Metadata? = null

    init {
        try {
            metadata = ImageMetadataReader.readMetadata(File(path))

            for (directory in metadata!!.directories) {
                for (tag in directory.tags) {
                    if (relevantTags.values.contains(tag.tagName)) {
                        this.relevantInfo[tag.tagName] = tag.description
                    }
                }
            }
        } catch (x: Exception) {
            System.err.println("unable to read EXIF data from: $path")
        }
    }

    fun getCameraModel(): String? {
        return relevantInfo[relevantTags["camera"]]
    }

    fun getCameraMaker(): String? {
        return relevantInfo[relevantTags["make"]]
    }

    fun getWidth(): String? {
        return relevantInfo[relevantTags["width"]]?.filter { it.isDigit() }
    }

    fun getHeight(): String? {
        return relevantInfo[relevantTags["height"]]?.filter { it.isDigit() }
    }

    fun getExposureTime(): Double {
        // 1497/5 sec
        val parts = relevantInfo[relevantTags["exposure"]]?.split(" ")
        val s = parts?.get(0) ?: "0/1"
        // handle different formats (1/4000 vs. 0.2)
        if (s.indexOf("/") > 0) {
            val value = s.split("/")
            return value[0].toDouble() / value[1].toDouble()
        }
        return s.toDouble()
    }

    fun getIso(): String? {
        return relevantInfo[relevantTags["iso"]]?.filter { it.isDigit() }
    }
    fun getGain(): String? {
        return relevantInfo[relevantTags["gain"]]?.filter { it.isDigit() }
    }
    fun getBias(): String? {
        return relevantInfo[relevantTags["bias"]]?.filter { it.isDigit() }
    }


    fun getCFAMode(): String? {
        return relevantInfo[relevantTags["cfa"]]
    }

    fun getSensingMethod(): String? {
        return relevantInfo[relevantTags["sensing-method"]]
    }

    fun getFileSize(): String? {
        return relevantInfo[relevantTags["file-size"]]?.filter { it.isDigit() }
    }

    fun getDate(): LocalDate? {
        val s = relevantInfo[relevantTags["date"]]
        val d = s?.substring(0, s.indexOf(" "))
        return LocalDate.parse(d, DateTimeFormatter.ofPattern("yyyy:MM:dd"))
    }

    fun getTime(): LocalTime? {
        val s = relevantInfo[relevantTags["date"]]
        val d = s?.substring(s.indexOf(" ") + 1)
        return LocalTime.parse(d)
    }

    fun getAllData() : List<List<String>> {
        val rows : MutableList<List<String>> = mutableListOf()
        for (directory in metadata!!.directories) {
            for (tag in directory.tags) {
                rows.add( listOf(tag.tagName?: "", tag.description?: "", "") )
            }
        }
        return rows
    }

}