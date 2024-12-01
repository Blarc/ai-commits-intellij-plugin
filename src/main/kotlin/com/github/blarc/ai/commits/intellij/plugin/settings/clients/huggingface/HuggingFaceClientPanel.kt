package com.github.blarc.ai.commits.intellij.plugin.settings.clients.huggingface;

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class HuggingFaceClientPanel private constructor(
    private val clientConfiguration: HuggingFaceClientConfiguration,
    val service: HuggingFaceClientService
) : LLMClientPanel(clientConfiguration) {

    private val tokenPasswordField = JBPasswordField()
    private val maxNewTokensTextField = JBTextField()
    private val waitForModelCheckBox = JBCheckBox()
    private val removePrompt = JBCheckBox()

    constructor(configuration: HuggingFaceClientConfiguration) : this(configuration, HuggingFaceClientService.getInstance())

    override fun create() = panel {
        nameRow()
        timeoutRow(clientConfiguration::timeout)
        tokenRow()
        modelIdRow()
        temperatureRow()
        maxNewTokens()
        waitForModel()
        removePrompt()
        verifyRow()
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.token = String(tokenPasswordField.password)
        clientConfiguration.maxNewTokens = maxNewTokensTextField.text.toInt()
        clientConfiguration.waitForModel = waitForModelCheckBox.isSelected
        clientConfiguration.removePrompt = removePrompt.isSelected
        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }

    private fun Panel.tokenRow() {
        row {
            label(message("settings.llmClient.token"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(getter = { "" }, setter = {
                    HuggingFaceClientService.getInstance().saveToken(clientConfiguration, it)
                })
                .emptyText(if (clientConfiguration.tokenIsStored) message("settings.llmClient.token.stored") else message("settings.huggingface.token.example"))
                .resizableColumn()
                .align(Align.FILL)
                // maxLineLength was eye-balled, but prevents the dialog getting wider
                .comment(message("settings.huggingface.token.comment"), 50)
        }
    }

    private fun Panel.maxNewTokens() {
        row {
            label(message("settings.huggingface.maxNewTokens"))
                .widthGroup("label")
            cell(maxNewTokensTextField)
                .bindIntText(clientConfiguration::maxNewTokens)
                .resizableColumn()
                .align(Align.FILL)
        }
    }

    private fun Panel.waitForModel() {
        row {
            label(message("settings.huggingface.waitForModel"))
                .widthGroup("label")
            cell(waitForModelCheckBox)
                .bindSelected(clientConfiguration::waitForModel)
                .resizableColumn()
                .align(Align.FILL)

            contextHelp(message("settings.huggingface.waitModel.comment"))
                .align(AlignX.RIGHT)
        }
    }

    private fun Panel.removePrompt() {
        row {
            label(message("settings.huggingface.removePrompt"))
                .widthGroup("label")
            cell(removePrompt)
                .bindSelected(clientConfiguration::removePrompt)
                .resizableColumn()
                .align(Align.FILL)

            contextHelp(message("settings.huggingface.removePrompt.comment"))
                .align(AlignX.RIGHT)
        }
    }
}
