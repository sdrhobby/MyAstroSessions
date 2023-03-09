package de.sdr.astro.cat.gui

open class TypedNode(userObject: String, type: String, allowsChildren: Boolean) :
    InvisibleNode(userObject, allowsChildren, true) {

    var nodeType : String = type

}