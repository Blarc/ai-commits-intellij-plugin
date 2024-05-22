package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel

class OllamaClientPanel(client: LLMClientConfiguration) : LLMClientPanel(client) {
    override fun verifyConfiguration() {

        val newConfiguration = OllamaClientConfiguration()
        newConfiguration.host = hostComboBox.item
        newConfiguration.timeout = socketTimeoutTextField.text.toInt()
        newConfiguration.modelId = modelComboBox.item
        newConfiguration.temperature = temperatureTextField.text

        OllamaClientService.getInstance().verifyConfiguration(newConfiguration, verifyLabel)
    }
}
