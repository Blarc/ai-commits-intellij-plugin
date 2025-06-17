package com.github.blarc.ai.commits.intellij.plugin.settings.clients.anthropic;

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class AnthropicClientPanel private constructor(
    private val clientConfiguration: AnthropicClientConfiguration,
    val service: AnthropicClientService
) : LLMClientPanel(clientConfiguration) {
    private val tokenPasswordField = JBPasswordField()
    private val versionTextField = JBTextField()
    private val betaTextField = JBTextField()
    private val topPTextField = JBTextField()
    private val topKTextField = JBTextField()

    constructor(configuration: AnthropicClientConfiguration) : this(configuration, AnthropicClientService.getInstance())

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty())
        modelIdRow()
        temperatureRow()
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        versionRow()
        betaRow()
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        topKRow(topKTextField, clientConfiguration::topK.toNullableProperty())
        verifyRow()
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    AnthropicClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message("settings.anthropic.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.anthropic.token.comment"), 50)
        }
    }

    private fun Panel.versionRow() {
        row {
            label(message("settings.anthropic.version"))
                .widthGroup("label")
            cell(versionTextField)
                .bindText(clientConfiguration::version.toNonNullableProperty(""))
                .resizableColumn()
                .align(Align.FILL)
                .comment(message("settings.anthropic.version.comment"), 50)
        }
    }

    private fun Panel.betaRow() {
        row {
            label(message("settings.anthropic.beta"))
                .widthGroup("label")
            cell(betaTextField)
                .bindText(clientConfiguration::beta.toNonNullableProperty(""))
                .resizableColumn()
                .align(Align.FILL)
                .comment(message("settings.anthropic.beta.comment"), 50)
        }
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.host = hostComboBox.item
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.version = versionTextField.text
        clientConfiguration.beta = betaTextField.text
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()
        clientConfiguration.topK = topKTextField.text.toIntOrNull()
        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
