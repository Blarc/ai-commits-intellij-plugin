package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ernie
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class ErnieClientPanel(private val clientConfiguration: ErnieClientConfiguration) : LLMClientPanel(clientConfiguration) {

    private val apiKeyField = JBPasswordField()
    private val secretKeyField = JBPasswordField()

    override fun create() = panel {
        nameRow()
        hostRow()
        modelIdRow()

        row {
            label(message("settings.ernie.apiKey"))
                .widthGroup("label")
            cell(apiKeyField)
                .bindText(getter = {""}, setter = {
                    ErnieClientService.getInstance().saveApiKey(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.apiKeyIsStored) message("settings.openAI.token.stored") else "")
                .resizableColumn()
                .widthGroup("input")
        }
        row {
            label(message("settings.ernie.secretKey"))
                .widthGroup("label")
            cell(secretKeyField)
                .bindText(getter = {""}, setter = {
                    ErnieClientService.getInstance().saveSecretKey(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.secretKeyIsStored) message("settings.openAI.token.stored") else "")
                .resizableColumn()
                .widthGroup("input")
        }

        temperatureRow()
        verifyRow()
    }

    override fun verifyConfiguration() {

        clientConfiguration.host = hostComboBox.item
//        clientConfiguration.proxyUrl = proxyTextField.text
//        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.apiKey = String(apiKeyField.password)
        clientConfiguration.secretKey = String(secretKeyField.password)

        ErnieClientService.getInstance().verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
