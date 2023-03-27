package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.*
import java.awt.Component
import java.util.*
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class AppSettingsConfigurable : BoundConfigurable(message("settings.general.group.title")) {
    override fun createPanel() = panel {

        row {
            passwordField()
                .label(message("settings.openAIToken"))
                .bindText(
                    { AppSettings.instance.getOpenAIToken().orEmpty() },
                    { AppSettings.instance.saveOpenAIToken(it)}
                )
                .align(Align.FILL)
        }
        row {
            comment(message("settings.openAITokenComment"))
        }
        row {
            comboBox(Locale.getAvailableLocales().toList(), AppSettingsListCellRenderer())
                .label(message("settings.locale"))
                .bindItem(AppSettings.instance::locale.toNullableProperty())
        }
    }
}