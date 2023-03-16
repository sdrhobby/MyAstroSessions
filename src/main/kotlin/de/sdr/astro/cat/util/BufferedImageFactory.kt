package de.sdr.astro.cat.util

import nom.tam.fits.Fits
import nom.tam.fits.ImageHDU
import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.Raster
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
                if (imagePath.lowercase().endsWith("fit") || imagePath.lowercase().endsWith("fits"))
                    img = getInstance().readFitsImage(imagePath)
                else
                    img = getInstance().getImageIoImage(imagePath)
                getInstance().cache.add(imagePath, img!!)
            }
            return img as BufferedImage
        }
    }

    private fun getImageIoImage(imagePath: String): BufferedImage {
        return ImageIO.read(FileInputStream(imagePath))
    }

    private fun readFitsImage(path: String): BufferedImage? {
        val f: Fits = Fits(path)
        val hdu = f.readHDU() as ImageHDU
        val bitPix = Math.abs(hdu.bitPix)
        val bZero = hdu.bZero.toInt()
        val nAxis = hdu.header.getIntValue("NAXIS")
        val w = hdu.header.getIntValue("NAXIS1")
        val h = hdu.header.getIntValue("NAXIS2")
        val rowOrder = hdu.header.getIntValue("ROWORDER")
        val bottomUP = "BOTTOM-UP".equals(rowOrder)

//        println(hdu.size)
        hdu.info(System.out)
//        println("Bits per pixel: $bitPix")
//        println(String.format("image width: %d  height: %d ", w, h))

        val imageData = hdu.data
        val o = imageData.data
        var data: IntArray = IntArray(0)
        if (nAxis == 3) {
            if (bitPix == 16) {
                data = getRGBIntArrFrom3AxisShorts(o as Array<Array<ShortArray>>, w, h, bZero, bottomUP)
                return createBufferedImageRGB(h, w, data)
            } else if (bitPix == 32) {
                data = getRGBIntArrFrom3AxisFloats(o as Array<Array<FloatArray>>, w, h, bottomUP)
                return createBufferedImageRGB(h, w, data)
            }
        } else {
            data = getIntArrFrom2AxisShorts(o as Array<ShortArray>, w, h, bZero, bottomUP)
            return createBufferedImageGray(h, w, data)
        }
        return null
    }

    private fun getIntArrFrom2AxisShorts(
        o: Array<ShortArray>,
        w: Int,
        h: Int,
        bZero: Int,
        bottomUp: Boolean
    ): IntArray {
        val data = IntArray(w * h)
        for (j in 0 until h) {
            val offset = if (bottomUp) j * w else (h - j - 1) * w;
            for (k in 0 until w) {
                data[offset + k] = o[j][k] + bZero
            }
        }
        return data;
    }

    private fun getIntArrFrom3AxisShorts(
        o: Array<Array<ShortArray>>,
        w: Int,
        h: Int,
        bZero: Int,
        bottomUp: Boolean
    ): IntArray {
        val data = IntArray(w * h)
        for (j in 0 until h) {
            val offset = if (bottomUp) j * w else (h - j - 1) * w;
            for (k in 0 until w) {
                data[offset + k] = o[1][j][k] + bZero
            }
        }
        return data;
    }

    private fun getRGBIntArrFrom3AxisShorts(
        o: Array<Array<ShortArray>>,
        w: Int,
        h: Int,
        bZero: Int,
        bottomUp: Boolean
    ): IntArray {
        val data = IntArray(w * h)
        for (j in 0 until h) {
            val offset = if (bottomUp) j * w else (h - j - 1) * w;
            for (k in 0 until w) {
                val r = (o[0][j][k] + bZero) shr 8      // shr 8 entspricht:  * 255 / 2^16
                val g = (o[1][j][k] + bZero) shr 8
                val b = (o[2][j][k] + bZero) shr 8
                data[offset + k] = (r shl 16) or (g shl 8) or b
            }
        }
        return data;
    }

    private fun getGrayIntArrFrom3AxisFloats(o: Array<Array<FloatArray>>, w: Int, h: Int, bottomUp: Boolean): IntArray {
        val data = IntArray(w * h)
        for (j in 0 until h) {
            val offset = if (bottomUp) j * w else (h - j - 1) * w;
            for (k in 0 until w) {
                // values are relative between 0 and 1.0 --> scale up
                data[offset + k] = (o[1][j][k] * 1.99 * 32768).toInt()
            }
        }
        return data;
    }

    private fun getRGBIntArrFrom3AxisFloats(o: Array<Array<FloatArray>>, w: Int, h: Int, bottomUp: Boolean): IntArray {
        val data = IntArray(w * h)
        for (j in 0 until h) {
            val offset = if (bottomUp) j * w else (h - j - 1) * w;
            for (k in 0 until w) {
                // values are relative between 0 and 1.0 --> scale up channels to 8 bit RGB
                val r = (o[0][j][k] * 255).toInt()
                val g = (o[1][j][k] * 255).toInt()
                val b = (o[2][j][k] * 255).toInt()
                // encode as rgb bytes of single int value
                data[offset + k] = (r shl 16) or (g shl 8) or b
            }
        }
        return data;
    }

    //    private int[] extractFirstBandFromShorts( short[][][] shortData, int bZero )
    private fun createBufferedImageGray(h: Int, w: Int, data: IntArray): BufferedImage {
        val img = BufferedImage(w, h, BufferedImage.TYPE_USHORT_GRAY)
        val buffer = DataBufferInt(data, w * h)
        val raster = Raster.createRaster(img.sampleModel, buffer, Point())
        img.data = raster
        return img
    }

    private fun createBufferedImageRGB(h: Int, w: Int, data: IntArray): BufferedImage {
        val img = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        val buffer = DataBufferInt(data, w * h)
        val raster = Raster.createRaster(img.sampleModel, buffer, Point())
        img.data = raster
        return img
    }
}