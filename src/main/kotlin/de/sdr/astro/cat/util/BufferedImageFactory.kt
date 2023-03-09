package de.sdr.astro.cat.util

import java.awt.image.BufferedImage
import java.io.FileInputStream
import javax.imageio.ImageIO

class BufferedImageFactory {
    val cache: LRUCache = LRUCache(20)

    companion object {
        private var instance: BufferedImageFactory? = null

        @JvmStatic
        private fun getInstance(): BufferedImageFactory {
            if (instance == null) {
                instance = BufferedImageFactory()
            }
            return instance!!
        }

        @JvmStatic
        fun getImage(imagePath: String): BufferedImage {
            var img = getInstance().cache.get(imagePath)
            if (img == null) {
                img = ImageIO.read( FileInputStream(imagePath))
                getInstance().cache.add( imagePath, img )
            }
            return img as BufferedImage
        }
    }

}