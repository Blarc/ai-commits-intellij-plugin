package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiApi

import GeminiApiClientService
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.notBlank
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class GeminiApiClientPanel private constructor(
    private val clientConfiguration: GeminiApiClientConfiguration,
    val service: GeminiApiClientService
) : LLMClientPanel(clientConfiguration) {

    private val projectIdTextField = JBTextField()

    constructor(configuration: GeminiApiClientConfiguration): this(configuration, GeminiApiClientService.getInstance())

    override fun create() = panel {
        nameRow()
        apiKeyRow()
        modelIdRow()
        temperatureRow()
        verifyRow()
    }

    private fun Panel.apiKeyRow() {
        row {
            label(message("settings.geminiApi.api-key"))
                .widthGroup("label")

            cell(projectIdTextField)
                .bindText(clientConfiguration::apiKey)
                .resizableColumn()
                .align(Align.FILL)
                .validationOnInput { notBlank(it.text) }
                .comment(message("settings.geminiApi.apk-kay.comment"))
        }

    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.apiKey = projectIdTextField.text
        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
