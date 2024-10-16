package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openrouter

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.*

class OpenRouterClientPanel(private val clientConfiguration: OpenRouterClientConfiguration) : LLMClientPanel(clientConfiguration) {
    private val tokenPasswordField = JBPasswordField()

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty())
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        modelIdRow()
        temperatureRow()
        verifyRow()
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    OpenRouterClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message("settings.openRouter.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                .comment(message("settings.openRouter.token.comment"), 50)
        }
    }

    override fun verifyConfiguration() {
        clientConfiguration.host = hostComboBox.item
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)

        OpenRouterClientService.getInstance().verifyConfiguration(clientConfiguration, verifyLabel)
    }
}