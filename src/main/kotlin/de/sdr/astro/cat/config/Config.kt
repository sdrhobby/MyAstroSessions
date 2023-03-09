package de.sdr.astro.cat.config

import de.sdr.astro.cat.model.OverlayInfo
import de.sdr.astro.cat.model.Session
import java.awt.Dimension
import java.awt.Point
import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.util.*

class Config private constructor() {

    var l10n: ResourceBundle = ResourceBundle.getBundle("l10n")

    private val configFileFolder =
        File(System.getProperty("user.home") + File.separator + ".config" + File.separator + "astrocat")
    private val configFileName = configFileFolder.path + File.separator + "astrocat.cfg"
    private val readmeTemplateName = configFileFolder.path + File.separator + "README.template"
    var scanFolder = ""
    val telescopes: MutableList<Telescope> = mutableListOf()
    val cameras: MutableList<Camera> = mutableListOf()
    val mounts: MutableList<Mount> = mutableListOf()
    val profiles: MutableList<Profile> = mutableListOf()
    val overlayInfos: MutableList<OverlayInfo> = mutableListOf()
    var autoscan = false
    var locationOnScreen = Point(0, 0)
    var frameSize = Dimension(1000, 660)

    val unknownConfigLines : MutableList<String> = mutableListOf()

    companion object {
        private var instance: Config? = null
        @JvmStatic
        fun getInstance(): Config {
            if (instance == null) {
                instance = Config()
                instance?.switchLocale("en")

                instance?.readConfigFile()
            }
            return instance!!
        }
    }

    fun imageExtensions(): Array<String> {
        return arrayOf("fit", "fits", "nef", "jpg")
    }

    fun saveConfig() {
        if (!getInstance().configFileFolder.exists()) {
            println("config-folder created: " + getInstance().configFileFolder.mkdirs())
        }
        val fw = FileWriter(getInstance().configFileName)
        fw.write("# last size of main windown\n")
        fw.write("windowlocation=${locationOnScreen.x},${locationOnScreen.y} \n")
        fw.write("windowsize=${frameSize.width}x${frameSize.height} \n")

        fw.write("# default Astro-image folder\n")
        fw.write("folder=" + getInstance().scanFolder + "\n")
        fw.write("autoscan=$autoscan")

        fw.write("\n# Telescope configurations\n")
        fw.write("ID; Name; FocalLength; Aperture; Keywords\n")
        for (telescope in getInstance().telescopes) {
            fw.write(telescope.toConfigLine() + "\n")
        }
        fw.write("\n# Camera configurations\n")
        fw.write("ID; Name; xRes; yRes; xPixel-Size; yPixel-Size; Keywords\n")
        for (camera in getInstance().cameras) {
            fw.write(camera.toConfigLine() + "\n")
        }
        fw.write("\n# Mounts configurations\n")
        for (mount in getInstance().mounts) {
            fw.write(mount.toConfigLine() + "\n")
        }
        fw.write("\n# Profiles configurations\n")
        for (profile in getInstance().profiles) {
            fw.write(profile.toConfigLine() + "\n")
        }
        fw.write("\n# overlay coordinates for AstroObjects\n")
        for (overlayInfo in getInstance().overlayInfos) {
            fw.write(overlayInfo.toConfigLine() + "\n")
        }
        // write "unknown" lines that have been read from orig config-file during startup
        fw.write("\n# un-interpreted lines, copied from original config file\n")
        fw.write("\n# maybe written by a new version of this software\n")
        for (line in unknownConfigLines) {
            fw.write(line + "\n")
        }

        fw.flush()
        fw.close()
        println("Configuration file $configFileName updated!")
    }

    fun readConfigFile() {
        val fr = FileReader(getInstance().configFileName)
        for (line in fr.readLines()) {
            if (line.isEmpty() || line.startsWith('#'))
                continue
            if (line.startsWith("windowlocation")) {
                try {
                    val sub = line.substring(line.indexOf('=') + 1).trim()
                    val arr = sub.split(',')
                    locationOnScreen = Point(arr[0].toInt(), arr[1].toInt())
                } catch (e: Exception) {
                    println("invalid format for 'windowsize' in config-file: $line")
                }
            } else if (line.startsWith("windowsize")) {
                try {
                    val sub = line.substring(line.indexOf('=') + 1).trim()
                    val arr = sub.split('x')
                    frameSize = Dimension(arr[0].toInt(), arr[1].toInt())
                } catch (e: Exception) {
                    println("invalid format for 'windowsize' in config-file: $line")
                }
            } else if (line.startsWith("folder")) {
                scanFolder = line.substring(line.indexOf('=') + 1).trim()
            } else if (line.startsWith("autoscan")) {
                autoscan = (line.substring(line.indexOf('=') + 1).trim()) == "true"
            } else if (line.startsWith("telescope_")) {
                telescopes.add(Telescope.fromConfigLine(line))
            } else if (line.startsWith("camera_")) {
                cameras.add(Camera.fromConfigLine(line))
            } else if (line.startsWith("mount_")) {
                mounts.add(Mount.fromConfigLine(line))
            } else if (line.startsWith("profile_")) {
                profiles.add(Profile.fromConfigLine(line))
            } else if (line.startsWith("overlayinfo:")) {
                overlayInfos.add(OverlayInfo.fromConfigLine(line))
            } else {
                unknownConfigLines.add(line)
            }
        }
        fr.close()
    }

    fun readReadmeTemplate(): String {
        val sb = StringBuffer()
        val reader = FileReader(readmeTemplateName)
        reader.forEachLine {
            sb.append(it)
            sb.append("\n")
        }
        return sb.toString()
    }

    fun saveSessionProfile(session: Session, sessionProfile: Profile) {
        val fileName = session.path + File.separator + ".astrocat.session"
        val fw = FileWriter(fileName)
        fw.write(sessionProfile.toConfigLine() + "\n")
        fw.flush()
        fw.close()
        println("Configuration file $fileName updated!")
    }

    fun readSessionProfile(session: Session): Profile? {
        val fileName = session.path + File.separator + ".astrocat.session"
        var profile: Profile? = null
        try {
            val fr = FileReader(fileName)
            val lines = fr.readLines()
            fr.close()
            profile = Profile.fromConfigLine(lines[0])
            println("read session profile from $fileName as $profile")
        } catch (x: FileNotFoundException) {
            System.err.println("No session profile config found in $fileName")
        }
        return profile
    }

    fun updateTelescopes(newList: List<Telescope>) {
        telescopes.clear()
        telescopes.addAll(newList)
    }

    fun updateCameras(newList: List<Camera>) {
        cameras.clear()
        cameras.addAll(newList)
    }

    fun updateMounts(newList: List<Mount>) {
        mounts.clear()
        mounts.addAll(newList)
    }

    fun updateProfiles(newList: List<Profile>) {
        profiles.clear()
        profiles.addAll(newList)
    }

    fun updateGeometry(newLocation: Point, newSize: Dimension) {
        this.locationOnScreen = newLocation
        this.frameSize = newSize
    }

    fun updateOverlayInfos(newOverlayInfos: Collection<OverlayInfo>) {
        overlayInfos.clear()
        overlayInfos.addAll(newOverlayInfos)
    }

    fun switchLocale(loc: String) {
        l10n = if (loc.isEmpty()) {
            ResourceBundle.getBundle("l10n")
        } else {
            val newLocale = Locale(loc, loc.uppercase())
            Locale.setDefault(newLocale)
            ResourceBundle.getBundle("l10n", newLocale)
        }
    }
}




