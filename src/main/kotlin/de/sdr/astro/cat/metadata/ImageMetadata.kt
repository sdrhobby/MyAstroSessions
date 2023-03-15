package de.sdr.astro.cat.metadata

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class ImageMetadata (
    val camera : String?,
    val width : Int?,
    val height : Int?,
    var exposure : Double?,
    val iso : Int?,
    val gain : Int?,
    val bias : Int?,
    val mount : String?
    // TODO: add coordinates from FITS

)
{
    var date : LocalDate = LocalDate.MIN
    var time : LocalTime = LocalTime.MIN
    var datetime : LocalDateTime = LocalDateTime.MIN
    var exifData : ExifData? = null
    var fitsData: FitsData? = null

    fun addFileNameData( fileNameData: FileNameData) {
        // prefer date, time and exposure from the filename, if available
        date = fileNameData.getDate()?: date
        time = fileNameData.getTime()?: time
        datetime = LocalDateTime.of(date, time)
        exposure = fileNameData.getExposureTime()?: exposure?:0.0
    }

    // for representation in a table
    fun baseDataAs2DArray() : List<List<String>> {
        val rows : MutableList<List<String>> = mutableListOf()
        rows.add( listOf("Camera", camera?: "", "") )
        rows.add( listOf("Width", (width?: 0).toString(), "Image width") )
        rows.add( listOf("Height", (height?: 0).toString(), "Image height") )
        rows.add( listOf("Exposure", (exposure?: 0).toString(), "Image exposure in seconds") )
        rows.add( listOf("ISO", (iso?: 0).toString(), "ISO capture setting") )
        // TODO: double-check keys for gain and bias
        rows.add( listOf("GAIN", (gain?: 0).toString(), "GAIN setting") )
        rows.add( listOf("BIAS", (bias?: 0).toString(), "BIAS setting") )
        return rows
    }

    fun detailDataAs2DArray() : List<List<String>> {
        var details : List<List<String>> = listOf()
        if ( fitsData != null )
            details = fitsDataAs2DArray()
        if ( exifData != null ) {
            details = exifDataAs2DArray()
        }
        return details
    }

    private fun fitsDataAs2DArray() : List<List<String>> {
        val rows : MutableList<List<String>> = mutableListOf()
        if ( fitsData == null )
            return rows
        return fitsData!!.getAllMetadata()
    }

    private fun exifDataAs2DArray() : List<List<String>> {
        val rows : MutableList<List<String>> = mutableListOf()
        if ( exifData == null )
            return rows
        return exifData!!.getAllData()
    }

    companion object
}

fun ImageMetadata.Companion.fromExif( exif : ExifData ): ImageMetadata {
    val metadata = ImageMetadata(
        exif.getCameraModel(),
        exif.getWidth()?.toInt(),
        exif.getHeight()?.toInt(),
        exif.getExposureTime(),
        exif.getIso()?.toInt(),
        exif.getGain()?.toInt(),
        exif.getBias()?.toInt(),
        "" // there is no telescope data in EXIF
    )
    metadata.date = exif.getDate()!!
    metadata.time = exif.getTime()!!
    // remember exif data for Image Detail view
    metadata.exifData = exif
    return metadata
}
fun ImageMetadata.Companion.fromFits( fits : FitsData): ImageMetadata {

    val metadata = ImageMetadata(
        fits.getCameraModel(),
        fits.getWidth(),
        fits.getHeight(),
        fits.getExposureTime(),
        fits.getIso(),
        fits.getGain(),
        fits.getBias(),
        fits.getMount()
    )
    metadata.fitsData = fits
    return metadata
}