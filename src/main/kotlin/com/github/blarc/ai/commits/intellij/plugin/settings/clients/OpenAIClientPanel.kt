package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty

class OpenAIClientPanel(private val client: OpenAIClient) : LLMClientPanel {

    private val hostComboBox = ComboBox(client.hosts.toTypedArray())

    override fun create() = panel {
        row {
            label(AICommitsBundle.message("settings.openAIHost"))
                .widthGroup("label")

            cell(hostComboBox)
                .bindItem(client::host.toNullableProperty())
                .widthGroup("input")
        }
    }
}
