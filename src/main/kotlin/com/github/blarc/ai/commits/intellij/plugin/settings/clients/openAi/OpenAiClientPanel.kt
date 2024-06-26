package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNonNullableProperty

class OpenAiClientPanel(private val clientConfiguration: OpenAiClientConfiguration) : LLMClientPanel(clientConfiguration) {

    private val tokenPasswordField = JBPasswordField()

    override fun create() = panel {
        nameRow()
        hostRow()
        proxyRow()
        timeoutRow()

        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = {""}, setter = {
                    OpenAiClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.openAI.token.stored") else message("settings.openAI.token.example"))
                .resizableColumn()
                .widthGroup("input")
        }
        row {
            comment(message("settings.openAi.token.comment"))
                .align(AlignX.LEFT)
        }

        modelIdRow()

        row {
            label(message("settings.openAi.organizationId"))
                .widthGroup("label")
            textField()
                .bindText(clientConfiguration::organizationId.toNonNullableProperty(""))
                .widthGroup("input")
        }

        temperatureRow()
        verifyRow()

    }

    override fun verifyConfiguration() {

        clientConfiguration.host = hostComboBox.item
        clientConfiguration.proxyUrl = proxyTextField.text
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)

        OpenAiClientService.getInstance().verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
