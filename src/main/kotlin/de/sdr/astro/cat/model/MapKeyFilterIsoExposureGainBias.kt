package de.sdr.astro.cat.model

class MapKeyFilterIsoExposureGainBias(val iso: Int, val exposure: Double, val gain: Int, val bias : Int, val filter: String) {

    override fun equals(other: Any?): Boolean {
        return if (other is MapKeyFilterIsoExposureGainBias) {
            iso == other.iso && exposure == other.exposure && gain == other.gain && bias == other.bias && other.filter == filter
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = iso
        result = 31 * result + exposure.hashCode()
        result = 31 * result + gain
        result = 31 * result + bias
        result = 31 * result + filter.hashCode()
        return result
    }

    override fun toString(): String {
        return "MapKeyIsoExposure(iso=$iso, exposure=$exposure, gain=$gain, bias=$bias, filter=$filter)"
    }


}