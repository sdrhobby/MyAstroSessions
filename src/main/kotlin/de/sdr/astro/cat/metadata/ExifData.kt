package de.sdr.astro.cat.metadata

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Metadata
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ExifData(val path: String) {

    private val relevantTags = mapOf(
        Constants.CAMERA to "Model",
        Constants.WIDTH to "Image Width",
        Constants.HEIGHT to "Image Height",
        Constants.EXPOSURE to "Exposure Time",
        Constants.ISO to "ISO Speed Ratings",
        Constants.GAIN to "GAIN",   // TODO: check
        Constants.BIAS to "BIAS",   // TODO: check

        Constants.CFA to "CFA Pattern",
        "sensing-method" to "Sensing Method",
        "file-size" to "File Size",
        "make" to "Make",
        Constants.DATE to "Date/Time",
        Constants.TIME to "Date/Time"
    )
    private val info: MutableMap<String, String> = mutableMapOf()
    var metadata: Metadata? = null

    init {
        try {
            metadata = ImageMetadataReader.readMetadata(File(path))

            for (directory in metadata!!.directories) {
                for (tag in directory.tags) {
                    // the "relevant" entries will get keys common for EXIF and FIT
                    val relevantKey: String? = getMapKey(relevantTags, tag.tagName)
                    if (relevantKey != null) {
                        this.info[relevantKey] = tag.description
                    } else {
                        // the rest will use their native Exif tag-names
                        if (tag.description != null)
                            this.info[tag.tagName] = tag.description
                    }
                }
            }
        } catch (x: Exception) {
            System.err.println("unable to read EXIF data from: $path")
        }
    }

    fun <K, V> getMapKey(map: Map<K, V>, target: V): K? {
        for ((key, value) in map) {
            if (target == value) {
                return key
            }
        }
        return null
    }

    fun getCameraModel(): String? {
        return info[Constants.CAMERA]
    }

    fun getCameraMaker(): String? {
        return info[relevantTags["make"]]
    }

    fun getWidth(): String? {
        return info[Constants.WIDTH]?.filter { it.isDigit() }
    }

    fun getHeight(): String? {
        return info[Constants.HEIGHT]?.filter { it.isDigit() }
    }

    fun getExposureTime(): Double {
        // 1497/5 sec
        val parts = info[Constants.EXPOSURE]?.split(" ")
        val s = parts?.get(0) ?: "0/1"
        // handle different formats (1/4000 vs. 0.2)
        if (s.indexOf("/") > 0) {
            val value = s.split("/")
            return value[0].toDouble() / value[1].toDouble()
        }
        return s.toDouble()
    }

    fun getIso(): String? {
        return info[Constants.ISO]?.filter { it.isDigit() }
    }

    fun getGain(): String? {
        return info[Constants.GAIN]?.filter { it.isDigit() }
    }

    fun getBias(): String? {
        return info[Constants.BIAS]?.filter { it.isDigit() }
    }


    fun getCFAMode(): String? {
        return info[Constants.CFA]
    }

    fun getSensingMethod(): String? {
        return info[relevantTags["sensing-method"]]
    }

    fun getFileSize(): String? {
        return info[relevantTags["file-size"]]?.filter { it.isDigit() }
    }

    fun getDate(): LocalDate? {
        val s = info[Constants.DATE]
        val d = s?.substring(0, s.indexOf(" "))
        return LocalDate.parse(d, DateTimeFormatter.ofPattern("yyyy:MM:dd"))
    }

    fun getTime(): LocalTime? {
        val s = info[Constants.DATE]
        val d = s?.substring(s.indexOf(" ") + 1)
        return LocalTime.parse(d)
    }

    fun getAllData(): List<List<String>> {
        val rows: MutableList<List<String>> = mutableListOf()
        for (directory in metadata!!.directories) {
            for (tag in directory.tags) {
                rows.add(listOf(tag.tagName ?: "", tag.description ?: "", ""))
            }
        }
        return rows
    }

    /**
     * request a value from the info map
     * The key may be one of the predefined values in @ref Constants, which is common for EXIF and FITS
     * or it may be a native Exif key.
     * @param key ... see above
     * @return value: String ... the corrsponding value as String (without any treatment) or null, if nothing is found
     */
    fun getValueByKey(key: String): String? {
        return info[key]
    }

}