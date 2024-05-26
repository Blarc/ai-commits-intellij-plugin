package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel

class OllamaClientPanel private constructor(
    configuration: OllamaClientConfiguration,
    val service: OllamaClientService
): LLMClientPanel(configuration) {

    constructor(configuration: OllamaClientConfiguration): this(configuration, OllamaClientService.getInstance())

    override fun verifyConfiguration() {

        val newConfiguration = OllamaClientConfiguration()
        newConfiguration.host = hostComboBox.item
        newConfiguration.timeout = socketTimeoutTextField.text.toInt()
        newConfiguration.modelId = modelComboBox.item
        newConfiguration.temperature = temperatureTextField.text

        service.verifyConfiguration(newConfiguration, verifyLabel)
    }
}
