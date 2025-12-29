package com.github.blarc.ai.commits.intellij.plugin.settings.clients.mistral;

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class MistralAIClientPanel private constructor(
    private val clientConfiguration: MistralAIClientConfiguration,
    val service: MistralAIClientService
) : LLMClientPanel(clientConfiguration) {

    private val tokenPasswordField = JBPasswordField()
    private val topPTextField = JBTextField()
    private val maxTokensTextField = JBTextField()

    constructor(configuration: MistralAIClientConfiguration) : this(configuration, MistralAIClientService.getInstance())

    override fun create() = panel {
        nameRow()
        modelIdRow()
        tokenRow()
        maxTokens()
        temperatureRow(clientConfiguration::temperature.toMutableProperty())
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        verifyRow()
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()
        clientConfiguration.maxTokens = maxTokensTextField.text.toIntOrNull()
        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }

    override fun getRefreshModelsFunction() = fun () {
        service.refreshModels(clientConfiguration, modelComboBox, verifyLabel)
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    MistralAIClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message("settings.openAI.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.mistral.token.comment"), 50)
        }
    }

    private fun Panel.maxTokens() {
        row {
            label(message("settings.mistral.maxTokens"))
                .widthGroup("label")
            cell(maxTokensTextField)
                .bindText({ clientConfiguration.maxTokens?.toString() ?: "" }, { s -> clientConfiguration::maxTokens.set(s.toInt()) })
                .align(Align.FILL)
                .validationOnInput { isInt(it.text) }
                .resizableColumn()
        }
    }
}
