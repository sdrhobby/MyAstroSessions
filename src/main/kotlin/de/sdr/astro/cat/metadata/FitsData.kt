package de.sdr.astro.cat.metadata

import nom.tam.fits.Fits
import nom.tam.fits.Header
import nom.tam.fits.ImageHDU

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

    private var fitsHeader : Header

    init {
        val fitsFile: Fits = Fits(path)
        val hdu = fitsFile.readHDU() as ImageHDU
        fitsHeader = hdu.header
    }

    fun dumpAllKeys() {
        fitsHeader.dumpHeader( System.out )
    }

    fun getAllMetadata() : List<List<String>> {
        val rows : MutableList<List<String>> = mutableListOf()
        fitsHeader.iterator().forEach {
            val row : MutableList<String> = mutableListOf()
            row.add( it.key )
            row.add( if (it.value != null) it.value else "")
            row.add( if ( it.comment != null) it.comment else "")
            // ensure we have 3 elements
            while (row.size < 3)
                row.add("")
            rows.add( row )
        }
        return rows
    }

    fun getCameraModel(): String? {
        return fitsHeader.getStringValue(keys["camera"])
    }

    fun getWidth(): Int {
        return fitsHeader.getIntValue(keys["width"])
    }

    fun getHeight(): Int {
        return fitsHeader.getIntValue(keys["height"])
    }

    fun getExposureTime(): Double? {
        return fitsHeader.getDoubleValue(keys["exposure"])
    }

    fun getIso(): Int? {
        return fitsHeader.getIntValue(keys["iso"])
    }
    fun getGain(): Int? {
        return fitsHeader.getIntValue(keys["gain"])
    }
    fun getBias(): Int? {
        return fitsHeader.getIntValue(keys["bias"])
    }

    fun getCFAMode(): String? {
        return fitsHeader.getStringValue(keys["cfa"])
    }
}