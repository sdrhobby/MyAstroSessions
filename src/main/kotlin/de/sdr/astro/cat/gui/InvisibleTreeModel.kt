package de.sdr.astro.cat.gui

import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode


/**
 * credits to: http://www.java2s.com/Code/Java/Swing-Components/InvisibleNodeTreeExample.htm
 */
internal class InvisibleTreeModel(
    root: TreeNode?, asksAllowsChildren: Boolean,
    private var isActivatedFilter: Boolean,
) : DefaultTreeModel(root, asksAllowsChildren) {

    @JvmOverloads
    constructor(root: TreeNode?, asksAllowsChildren: Boolean = false) : this(root, false, false)

    fun activateFilter(newValue: Boolean) {
        isActivatedFilter = newValue
    }

    override fun getChild(parent: Any, index: Int): Any {
        if (isActivatedFilter) {
            if (parent is InvisibleNode) {
                return parent.getChildAt(
                    index,
                    isActivatedFilter
                )
            }
        }
        return (parent as TreeNode).getChildAt(index)
    }

    override fun getChildCount(parent: Any): Int {
        if (isActivatedFilter) {
            if (parent is InvisibleNode) {
                return parent.getChildCount(isActivatedFilter)
            }
        }
        return (parent as TreeNode).childCount
    }
}

