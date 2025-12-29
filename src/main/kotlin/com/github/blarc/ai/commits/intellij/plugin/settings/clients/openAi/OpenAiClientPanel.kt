package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.github.blarc.ai.commits.intellij.plugin.temperatureValidNullable
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder

class OpenAiClientPanel(private val clientConfiguration: OpenAiClientConfiguration) : LLMClientPanel(clientConfiguration) {
    private val tokenPasswordField = JBPasswordField()
    private val organizationIdTextField = JBTextField()
    private val topPTextField = JBTextField()

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty())
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        modelIdRow()
        organizationIdRow()
        temperatureRow(clientConfiguration::temperature.toMutableProperty(), ValidationInfoBuilder::temperatureValidNullable)
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        verifyRow()

    }

    override fun verifyConfiguration() {

        clientConfiguration.host = hostComboBox.item
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.organizationId = organizationIdTextField.text
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()

        OpenAiClientService.getInstance().verifyConfiguration(clientConfiguration, verifyLabel)
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    OpenAiClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message("settings.openAI.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.openAi.token.comment"), 50)
        }
    }

    private fun Panel.organizationIdRow() {
        row {
            label(message("settings.openAi.organizationId"))
                .widthGroup("label")
            cell(organizationIdTextField)
                .bindText(clientConfiguration::organizationId.toNonNullableProperty(""))
                .align(Align.FILL)
                .resizableColumn()
        }
    }
}
