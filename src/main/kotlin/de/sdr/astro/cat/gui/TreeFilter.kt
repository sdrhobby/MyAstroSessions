package de.sdr.astro.cat.gui

import javax.swing.JTree

class TreeFilter(private val tree: JTree) {

    private var deepestLevel = -1

    fun applyFilter(filter: String) : Int {
        val needle = filter.lowercase()
        val model = tree.model as InvisibleTreeModel
        val rootNode = model.root as InvisibleNode
        if ( needle.isEmpty())
            setChildrenVisible(rootNode)

        // for now only apply filter on first level nodes
        filterChildNodes(rootNode, needle)
        model.reload()
        return deepestLevel
    }

    private fun filterChildNodes(node: InvisibleNode, needle: String) {
        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i, false) as InvisibleNode
            val nodeText = child.userObject.toString().lowercase()
            if (nodeText.indexOf(needle) >= 0) {
                child.isVisible = true
                val levels = setParentsVisible(child, 0)
                if ( levels > deepestLevel)
                    deepestLevel = levels
                setChildrenVisible(child)
            } else {
                child.isVisible = false
                // if no match at that level - look deeper and start recursion on that node
                filterChildNodes(child, needle)
            }
        }
    }

    private fun setChildrenVisible(node: InvisibleNode) {
        for (i in 0 until node.childCount) {
            val child = node.getChildAt(i, false) as InvisibleNode
            child.isVisible = true
            setChildrenVisible( child )
        }
    }

    private fun setParentsVisible(node: InvisibleNode, levels : Int) : Int {
        if (node.parent != null) {
            val parent = node.parent as InvisibleNode
            parent.isVisible = true
            return setParentsVisible( parent, levels + 1 )
        }
        return levels
    }

}