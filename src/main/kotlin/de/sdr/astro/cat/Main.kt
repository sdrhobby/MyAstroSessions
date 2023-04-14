package de.sdr.astro.cat

import com.github.weisj.darklaf.LafManager
import com.github.weisj.darklaf.theme.DarculaTheme
import com.github.weisj.darklaf.theme.IntelliJTheme
import de.sdr.astro.cat.config.Config
import de.sdr.astro.cat.gui.MainFrame


fun main(args: Array<String>) {
    // initialize configurations
    Config.getInstance()
    Config.getInstance().switchLocale( Config.getInstance().locale, false )

    if ( Config.getInstance().theme == "dark" )
        LafManager.install(DarculaTheme())
    else
        LafManager.install(IntelliJTheme())

    MainFrame()
}