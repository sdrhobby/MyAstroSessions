package de.sdr.astro.cat.gui

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import com.github.weisj.darklaf.theme.IntelliJTheme
import de.sdr.astro.cat.config.Config
import de.sdr.astro.cat.config.Config.Companion.getInstance
import de.sdr.astro.cat.forms.AstroCatGui
import de.sdr.astro.cat.forms.ConfigPanel
import de.sdr.astro.cat.forms.ImageExportPanel
import java.awt.Color
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.*
import javax.swing.plaf.FontUIResource
import kotlin.system.exitProcess


class MainFrame : JFrame() {

    init {

        SwingUtilities.invokeLater {
            initGlobalUISettings()

            title = "MyAstroSessions v" + getInstance().l10n.getString("VERSION") + " (by sdr)"
            preferredSize = Config.getInstance().frameSize
            contentPane = AstroCatGui.getInstance().topPanelAstroCatGui
            AstroCatGui.getInstance().mainFrame = this
            defaultCloseOperation = EXIT_ON_CLOSE

            jMenuBar = createMenu()

            pack()
            // this forces the main Frame to appear centered on screen
            val l = Config.getInstance().locationOnScreen
            if (l.x == 0 && l.y == 0)
                setLocationRelativeTo(null)
            else
                setLocation(l.x, l.y)
            isVisible = true

            addWindowListener(MyWindowListener())
        }
    }

    private fun initGlobalUISettings() {
        UIManager.put("ComboBox.disabledBackground", Color(212, 212, 210))
        UIManager.put("ComboBox.disabledForeground", Color.BLACK)

        UIManager.getDefaults().keys.forEach {
            val fr = UIManager.get(it)
            if (fr != null && fr is FontUIResource) {
                val f = FontUIResource(fr.family, fr.style, 14)
                UIManager.put(it, f)
            }
        }
    }


    private fun createMenu(): JMenuBar {
        val menuBar = JMenuBar()

        val menuFile = JMenu(Config.getInstance().l10n.getString("menu_file"))

        val menuItemEquipment = JMenuItem(Config.getInstance().l10n.getString("menu_my.equipment"))
        menuItemEquipment.addActionListener {
            showEquipmentDialog()
        }

        val menuItemSkymap = JMenuItem(Config.getInstance().l10n.getString("menu_show.skymap"))
        menuItemSkymap.addActionListener {
            AstroCatGui.getInstance().showSkymapDialog(null)
        }

        val menuItemImageExporter = JMenuItem(Config.getInstance().l10n.getString("menu_show.imageexporter"))
        menuItemImageExporter.addActionListener {
            AstroCatGui.getInstance().showImageExporterDialog()
        }

        val menuItemExit = JMenuItem(Config.getInstance().l10n.getString("menu_end"))
        menuItemExit.addActionListener {
            println(this.location)
            println(this.size)
            Config.getInstance().saveConfig()
            exitProcess(0)
        }

        menuFile.add(menuItemEquipment)
        menuFile.add(menuItemSkymap)
        menuFile.add(menuItemImageExporter)
        menuFile.addSeparator()
        menuFile.add(menuItemExit)

        val menuLook = JMenu(Config.getInstance().l10n.getString("menu_view"))

        val menuItemLightTheme = JMenuItem("Light")
        menuItemLightTheme.setMnemonic('L')
        menuItemLightTheme.addActionListener {
            LafManager.setTheme(IntelliJTheme())
            LafManager.install()
            LafManager.forceLafUpdate()
            Config.getInstance().theme = "light"
        }
        val menuItemDarkTheme = JMenuItem("Dark")
        menuItemDarkTheme.setMnemonic('D')
        menuItemDarkTheme.addActionListener {
            LafManager.setTheme(DarculaTheme())
            LafManager.install()
            LafManager.forceLafUpdate()
            Config.getInstance().theme = "dark"
        }

        val themesMenu = JMenu(Config.getInstance().l10n.getString("menu_themes"))
        themesMenu.add(menuItemLightTheme)
        themesMenu.add(menuItemDarkTheme)

        val menuItemLangDe = JMenuItem("Deutsch")
        menuItemLangDe.setMnemonic('D')
        menuItemLangDe.addActionListener {
            Config.getInstance().switchLocale("de", true)
        }
        if ("de".equals(Config.getInstance().locale))
            menuItemLangDe.isEnabled = false

        val menuItemLangEn = JMenuItem("English")
        menuItemLangEn.setMnemonic('E')
        menuItemLangEn.addActionListener {
            Config.getInstance().switchLocale("en", true)
        }
        if ("en".equals(Config.getInstance().locale) || Config.getInstance().locale.isEmpty())
            menuItemLangEn.isEnabled = false

        val langMenu = JMenu(Config.getInstance().l10n.getString("menu_language"))
        langMenu.add(menuItemLangDe)
        langMenu.add(menuItemLangEn)

        menuLook.add(themesMenu)
        menuLook.add(langMenu)

        menuBar.add(menuFile)
        menuBar.add(menuLook)
        return menuBar
    }

    private fun showEquipmentDialog() {
        val equipmentDialog = JDialog(this)
        equipmentDialog.title = Config.getInstance().l10n.getString("menu_equipment")
        equipmentDialog.contentPane = ConfigPanel().topPanel
        equipmentDialog.pack()
        equipmentDialog.setLocationRelativeTo(null)
        equipmentDialog.isVisible = true
    }

}

class MyWindowListener : WindowListener {
    override fun windowOpened(p0: WindowEvent?) {
    }

    override fun windowClosing(event: WindowEvent?) {
        Config.getInstance().updateGeometry(event?.window?.location!!, event.window?.size!!)
        Config.getInstance().saveConfig()
    }

    override fun windowClosed(p0: WindowEvent?) {
    }

    override fun windowIconified(p0: WindowEvent?) {
    }

    override fun windowDeiconified(p0: WindowEvent?) {
    }

    override fun windowActivated(p0: WindowEvent?) {
    }

    override fun windowDeactivated(p0: WindowEvent?) {
    }
}
