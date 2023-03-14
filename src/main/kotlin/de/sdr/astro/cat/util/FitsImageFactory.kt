package de.sdr.astro.cat.util

import nom.tam.fits.Fits
import nom.tam.fits.ImageHDU
import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.awt.image.Raster

class FitsImageFactory {
    val cache: LRUCache = LRUCache(20)

    companion object {
        private var instance: FitsImageFactory? = null

        @JvmStatic
        private fun getInstance(): FitsImageFactory {
            if (instance == null) {
                instance = FitsImageFactory()
            }
            return instance!!
        }
        @JvmStatic
        fun getFitsImage(path: String): BufferedImage {
            // try to get the viewer from the factory cache, identified by image path
            var fitsImage = getInstance().cache.get(path)
            if (fitsImage == null) {
                try {
                    fitsImage = getInstance().readFitsImage(path)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // put this new instance to the local factory LRUcache
                getInstance().cache.add(path, fitsImage as BufferedImage)
            }
            return fitsImage as BufferedImage
        }

    }

    private fun readFitsImage(path: String ) : BufferedImage {
        val f: Fits = Fits(path)
        val hdu = f.readHDU() as ImageHDU
        val bitPix = Math.abs( hdu.bitPix )
        val bZero = hdu.bZero.toInt()
        val nAxis = hdu.header.getIntValue("NAXIS")
        val w = hdu.header.getIntValue("NAXIS1")
        val h = hdu.header.getIntValue("NAXIS2")
        val rowOrder = hdu.header.getIntValue("ROWORDER")
        val bottomUP = "BOTTOM-UP".equals(rowOrder)

        println(hdu.size)
        hdu.info(System.out)
        println("Bits per pixel: $bitPix")
        println(String.format("image width: %d  height: %d ", w, h))

        val imageData = hdu.data
        val o = imageData.data
        var data : IntArray = IntArray(0)
        if ( nAxis == 3 ) {
            if (bitPix == 16) {
                data = getIntArrFrom3AxisShorts(o as Array<Array<ShortArray>>, w, h, bZero, bottomUP)
            } else if (bitPix == 32) {
                data = getIntArrFrom3AxisFloats(o as Array<Array<FloatArray>>, w, h, bottomUP)
            }
        }
        else {
            data = getIntArrFrom2AxisShorts(o as Array<ShortArray>, w, h, bZero, bottomUP)
        }
        return createBufferedImageGray(h, w, data)
    }

    private fun getIntArrFrom2AxisShorts(o : Array<ShortArray>, w : Int, h: Int, bZero : Int, bottomUp : Boolean ) : IntArray {
        val data = IntArray(w * h)
        for (j in 0 until h) {
            val offset = if ( bottomUp ) j * w else (h-j-1) * w;
            for (k in 0 until w) {
                data[offset + k] = o[j][k] + bZero
            }
        }
        return data;
    }

    private fun getIntArrFrom3AxisShorts(o : Array<Array<ShortArray>>, w : Int, h: Int, bZero : Int, bottomUp : Boolean ) : IntArray {
        val data = IntArray(w * h)
        for (j in 0 until h) {
            val offset = if ( bottomUp ) j * w else (h-j-1) * w;
            for (k in 0 until w) {
                data[offset + k] = o[1][j][k] + bZero
            }
        }
        return data;
    }

    private fun getIntArrFrom3AxisFloats(o : Array<Array<FloatArray>>, w : Int, h: Int, bottomUp : Boolean ) : IntArray {
        val data = IntArray(w * h)
        for (j in 0 until h) {
            val offset = if ( bottomUp ) j * w else (h-j-1) * w;
            for (k in 0 until w) {
                // values are relative between 0 and 1.0 --> scale up
                data[offset + k] = (o[1][j][k] * 1.99 * 32768).toInt()
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

}