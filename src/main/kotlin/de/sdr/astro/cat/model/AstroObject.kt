package de.sdr.astro.cat.model

import de.sdr.astro.cat.util.Util
import java.io.File

class AstroObject(path: String, doScan: Boolean) : PathObject(path), Comparable<AstroObject> {

    val sessions: MutableList<Session> = mutableListOf()
    val astroObjects: MutableList<AstroObject> = mutableListOf()

    init {
        println("AstroObject: $name ---")
        if (doScan)
            File(path)
                .walk(FileWalkDirection.TOP_DOWN)
                .maxDepth(1)
                .sorted()
                .forEach {
                    val subPath = Util.removeLeadingSlash(it.path.substring(path.length))
                    if (subPath.isNotEmpty()) {
                        if (Util.startsWithDate(subPath)) {
                            sessions.add(Session(it.path))
                        } else {
                            astroObjects.add(AstroObject(it.path, true))
                        }
                    }
                }
    }

    fun hasSessions(): Boolean {
        return sessions.size > 0
    }

    override fun compareTo(other: AstroObject): Int {
        return this.name.compareTo(other.name)
    }
}