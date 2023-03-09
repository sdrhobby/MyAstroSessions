package de.sdr.astro.cat.gui

import java.util.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode


/**
 * credits to: http://www.java2s.com/Code/Java/Swing-Components/InvisibleNodeTreeExample.htm
 */
open class InvisibleNode @JvmOverloads constructor(
    userObject: Any? = null, allowsChildren: Boolean = true,
    var isVisible: Boolean = true,
) :
    DefaultMutableTreeNode(userObject, allowsChildren) {

    fun getChildAt(index: Int, filterIsActive: Boolean): TreeNode {
        if (!filterIsActive) {
            return super.getChildAt(index)
        }
        if (children == null) {
            throw ArrayIndexOutOfBoundsException("node has no children")
        }
        var realIndex = -1
        var visibleIndex = -1
        val e: Enumeration<*> = children.elements()
        while (e.hasMoreElements()) {
            val node = e.nextElement() as InvisibleNode
            if (node.isVisible) {
                visibleIndex++
            }
            realIndex++
            if (visibleIndex == index) {
                return children.elementAt(realIndex) as TreeNode
            }
        }
        throw ArrayIndexOutOfBoundsException("index unmatched")
        //return (TreeNode)children.elementAt(index);
    }

    fun getChildCount(filterIsActive: Boolean): Int {
        if (!filterIsActive) {
            return super.getChildCount()
        }
        if (children == null) {
            return 0
        }
        var count = 0
        val e: Enumeration<*> = children.elements()
        while (e.hasMoreElements()) {
            val node = e.nextElement() as InvisibleNode
            if (node.isVisible) {
                count++
            }
        }
        return count
    }
}
