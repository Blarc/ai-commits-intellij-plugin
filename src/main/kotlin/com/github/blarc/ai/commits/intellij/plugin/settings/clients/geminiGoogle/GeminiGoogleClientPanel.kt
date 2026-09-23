package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiGoogle

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class GeminiGoogleClientPanel private constructor(
    private val clientConfiguration: GeminiGoogleClientConfiguration,
    val service: GeminiGoogleClientService
) : LlmClientPanel(clientConfiguration) {

    private val tokenPasswordField = JBPasswordField()
    private val topKTextField = JBTextField()
    private val topPTextField = JBTextField()

    constructor(configuration: GeminiGoogleClientConfiguration): this(configuration, GeminiGoogleClientService.getInstance())

    override fun create() = panel {
        nameRow()
        tokenRow()
        modelIdRow()
        temperatureRow(clientConfiguration::temperature.toMutableProperty())
        topKRow(topKTextField, clientConfiguration::topK.toNullableProperty())
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        cleanUpRegexRow()
        verifyRow()
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.geminiGoogle.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    GeminiGoogleClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message("settings.geminiGoogle.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.geminiGoogle.token.comment"), 50)
        }
    }


    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()
        clientConfiguration.topK = topKTextField.text.toIntOrNull()
        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
