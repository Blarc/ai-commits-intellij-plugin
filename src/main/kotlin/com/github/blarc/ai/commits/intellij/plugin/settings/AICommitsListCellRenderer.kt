package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientConfiguration
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
                val ideLocale = AICommitsUtils.getIDELocale()
                // lang format: "Language name in IDE's locale + (ISO 639 lang code)"; example for Spanish in IDE "francés (fr)", for IDE in German "Französisch (fr)"
                text = "${Locale.forLanguageTag(value.language).getDisplayLanguage(ideLocale)} (${value.language})"
            }

            is Prompt -> {
                text = value.name
            }

            // This is used for combo box in settings dialog
            is LlmClientConfiguration -> {
                text = value.name
                icon = value.getClientIcon()
            }
        }
        return component
    }
}
