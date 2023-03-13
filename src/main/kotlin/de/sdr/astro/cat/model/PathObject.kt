package de.sdr.astro.cat.model

import de.sdr.astro.cat.config.Config

open class PathObject(val path: String) {

    val calibrationImageTypes = arrayOf(Model.DARKS, Model.FLATS, Model.BIASES, Model.DARKFLATS)
    val imageTypes = arrayOf(Model.LIGHTS, Model.DARKS, Model.FLATS, Model.BIASES, Model.RESULTS)
    open val name: String
        get() = getLastPathElement()
    open val extension: String
        get() = name.substring(name.lastIndexOf(".") + 1).lowercase()

    open val pureName : String
        get() = name.substring( 0, name.lastIndexOf("."))

    open val folder: String
        get() {
            return path.substring(0, path.lastIndexOf('/'))
        }

    fun getLastPathElement(i: Int = 0): String {
        val elements = path.split("/")
        return elements[elements.size - 1 - i]
    }

    fun isCalibrationImage(): Boolean {
        // check if it is in one of the image folders of a session (biases, darks, flats)
        return calibrationImageTypes.contains(getLastPathElement(1).lowercase()) && Config.getInstance().capturedImageExtensions().contains(extension)
    }
    fun isLightImage(): Boolean {
        // check if the image is either directly in the lights folder or in a direct filter-subfolder (e.g. lights/R)
        val lastPathSegment = getLastPathElement(1).lowercase()
        if (lastPathSegment.startsWith('.') || lastPathSegment.startsWith('_'))
            return false
        val parentPathSegment = getLastPathElement(2).lowercase()
        return Config.getInstance().capturedImageExtensions().contains(extension) &&
                (Model.LIGHTS.equals(lastPathSegment) || Model.LIGHTS.equals(parentPathSegment))
    }

    fun isFilter(): Boolean {
        val parentPathSegment = getLastPathElement(1)
        return (Model.LIGHTS.equals( parentPathSegment ) &&
            ! name.startsWith('.') &&
            ! name.startsWith('_'));
    }

    fun isSessionResultImage(sessionName : String): Boolean {
        // check if it is directly in the session folder or in a "result" subfolder
        val parentFolder = getLastPathElement(1)
        val folderMatch = Model.RESULTS.equals(parentFolder) || sessionName.equals(parentFolder)
        val extensionMatch = Config.getInstance().resultImageExtensions().contains(extension)
        return folderMatch && extensionMatch
    }

    fun isFits(): Boolean {
        return arrayOf("fit", "fits").contains(extension.lowercase())
    }
    fun isJpegTiffPng(): Boolean {
        return arrayOf("jpg", "jpeg", "tiff", "tif", "png").contains(extension.lowercase())
    }

    fun isRaw() : Boolean {
        return arrayOf("nef").contains(extension.lowercase())
    }

    fun isText() : Boolean {
        return arrayOf("txt").contains(extension.lowercase())
    }

    override fun equals(other: Any?): Boolean {
        return (other is PathObject ) && (this.path == other.path)
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}