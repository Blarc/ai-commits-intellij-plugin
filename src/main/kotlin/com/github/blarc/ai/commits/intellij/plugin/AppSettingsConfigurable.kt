package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class AppSettingsConfigurable : BoundConfigurable(message("settings.general.group.title")) {
    override fun createPanel() = panel {

        row {
            textField()
                .label(message("settings.openAIToken"))
                .bindText(
                    { AppSettings.instance.getOpenAIToken().orEmpty() },
                    { AppSettings.instance.saveOpenAIToken(it)}
                )
                .align(Align.FILL)
        }
    }
}