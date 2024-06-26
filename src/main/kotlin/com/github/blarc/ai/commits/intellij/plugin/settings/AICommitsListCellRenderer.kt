package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.Prompt
import java.awt.Component
import java.util.*
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class AICommitsListCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        when (value) {
            is Locale -> {
                text = value.displayLanguage
            }

            is Prompt -> {
                text = value.name
            }

            // This is used for combo box in settings dialog
            is LLMClientConfiguration -> {
                text = value.name
                icon = value.getClientIcon()
            }
        }
        return component
    }
}
