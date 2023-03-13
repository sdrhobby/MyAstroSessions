package de.sdr.astro.cat.gui

import de.sdr.astro.cat.model.PathObject

class FilterTreeNode(userObject: String, type: String, nodeObject: PathObject?, allowsChildren: Boolean = true, val realPath : String) :
    PathObjectTreeNode(userObject, type, nodeObject, allowsChildren) {
}