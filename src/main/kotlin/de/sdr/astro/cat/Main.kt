package de.sdr.astro.cat

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.IntelliJTheme
import de.sdr.astro.cat.config.Config
import de.sdr.astro.cat.gui.MainFrame


fun main(args: Array<String>) {
    // initialize configurations
    Config.getInstance()
    Config.getInstance().switchLocale("")

    LafManager.install(IntelliJTheme())

    MainFrame()
}