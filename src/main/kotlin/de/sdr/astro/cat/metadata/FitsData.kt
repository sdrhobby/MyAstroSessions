package de.sdr.astro.cat.metadata

import nom.tam.fits.Fits
import nom.tam.fits.Header
import nom.tam.fits.ImageHDU

class FitsData(val path: String) {

    private val keys = mapOf(
        Constants.CAMERA to "INSTRUME",
        Constants.WIDTH to "NAXIS1",
        Constants.HEIGHT to "NAXIS2",
        Constants.EXPOSURE to "EXPTIME",
        Constants.ISO to "ISOSPEED",
        Constants.GAIN to "GAIN",   // TODO: double-check
        Constants.BIAS to "BIAS",   // TODO: double-check

        Constants.CFA to "BAYERPAT",  // cfa = color filter array
        Constants.MOUNT to "TELESCOP",
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

    fun getMount(): String? {
        return fitsHeader.getStringValue(keys["mount"])
    }

    /**
     * request a value from the info map
     * The key may be one of the predefined values in @ref Constants, which is common for EXIF and FITS
     * or it may be a native Exif key.
     * @param key ... see above
     * @return value: String ... the corrsponding value as String (without any treatment) or null, if nothing is found
     */
    fun getValueByKey(key: String) : String? {
        val translatedKey = if ( keys[key] != null ) keys[key] else key;
        return fitsHeader.getStringValue(translatedKey)
    }

}