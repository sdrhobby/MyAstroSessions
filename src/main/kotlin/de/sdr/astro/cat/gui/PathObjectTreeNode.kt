package de.sdr.astro.cat.gui

import de.sdr.astro.cat.model.PathObject

class PathObjectTreeNode// own constructor, allowing a Node Type and a custom object per node
    (userObject: String, type: String, nodeObject: PathObject?, allowsChildren: Boolean = true) :
    TypedNode(userObject, type, allowsChildren) {

    lateinit var nodeObject : PathObject

    init {
        if (nodeObject != null) {
            this.nodeObject = nodeObject
        }
    }

}