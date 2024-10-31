package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiGoogle

import GeminiGoogleClientService
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel

class GeminiGoogleClientPanel private constructor(
    private val clientConfiguration: GeminiGoogleClientConfiguration,
    val service: GeminiGoogleClientService
) : LLMClientPanel(clientConfiguration) {

    private val tokenPasswordField = JBPasswordField()

    constructor(configuration: GeminiGoogleClientConfiguration): this(configuration, GeminiGoogleClientService.getInstance())

    override fun create() = panel {
        nameRow()
        tokenRow()
        modelIdRow()
        temperatureRow()
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
        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
