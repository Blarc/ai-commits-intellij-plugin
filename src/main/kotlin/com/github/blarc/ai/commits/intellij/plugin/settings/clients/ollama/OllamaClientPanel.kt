package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*

class OllamaClientPanel private constructor(
    private val clientConfiguration: OllamaClientConfiguration,
    val service: OllamaClientService
): LlmClientPanel(clientConfiguration) {

    private val topKTextField = JBTextField()
    private val topPTextField = JBTextField()
    private val numCtxTextField = JBTextField()
    private val numPredictTextField = JBTextField()

    constructor(configuration: OllamaClientConfiguration): this(configuration, OllamaClientService.getInstance())

    override fun create() = panel {
        nameRow()
        hostRow(clientConfiguration::host.toNullableProperty())
        timeoutRow(clientConfiguration::timeout)
        modelIdRow()
        temperatureRow(clientConfiguration::temperature.toMutableProperty())
        topKRow(topKTextField, clientConfiguration::topK.toNullableProperty())
        topPDoubleRow(topPTextField, clientConfiguration::topP.toNullableProperty())
        numCtxRow()
        numPredictRow()
        verifyRow()
    }

    override fun verifyConfiguration() {
        // Configuration passed to panel is already a copy of the original or a new configuration
        clientConfiguration.host = hostComboBox.item
        clientConfiguration.timeout = socketTimeoutTextField.text.toInt()
        clientConfiguration.modelId = modelComboBox.item
        clientConfiguration.temperature = temperatureTextField.text
        clientConfiguration.topP = topPTextField.text.toDoubleOrNull()
        clientConfiguration.topK = topKTextField.text.toIntOrNull()
        clientConfiguration.numCtx = numCtxTextField.text.toIntOrNull()
        clientConfiguration.numPredict = numPredictTextField.text.toIntOrNull()

        service.verifyConfiguration(clientConfiguration, verifyLabel)
    }

    override fun getRefreshModelsFunction() = fun () {
        service.refreshModels(clientConfiguration, modelComboBox, verifyLabel)
    }

    private fun Panel.numCtxRow() {
        row {
            label(message("settings.ollama.numCtx"))
                .widthGroup("label")
            cell(numCtxTextField)
                .bindText({ clientConfiguration.numCtx?.toString() ?: "" }, { s -> clientConfiguration::numCtx.set(s.toInt()) })
                .align(Align.FILL)
                .validationOnInput { isInt(it.text) }
                .resizableColumn()
            contextHelp(message("settings.ollama.numCtx.comment"))
                .align(AlignX.RIGHT)
        }
    }

    private fun Panel.numPredictRow() {
        row {
            label(message("settings.ollama.numPredict"))
                .widthGroup("label")
            cell(numPredictTextField)
                .bindText({ clientConfiguration.numPredict?.toString() ?: "" }, { s -> clientConfiguration::numPredict.set(s.toInt()) })
                .align(Align.FILL)
                .validationOnInput { isInt(it.text) }
                .resizableColumn()
            contextHelp(message("settings.ollama.numPredict.comment"))
                .align(AlignX.RIGHT)
        }
    }

}
