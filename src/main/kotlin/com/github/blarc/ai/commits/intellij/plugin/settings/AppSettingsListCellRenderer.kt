package com.github.blarc.ai.commits.intellij.plugin.settings

import java.awt.Component
import java.util.*
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class AppSettingsListCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        if (value is Locale) {
            text = value.displayName
        }
        return component
    }
}