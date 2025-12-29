package com.github.blarc.ai.commits.intellij.plugin.settings.clients.qianfan

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class QianfanClientPanel(private val clientConfiguration: QianfanClientConfiguration) : LLMClientPanel(clientConfiguration) {

    private val apiKeyField = JBPasswordField()
    private val secretKeyField = JBPasswordField()
    private val topPTextField = JBTextField()

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty())
        modelIdRow()

        row {
            label(message("settings.qianfan.apiKey"))
                .widthGroup("label")
            cell(apiKeyField)
                .bindText(getter = { "" }, setter = {
                    QianfanClientService.getInstance().saveApiKey(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.apiKeyIsStored) message("settings.llmClient.token.stored") else "JzRxxxxxxxxxxxxxxxxxxxxx")
                .resizableColumn()
                .align(Align.FILL)
        }
        row {
            label(message("settings.qianfan.secretKey"))
                .widthGroup("label")
            cell(secretKeyField)
                .bindText(getter = { "" }, setter = {
                    QianfanClientService.getInstance().saveSecretKey(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.secretKeyIsStored) message("settings.llmClient.token.stored") else "kSlxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
                .resizableColumn()
                .align(Align.FILL)
        }

        temperatureRow(clientConfiguration::temperature.toMutableProperty())
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        verifyRow()
    }

    override fun verifyConfiguration() {

        clientConfiguration.host = hostComboBox.item
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.apiKey = String(apiKeyField.password)
        clientConfiguration.secretKey = String(secretKeyField.password)
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()

        QianfanClientService.getInstance().verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
