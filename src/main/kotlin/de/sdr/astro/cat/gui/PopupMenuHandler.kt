package de.sdr.astro.cat.gui

import de.sdr.astro.cat.config.Config
import de.sdr.astro.cat.forms.AstroCatGui
import de.sdr.astro.cat.forms.NewSessionPanel
import de.sdr.astro.cat.model.AstroObject
import de.sdr.astro.cat.model.Image
import de.sdr.astro.cat.model.PathObject
import de.sdr.astro.cat.model.Session
import java.awt.Desktop
import java.io.File
import javax.swing.*

class PopupMenuHandler(private val tree: JTree) {

    fun createPopup(pathObject: PathObject, limitedFlag : Boolean): JPopupMenu? {
        var pop: JPopupMenu? = null
        if (pathObject is AstroObject && ! limitedFlag) {
            pop = JPopupMenu()
            val itemNewFolder = JMenuItem(Config.getInstance().l10n.getString("popup.subfolder"))
            itemNewFolder.setMnemonic('U')
            pop.add(itemNewFolder)
            itemNewFolder.addActionListener {
                handleNewFolder(pathObject)
            }

            if ( pathObject.hasSessions() ) {
                val itemSkymap = JMenuItem(Config.getInstance().l10n.getString("popup_show.in.skymap"))
                itemSkymap.setMnemonic('M')
                pop.add(itemSkymap)
                itemSkymap.addActionListener {
                    AstroCatGui.getInstance().showSkymapDialog(pathObject)
                }
            }

            if (!tree.isRowSelected(0)) {
                pop.add( JSeparator() )
                val itemNewSession = JMenuItem(Config.getInstance().l10n.getString("popup_create.session"))
                itemNewSession.setMnemonic('S')
                pop.add(itemNewSession)
                itemNewSession.addActionListener {
                    handleNewSession(pathObject)
                }
            }
        } else if (pathObject is Session) {
            pop = null
        } else if (pathObject is Image) {
            pop = JPopupMenu()
            val itemImage = JMenuItem(Config.getInstance().l10n.getString("popup_show.external"))
            itemImage.setMnemonic('V')
            pop.add(itemImage)
            itemImage.addActionListener {
                handleExternalViewer(pathObject)
            }
        }
        return pop
    }

    private fun handleNewSession(parentObject: PathObject) {
        println("creating new Session under path: ${parentObject.path}")
        val newSessionDialog = JDialog(AstroCatGui.getInstance().mainFrame)
        newSessionDialog.title = Config.getInstance().l10n.getString("popup_confirm.new.session")
        newSessionDialog.contentPane = NewSessionPanel(parentObject).topPanel
        newSessionDialog.pack()
        newSessionDialog.setLocationRelativeTo(null)
        newSessionDialog.isVisible = true
    }

    private fun handleNewFolder(pathObject: PathObject) {
        println("creating new Folder under path: ${pathObject.path}")
    }

    private fun handleExternalViewer(pathObject: PathObject) {
        println("opening image in external viewer: ${pathObject.path}")
        Desktop.getDesktop().open(File(pathObject.path))
    }
}
