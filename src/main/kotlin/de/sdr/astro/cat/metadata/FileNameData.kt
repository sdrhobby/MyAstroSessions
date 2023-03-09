package de.sdr.astro.cat.metadata

import de.sdr.astro.cat.model.Image
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * parsing of Filenames like:   M-82_Light_180_secs_2022-07-31T00-32-35_004.nef
 *                               M42_Light_120_secs_2023-02-04T20-44-05_001.nef
 * extracting:
 * - exposure time
 * - date
 * - time
 * Object name will be ignored, because it might be screwed up (if not set correctly in Ekos)
 */
class FileNameData(val image: Image) {

    private val nameParts: List<String> = image.name.split("_")

    // NOTE, the namePart elements are indexed from last one with negative index, because already the name may include a separator "_"
    fun getType(): String {
        if (nameParts.size < 6)
            return ""
        return nameParts[nameParts.size - 5].lowercase()
    }

    fun getExposureTime(): Double? {
        if (nameParts.size < 6)
            return null
        return nameParts[nameParts.size - 4].toDouble()
    }

    fun getDate(): LocalDate? {
        if (nameParts.size < 6)
            return null
        val s = nameParts[nameParts.size - 2]
        val dateString = s.substring(0, s.indexOf("T"))
        return LocalDate.parse(dateString)
    }

    fun getTime(): LocalTime? {
        if (nameParts.size < 6)
            return null
        val s = nameParts[nameParts.size - 2]
        val dateString = s.substring(s.indexOf("T") + 1)
        return LocalTime.parse(dateString, DateTimeFormatter.ofPattern("HH-mm-ss"))
    }

}