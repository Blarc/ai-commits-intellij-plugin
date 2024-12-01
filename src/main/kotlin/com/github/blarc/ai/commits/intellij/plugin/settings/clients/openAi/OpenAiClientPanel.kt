package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class OpenAiClientPanel(private val clientConfiguration: OpenAiClientConfiguration) : LLMClientPanel(clientConfiguration) {
    private val proxyTextField = JBTextField()
    private val tokenPasswordField = JBPasswordField()
    private val organizationIdTextField = JBTextField()
    private val topPTextField = JBTextField()

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty())
        proxyRow()
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        modelIdRow()
        organizationIdRow()
        temperatureRow()
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        verifyRow()

    }

    override fun verifyConfiguration() {

        clientConfiguration.host = hostComboBox.item
        clientConfiguration.proxyUrl = proxyTextField.text
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.organizationId = organizationIdTextField.text
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()

        OpenAiClientService.getInstance().verifyConfiguration(clientConfiguration, verifyLabel)
    }

    private fun Panel.proxyRow() {
        row {
            label(message("settings.llmClient.proxy"))
                .widthGroup("label")
            cell(proxyTextField)
                .bindText(clientConfiguration::proxyUrl.toNonNullableProperty(""))
                .resizableColumn()
                .align(Align.FILL)
                .comment(message("settings.llmClient.proxy.comment"))
        }
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
