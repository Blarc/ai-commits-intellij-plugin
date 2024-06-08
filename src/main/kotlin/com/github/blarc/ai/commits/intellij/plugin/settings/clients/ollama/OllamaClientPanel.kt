package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty

class OllamaClientPanel private constructor(
    private val clientConfiguration: OllamaClientConfiguration,
    val service: OllamaClientService
): LLMClientPanel(clientConfiguration) {

    constructor(configuration: OllamaClientConfiguration): this(configuration, OllamaClientService.getInstance())

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty())
        timeoutRow(clientConfiguration::timeout)
        modelIdRow()
        temperatureRow()
        verifyRow()
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.host = hostComboBox.item
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text

        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
