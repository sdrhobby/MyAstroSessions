package de.sdr.astro.cat.model

open class LightImage : Image {
    override var type = Model.LIGHT
    var filter : String = ""

    constructor(path : String): super(path, Model.LIGHT)
    constructor(path : String, imageType : String, filter : String ): super(path, imageType) {
        this.filter = filter
    }

    init {
        // if there is a subfolder under "lights" treat it as filter
        if ( Model.LIGHTS.equals(getLastPathElement(2))) {
            filter = getLastPathElement(1)
        }
    }
}