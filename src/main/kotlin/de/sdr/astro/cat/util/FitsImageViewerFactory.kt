package de.sdr.astro.cat.util

import de.sdr.astro.cat.metadata.FitsData
import de.sdr.astro.cat.model.Image
import eap.fits.FitsException
import eap.fitsbrowser.FitsImageViewer

class FitsImageViewerFactory {
    val cache: LRUCache = LRUCache(20)

    companion object {
        private var instance: FitsImageViewerFactory? = null

        @JvmStatic
        private fun getInstance(): FitsImageViewerFactory {
            if (instance == null) {
                instance = FitsImageViewerFactory()
            }
            return instance!!
        }
        @JvmStatic
        fun getFitsImageViewer(image: Image, showGamma: Boolean): FitsImageViewer? {
            if (!image.isFits())
                return null
            // try to get the viewer from the factory cache, identified by image path
            var fitsImageViewer = getInstance().cache.get(image.path)
            if (fitsImageViewer == null) {
                try {
                    val fitsData: FitsData? = image.metadata.fitsData
                    fitsImageViewer = FitsImageViewer(fitsData?.getFitsImage())
                    fitsImageViewer.showProgress()
                    if (showGamma)
                        fitsImageViewer.allowGammaCorrection()
                } catch (e: FitsException) {
                    throw RuntimeException(e)
                }
                // put this new instance to the local factory LRUcache
                getInstance().cache.add(image.path, fitsImageViewer)
            }
            return fitsImageViewer as FitsImageViewer
        }

    }


}