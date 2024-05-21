package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.github.blarc.ai.commits.intellij.plugin.temperatureValid
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumWidth
import dev.ai4j.openai4j.OpenAiHttpException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class OpenAiClientPanel(private val client: OpenAiClient) : LLMClientPanel {

    private val hostComboBox = ComboBox(client.getHosts().toTypedArray())
    private val tokenPasswordField = JBPasswordField()
    private val verifyLabel = JBLabel()
    private val proxyTextField = JBTextField()
    private val socketTimeoutTextField = JBTextField()
    private var modelComboBox = ComboBox(client.getModelIds().toTypedArray())

    override fun create() = panel {
        row {
            label(message("settings.openAIHost"))
                .widthGroup("label")
            cell(hostComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem(client::host.toNullableProperty())
                .widthGroup("input")
        }
        row {
            label(message("settings.openAIProxy"))
                .widthGroup("label")
            cell(proxyTextField)
                .applyToComponent { minimumWidth = 400 }
                .bindText(client::proxyUrl.toNonNullableProperty(""))
                .resizableColumn()
                .widthGroup("input")
        }
        row {
            comment(message("settings.openAIProxyComment"))
        }
        row {
            label(message("settings.openAISocketTimeout")).widthGroup("label")
            cell(socketTimeoutTextField)
                .applyToComponent { minimumWidth = 400 }
                .bindIntText(client::timeout)
                .resizableColumn()
                .widthGroup("input")
                .validationOnInput { isInt(it.text) }
        }
        row {
            label(message("settings.openAIToken"))
                .widthGroup("label")
            cell(tokenPasswordField)
                .bindText(client::token)
                .emptyText(message("settings.openAITokenExample"))
                .resizableColumn()
                .focused()
                .widthGroup("input")
            button(message("settings.verifyToken")) { verifyToken() }
                .align(AlignX.RIGHT)
                .widthGroup("button")
        }
        row {
            comment(message("settings.openAITokenComment"))
                .align(AlignX.LEFT)
        }
        row {
            label(message("settings.openAIModel"))
                .widthGroup("label")

            cell(modelComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem({ client.modelId }, {
                    if (it != null) {
                        client.modelId = it
                    }
                })
                .widthGroup("input")
                .resizableColumn()
                .onApply { service<OpenAiClientService>().modelIds.add(modelComboBox.item) }
        }

        row {
            label(message("settings.openAITemperature"))
                .widthGroup("label")

            textField()
                .bindText(client::temperature)
                .applyToComponent { minimumWidth = 400 }
                .resizableColumn()
                .widthGroup("input")
                .validationOnInput { temperatureValid(it.text) }

            contextHelp(message("settings.openAITemperatureComment"))
                .resizableColumn()
                .align(AlignX.LEFT)
        }

        row {
            cell(verifyLabel)
                .applyToComponent { setAllowAutoWrapping(true) }
                .align(AlignX.RIGHT)
        }

    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun verifyToken() {
        runBackgroundableTask(message("settings.verify.running")) {
            if (tokenPasswordField.password.isEmpty()) {
                verifyLabel.icon = AllIcons.General.InspectionsError
                verifyLabel.text = message("settings.verify.token-is-empty")
            } else {
                verifyLabel.icon = AllIcons.General.InlineRefreshHover
                verifyLabel.text = message("settings.verify.running")

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        client.verifyConfiguration(hostComboBox.item, proxyTextField.text, socketTimeoutTextField.text, modelComboBox.item, String(tokenPasswordField.password))
                        verifyLabel.text = message("settings.verify.valid")
                        verifyLabel.icon = AllIcons.General.InspectionsOK
                    } catch (e: Exception) {
                        var errorMessage = e.localizedMessage
                        if (e.cause is OpenAiHttpException) {
                            val openAiError = Json.decodeFromString<OpenAiErrorWrapper>((e.cause as OpenAiHttpException).message!!).error
                            errorMessage = openAiError.code
                        }
                        verifyLabel.text = message("settings.verify.invalid", errorMessage)
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    }
                }
            }
        }
    }

    @Serializable
    data class OpenAiError(val message: String?, val type: String?, val param: String?, val code: String?)

    @Serializable
    data class OpenAiErrorWrapper(val error: OpenAiError)
}
