package de.sdr.astro.cat.util

import java.util.*

/**
 * this Class implements a "Least Recently Used cache", based on a SortedMap
 */
class LRUCache(private val capacity : Int) {

    private val cache : MutableMap<String, Any> = mutableMapOf()
    private val accessTimes : MutableMap<String, Long> = mutableMapOf()

    fun add( key : String, value : Any) {
        if ( capacity > 0 && cache.size >= capacity ) {
            removeLeastRecentlyUsed()
        }
        cache[key] = value
        accessTimes[key] = Date().time
    }

    fun get(key:String) : Any? {
        if ( cache.contains(key) )
            // update access time
            accessTimes[key] = Date().time
        return cache[key]
    }

    fun clear() {
        cache.clear()
        accessTimes.clear()
    }

    private fun removeLeastRecentlyUsed() {
        // get the oldest access time for the entries in the cache
        var oldest = Long.MAX_VALUE
        var oldestKey = ""
        for( (key, time) in accessTimes ) {
            if ( time < oldest ) {
                oldest = time
                oldestKey = key
            }
        }
        println( "removing oldestKey: $oldestKey")
        cache.remove(oldestKey)
    }

}