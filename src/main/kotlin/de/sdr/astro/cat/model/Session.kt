package de.sdr.astro.cat.model

import de.sdr.astro.cat.util.Util
import java.io.File
import java.io.FilenameFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Session(path: String) : PathObject(path), Comparable<Session>  {

    val date: LocalDate
        get() = LocalDate.parse(name.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    val astroObjectName: String
        get() = getLastPathElement(1)

    val imageMap: MutableMap<String, MutableList<Image>> = mutableMapOf()

    init {
        readSession()
    }

    fun readSession() {

        // initialize lists for diff. image types
        for (type in imageTypes) {
            imageMap[type] = mutableListOf()
        }

        println("  Session: $name / $date")
        File(path)
            .walk(FileWalkDirection.TOP_DOWN)
            .sorted()
            .forEach {
                if (it.isFile) {
                    val pathObject = PathObject(it.path)
                    val folder = pathObject.getLastPathElement(1)
                    // light image
                    if (pathObject.isLightImage()) {
                        // a LightImage will treat a subfolder under "lights" as filter
                        val image = LightImage(it.path)
                        imageMap[Model.LIGHTS]?.add(image)
                    }
                    else
                    // flats, darks, biases, darkflats
                    if (pathObject.isCalibrationImage()) {
                        val image = Image(it.path, Model.deriveImageTypeFromParentFolderName(folder))
                        imageMap[folder]?.add(image)
                    }
                    // images directly under the session folder --> treated as RESULT images
                    else if (pathObject.isSessionResultImage(this.name)) {
                        // treat images directly under the session folder as results
                        val image = Image(it.path, Model.RESULT)
                        imageMap[Model.RESULTS]?.add(image)
                    }
                }
            }

        for ((type, images) in imageMap) {
            println("    ${type}: ${images.size}")
        }
    }


    /**
     * categorize lights according to:
     * - iso
     * - exposure time
     */
    fun aggregateIsoExposureGainBias(imagetype: String): Map<MapKeyFilterIsoExposureGainBias, Int> {
        val isoExpMap: MutableMap<MapKeyFilterIsoExposureGainBias, Int> = mutableMapOf()
        imageMap[imagetype]?.forEach {
            // create Map-key from iso and exposure
            val mapKey = MapKeyFilterIsoExposureGainBias(
                it.metadata.iso ?: 0,
                it.metadata.exposure ?: 0.0,
                it.metadata.gain ?: 0,
                it.metadata.bias ?: 0,
                if ( it is LightImage ) (it as LightImage).filter else ""
            )
            val count = isoExpMap[mapKey] ?: 0
            isoExpMap[mapKey] = count + 1
        }
        for (mapKey in isoExpMap.keys) {
            val count: Int? = isoExpMap[mapKey]
            println(" # of images in $mapKey = $count")
        }

        return isoExpMap
    }


    fun totalCaptureTime(imageType: String): Double {
        var sum = 0.0
        imageMap[imageType]?.forEach {
            sum += it.metadata.exposure!!
        }
        return sum / 60
    }

    /**
     * this is for lights only
     */
    fun startAndStopTime(): Pair<String?, String?> {
        return if (imageMap[Model.LIGHTS]?.size!! > 0) {
            val sortedImages = imageMap[Model.LIGHTS]?.sortedBy { it.metadata.datetime }?.toList()
            val start = sortedImages?.get(0)?.metadata?.time.toString()
            val end = sortedImages?.get(sortedImages.size - 1)?.metadata?.time.toString()
            Pair(start, end)
        } else Pair("", "")
    }

    fun getCameraEntryFromMetadata() : String  {
        var camera = ""
        // get first light image and check metadata
        if (imageMap[Model.LIGHTS]?.size!! > 0) {
            val image = imageMap[Model.LIGHTS]?.get(0)
            camera = image?.metadata?.camera ?: ""
        }
        return camera
    }

    fun hasResultImages() : Boolean {
        return ((imageMap[Model.RESULTS]?.size) ?: 0) > 0
    }

    fun guessBestResultImage(): Image? {
        var result : Image? = null
        if ( hasResultImages() ) {
            // prefer jpeg, tif, png over fits (fits takes long to load)
            result = mostSuitableImage( imageMap[Model.RESULTS]?.filter { it.isJpegTiffPng() } )
            // fallback to fits, when no other is there
            if ( result == null ) {
                result = mostSuitableImage( imageMap[Model.RESULTS]?.filter { it.isFits() } )
            }
        }
        return result
    }

    private fun mostSuitableImage(filtered: List<Image>? ): Image? {
        var result : Image? = null
        var max = 0.0
        for (image in filtered!!) {
            // if there is an image with "preview" in its name, just take this one
            if ( image.pureName.lowercase().indexOf("preview") >= 0) {
                result = image
                break
            }
            // find similarity based on Levenstein distance
            val similarity = Util.findSimilarity(image.pureName, astroObjectName)
            if (similarity > max) {
                result = image
                max = similarity
            }
        }
        return result
    }

    fun determineReadmeFileName() : String? {
        val readmeFileFilter = FilenameFilter { _, s: String ->
            s.lowercase().endsWith(".txt") && (s.lowercase().contains("readme"))
        }
        val readmeFiles = File(path).list( readmeFileFilter )
        if (readmeFiles?.isNotEmpty()!!)
            return readmeFiles[0]
        return "readme.txt"
    }

    override fun compareTo(other: Session): Int {
        return other.name.compareTo(this.name)
    }

    /**
     * analyzes the Lights of the session and sorts them to a map where the filter is key
     */
    fun createLightFiltersMap() : Map<String, List<Image>> {
        val filterMap : MutableMap<String, MutableList<Image>> = mutableMapOf();
        for ( image in imageMap[Model.LIGHTS]!! ) {
            val lightImage = image as LightImage
            if ( filterMap[lightImage.filter] == null ) {
                filterMap[lightImage.filter] = mutableListOf()
            }
            filterMap[lightImage.filter]?.add(lightImage)
        }
        return filterMap
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Session

        if (path != other.path) return false

        return true
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

