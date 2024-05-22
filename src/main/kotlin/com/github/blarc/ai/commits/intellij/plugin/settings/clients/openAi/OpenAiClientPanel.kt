package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class OpenAiClientPanel(private val client: OpenAiClientConfiguration) : LLMClientPanel(client) {

    private val tokenPasswordField = JBPasswordField()

    override fun create() = panel {
        hostRow()
        proxyRow()
        timeoutRow()

        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(client::token)
                .emptyText(message("settings.openAI.token.example"))
                .resizableColumn()
                .focused()
                .widthGroup("input")
        }
        row {
            comment(message("settings.openAi.token.comment"))
                .align(AlignX.LEFT)
        }

        modelIdRow()
        temperatureRow()
        verifyRow()

    }

    override fun verifyConfiguration() {

        val newConfiguration = OpenAiClientConfiguration()
        newConfiguration.host = hostComboBox.item
        newConfiguration.proxyUrl = proxyTextField.text
        newConfiguration.timeout = socketTimeoutTextField.text.toInt()
        newConfiguration.modelId = modelComboBox.item
        newConfiguration.temperature = temperatureTextField.text
        newConfiguration.token = String(tokenPasswordField.password)

        OpenAiClientService.getInstance().verifyConfiguration(newConfiguration, verifyLabel)
    }
}
