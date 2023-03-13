package de.sdr.astro.cat.gui

import de.sdr.astro.cat.config.Config
import de.sdr.astro.cat.forms.AstroCatGui
import de.sdr.astro.cat.forms.NewSessionPanel
import de.sdr.astro.cat.model.*
import java.awt.Desktop
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

class PopupMenuHandler(private val tree: JTree) {

    fun createPopup(node: PathObjectTreeNode, limitedFlag: Boolean): JPopupMenu? {
        var pop: JPopupMenu? = null
        val pathObject = node.nodeObject
//        if (pathObject is AstroObject && ! limitedFlag) {
        if (node.nodeType == Model.ASTRO_OBJECT && !limitedFlag) {
            val ao = pathObject as AstroObject
            pop = JPopupMenu()
            val itemNewFolder = JMenuItem(Config.getInstance().l10n.getString("popup.subfolder"))
            pop.add(itemNewFolder)
            itemNewFolder.addActionListener {
                handleNewFolder(pathObject)
            }

            if (pathObject.hasSessions()) {
                val itemSkymap = JMenuItem(Config.getInstance().l10n.getString("popup_show.in.skymap"))
                pop.add(itemSkymap)
                itemSkymap.addActionListener {
                    AstroCatGui.getInstance().showSkymapDialog(pathObject)
                }
            }

            if (!tree.isRowSelected(0)) {
                pop.add(JSeparator())
                val itemNewSession = JMenuItem(Config.getInstance().l10n.getString("popup_create.session"))
                pop.add(itemNewSession)
                itemNewSession.addActionListener {
                    handleNewSession(pathObject)
                }
            }
        } else if (node.nodeType == Model.FILTER) {
            pop = JPopupMenu()
            val itemImage = JMenuItem(Config.getInstance().l10n.getString("popup_show.filtername.check"))
            pop.add(itemImage)
            itemImage.addActionListener {
                handleRenameFilter(node)
            }
        } else if (pathObject is Image) { // can be any type of image
            pop = JPopupMenu()
            val itemImage = JMenuItem(Config.getInstance().l10n.getString("popup_show.external"))
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

    private fun handleRenameFilter(node: PathObjectTreeNode) {
        val filterFolder = (node as FilterTreeNode).realPath
        val filterName = filterFolder.substring(filterFolder.lastIndexOf(File.separator) + 1)
        val newFilterFolder = filterFolder.substring(0, filterFolder.lastIndexOf(File.separator) + 1) + "_" + filterName
        // TODO: rename filter folder
        println("renaming ${filterFolder} to ${newFilterFolder}")
        File(filterFolder).renameTo(File(newFilterFolder))

        val session = (node.nodeObject as Filter).session
        // re-read Session from filesystem
        session.readSession()
        // remove node from the trees
        AstroCatGui.getInstance().removeNodeFromTreesByNodeObject(node.nodeObject)
    }
}
