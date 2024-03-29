package com.github.blarc.ai.commits.intellij.plugin.settings

import com.aallam.openai.api.model.ModelId
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClient
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

            is ModelId -> {
                text = value.id
            }

            is LLMClient -> {
                text = value.displayName
            }
        }
        return component
    }
}
