package de.sdr.astro.cat.model

import de.sdr.astro.cat.metadata.*

open class Image : PathObject {

    open var type = Model.deriveImageTypeFromParentFolderName( getLastPathElement(1) )
    var metadata: ImageMetadata = ImageMetadata("", 0, 0, 0.0, 0, 0, 0, "")

    constructor(path: String, imageType : String) : super(path) {
        type = imageType
    }
    init {
        if (isRaw()) {
            metadata = ImageMetadata.fromExif(ExifData(path))
        } else if (isFits()) {
            metadata = ImageMetadata.fromFits(FitsData(path))
        }
        metadata.addFileNameData(FileNameData(this))
    }

    fun getMetadataValue(key : String) : String? {
        return metadata?.getMetadataByKey(key)
    }
}