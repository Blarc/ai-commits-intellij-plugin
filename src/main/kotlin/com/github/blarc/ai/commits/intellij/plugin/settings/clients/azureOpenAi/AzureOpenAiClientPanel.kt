package com.github.blarc.ai.commits.intellij.plugin.settings.clients.azureOpenAi;

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class AzureOpenAiClientPanel private constructor(
    private val clientConfiguration: AzureOpenAiClientConfiguration,
    val service: AzureOpenAiClientService
) : LLMClientPanel(clientConfiguration) {

    private val tokenPasswordField = JBPasswordField()
    private val topPTextField = JBTextField()

    constructor(configuration: AzureOpenAiClientConfiguration) : this(configuration, AzureOpenAiClientService.getInstance())

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty(), "settings.azureOpenAi.host")
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        modelIdRow("settings.azureOpenAi.modelId")
        temperatureRow(clientConfiguration::temperature.toMutableProperty())
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        verifyRow()

    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.azureOpenAi.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    AzureOpenAiClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message("settings.azureOpenAi.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.azureOpenAi.token.comment"), 50)
        }
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.host = hostComboBox.item
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()
        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
