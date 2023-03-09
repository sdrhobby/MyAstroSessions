package de.sdr.astro.cat.config

import de.sdr.astro.cat.model.OverlayInfo
import de.sdr.astro.cat.model.Session
import java.awt.Dimension
import java.awt.Point
import java.io.*
import java.util.*
import javax.swing.JOptionPane

class Config private constructor() {

    var l10n: ResourceBundle = ResourceBundle.getBundle("l10n")

    private val configFileFolder =
        File(System.getProperty("user.home") + File.separator + ".config" + File.separator + "myastrosessions")
    private val configFileName = configFileFolder.path + File.separator + "myastrosessions.cfg"
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
    var locale = ""

    val unknownConfigLines: MutableList<String> = mutableListOf()

    companion object {
        private var instance: Config? = null

        @JvmStatic
        fun getInstance(): Config {
            if (instance == null) {
                instance = Config()
                instance?.checkReadmeTemplate()
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
        fw.write("# Gui language setting" + System.lineSeparator())
        fw.write("locale=${locale}" + System.lineSeparator())

        fw.write("# last size of main window" + System.lineSeparator())
        fw.write("windowlocation=${locationOnScreen.x},${locationOnScreen.y} " + System.lineSeparator())
        fw.write("windowsize=${frameSize.width}x${frameSize.height} " + System.lineSeparator())

        fw.write("# default Astro-image folder" + System.lineSeparator())
        fw.write("folder=" + getInstance().scanFolder + System.lineSeparator())
        fw.write("autoscan=$autoscan" + System.lineSeparator())

        fw.write(System.lineSeparator())
        fw.write("# Telescope configurations" + System.lineSeparator())
        fw.write("# ID; Name; FocalLength; Aperture; Keywords" + System.lineSeparator())
        for (telescope in getInstance().telescopes) {
            fw.write(telescope.toConfigLine() + "" + System.lineSeparator())
        }
        fw.write(System.lineSeparator())
        fw.write("# Camera configurations" + System.lineSeparator())
        fw.write("# ID; Name; xRes; yRes; xPixel-Size; yPixel-Size; Keywords" + System.lineSeparator())
        for (camera in getInstance().cameras) {
            fw.write(camera.toConfigLine() + "" + System.lineSeparator())
        }

        fw.write(System.lineSeparator())
        fw.write("# Mounts configurations" + System.lineSeparator())
        for (mount in getInstance().mounts) {
            fw.write(mount.toConfigLine() + "" + System.lineSeparator())
        }

        fw.write(System.lineSeparator())
        fw.write("# Profiles configurations" + System.lineSeparator())
        for (profile in getInstance().profiles) {
            fw.write(profile.toConfigLine() + "" + System.lineSeparator())
        }

        fw.write(System.lineSeparator())
        fw.write("# skymap overlay coordinates for AstroObjects" + System.lineSeparator())
        for (overlayInfo in getInstance().overlayInfos) {
            fw.write(overlayInfo.toConfigLine() + "" + System.lineSeparator())
        }

        fw.write(System.lineSeparator())
        // write "unknown" lines that have been read from orig config-file during startup
        fw.write("# un-interpreted lines, copied from original config file" + System.lineSeparator())
        fw.write("# maybe written by another version of this software" + System.lineSeparator())
        for (line in unknownConfigLines) {
            fw.write(line + "" + System.lineSeparator())
        }

        fw.flush()
        fw.close()
        println("Configuration file $configFileName updated!")
    }

    fun readConfigFile() {
        try {
            val fr = FileReader(getInstance().configFileName)
            for (line in fr.readLines()) {
                if (line.isEmpty() || line.startsWith('#'))
                    continue
                if (line.startsWith("locale")) {
                    locale = line.substring(line.indexOf('=') + 1).trim()
                } else if (line.startsWith("windowlocation")) {
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
        } catch (x: FileNotFoundException) {
            System.err.println(
                String.format(
                    "No config file found in %s. Maybe first start?",
                    getInstance().configFileName
                )
            );
        }
    }

    fun readReadmeTemplate(): String {
        val sb = StringBuffer()
        val reader = FileReader(readmeTemplateName)
        reader.forEachLine {
            sb.append(it)
            sb.append(System.lineSeparator())
        }
        return sb.toString()
    }

    fun saveSessionProfile(session: Session, sessionProfile: Profile) {
        val fileName = session.path + File.separator + ".myastro.session"
        val fw = FileWriter(fileName)
        fw.write(sessionProfile.toConfigLine() + System.lineSeparator())
        fw.flush()
        fw.close()
        println("Configuration file $fileName updated!")
    }

    fun readSessionProfile(session: Session): Profile? {
        val fileName = session.path + File.separator + ".myastro.session"
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


    fun switchLocale(loc: String, showDialog: Boolean) {
        if (showDialog) {
            val answer = JOptionPane.showMessageDialog(
                null, String.format(getInstance().l10n.getString("config_language.confirm.msg")),
                getInstance().l10n.getString("config_language.confirm.title"),
                JOptionPane.INFORMATION_MESSAGE
            )
        }
        this.locale = loc
        l10n = if (loc.isEmpty()) {
            ResourceBundle.getBundle("l10n")
        } else {
            val newLocale = Locale(loc, loc.uppercase())
            Locale.setDefault(newLocale)
            ResourceBundle.getBundle("l10n", newLocale)
        }
    }

    // TODO: use localized versions of README.template for that!
    fun checkReadmeTemplate() {
        try {
            val readmeTemplateFile = File(configFileFolder.path + File.separator + "README.template")
            if (!readmeTemplateFile.exists()) {
                val input = InputStreamReader(this.javaClass.getResourceAsStream("/README.template"))
                val output = FileWriter(configFileFolder.path + File.separator + "README.template")
                for (line in input.readLines()) {
                    output.write(line + System.lineSeparator())
                }
                output.flush()
                output.close()
                input.close()
            }
        } catch (x: Exception) {
            System.err.println("Error while creating README.template in " + configFileFolder)
        }

    }
}




