package com.github.blarc.ai.commits.intellij.plugin.settings.clients.githubModels

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientPanel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class GitHubModelsClientPanel private constructor(
    private val clientConfiguration: GitHubModelsClientConfiguration,
    val service: GitHubModelsClientService
) : LlmClientPanel(clientConfiguration) {

    private val tokenPasswordField = JBPasswordField()
    private val topPTextField = JBTextField()

    constructor(configuration: GitHubModelsClientConfiguration) : this(configuration, GitHubModelsClientService.getInstance())

    override fun create() = panel {
        nameRow()
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        modelIdRow()
        temperatureRow(clientConfiguration::temperature.toMutableProperty())
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        cleanUpRegexRow()
        verifyRow()
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    GitHubModelsClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message("settings.githubModels.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.githubModels.token.comment"), 50)
        }
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()
        clientConfiguration.token = String(tokenPasswordField.password)

        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }
}
