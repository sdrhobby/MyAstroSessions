package de.sdr.astro.cat.metadata

import eap.fits.FitsImageData
import eap.fits.InputStreamFitsFile
import org.eso.fits.FitsFile
import org.eso.fits.FitsHDUnit
import org.eso.fits.FitsHeader
import java.io.FileInputStream


class FitsData(val path: String) {

    private val keys = mapOf(
        "camera" to "INSTRUME",
        "width" to "NAXIS1",
        "height" to "NAXIS2",
        "exposure" to "EXPTIME",
        "iso" to "ISOSPEED",
        "gain" to "GAIN",   // TODO: double-check
        "bias" to "BIAS",   // TODO: double-check

        "cfa" to "BAYERPAT",  // cfa = color filter array
        "telescope" to "TELESCOP",
        "pixsizex" to "PIXSIZE1",
        "pixsizey" to "PIXSIZE2",
        "binx" to "XBINNING",
        "biny" to "YBINNING",
        "bin-pixsize-x" to "XPIXSZ",
        "bin-pixsize-y" to "YPIXSZ",
        "focal-length" to "FOCALLEN",
        "aperture" to "APTDIA",
        "arc-per-pix" to "SCALE",
        "location-lat" to "SITELAT",
        "location-long" to "SITELONG",
        "location-long" to "SITELONG",
        "airmass" to "AIRMASS"

        // TODO: coordinates
    )

    private var fitsHeader : FitsHeader

    init {
        val ffile = FitsFile(path) // open FITS file

        val hdu: FitsHDUnit = ffile.getHDUnit(0)
        fitsHeader = hdu.header
    }

    fun dumpAllKeys() {
        fitsHeader.keywords.forEach {
            println(it)
        }
    }

    fun getAllMetadata() : List<List<String>> {
        val rows : MutableList<List<String>> = mutableListOf()
        fitsHeader.keywords.forEach {

            val row :MutableList<String> = mutableListOf()
            val splitted = it.toString().split( "=", "/")
            for ( element in splitted ) {
                row.add(element.trim())
            }
            // ensure we have 3 elements
            while (row.size < 3)
                row.add("")
            rows.add( row )
        }
        return rows
    }

    fun getCameraModel(): String? {
        return fitsHeader.getKeyword(keys["camera"])?.string
    }

    fun getWidth(): Int {
        return fitsHeader.getKeyword(keys["width"]).int
    }

    fun getHeight(): Int {
        return fitsHeader.getKeyword(keys["height"]).int
    }

    fun getExposureTime(): Double? {
        return fitsHeader.getKeyword(keys["exposure"])?.real
    }

    fun getIso(): Int? {
        return fitsHeader.getKeyword(keys["iso"])?.int
    }
    fun getGain(): Int? {
        return fitsHeader.getKeyword(keys["gain"])?.int
    }
    fun getBias(): Int? {
        return fitsHeader.getKeyword(keys["bias"])?.int
    }

    fun getCFAMode(): String? {
        return fitsHeader.getKeyword(keys["cfa"])?.string
    }

    fun getFitsImage(): FitsImageData {
        val fitsInputStream = InputStreamFitsFile(FileInputStream(path))
        val fitsHDU = fitsInputStream.getHDU(0)
        return fitsHDU.data as FitsImageData
    }

}