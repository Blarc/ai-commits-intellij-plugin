package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.aallam.openai.api.exception.OpenAIAPIException
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.emptyText
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.temperatureValid
import com.intellij.icons.AllIcons
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.naturalSorted
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumWidth
import kotlinx.coroutines.*
import javax.swing.DefaultComboBoxModel

class OpenAIClientPanel(private val client: OpenAIClient) : LLMClientPanel {

    private val hostComboBox =  ComboBox(client.getHosts().toTypedArray())
    private val tokenPasswordField = JBPasswordField()
    private val verifyLabel = JBLabel()
    private val proxyTextField = JBTextField()
    private val socketTimeoutTextField = JBTextField()
    private var modelComboBox = ComboBox<String>()

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
                .bindText(client::proxyUrl.toNonNullableProperty(""))
                .applyToComponent { minimumWidth = 400 }
                .resizableColumn()
                .widthGroup("input")
        }
        row {
            comment(message("settings.openAIProxyComment"))
        }
        row {
            label(message("settings.openAISocketTimeout")).widthGroup("label")
            cell(socketTimeoutTextField)
                .bindIntText(client::timeout)
                .applyToComponent { minimumWidth = 400 }
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
            cell(verifyLabel)
                .align(AlignX.RIGHT)
        }
        row {
            label(message("settings.openAIModel"))
                .widthGroup("label")

            cell(modelComboBox)
                .bindItem({ client.modelId }, {
                    if (it != null) {
                        client.modelId = it
                    }
                })
                .widthGroup("input")
                .resizableColumn()
            button(message("settings.refreshModels")) {
                runBackgroundableTask(message("settings.loadingModels")) {
                    runBlocking(Dispatchers.IO) {
                        client.refreshModels()
                        modelComboBox.model = DefaultComboBoxModel(client.getModelIds().naturalSorted().toTypedArray())
                        modelComboBox.item = client.modelId
                    }
                }
            }
                .align(AlignX.RIGHT)
                .widthGroup("button")
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
                        client.verifyConfiguration(hostComboBox.item, proxyTextField.text, socketTimeoutTextField.text, String(tokenPasswordField.password))
                        verifyLabel.text = message("settings.verify.valid")
                        verifyLabel.icon = AllIcons.General.InspectionsOK
                    } catch (e: OpenAIAPIException) {
                        verifyLabel.text = message("settings.verify.invalid", e.statusCode)
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    } catch (e: NumberFormatException) {
                        verifyLabel.text = message("settings.verify.invalid", e.localizedMessage)
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    } catch (e: Exception) {
                        verifyLabel.text = message("settings.verify.invalid", "Unknown")
                        verifyLabel.icon = AllIcons.General.InspectionsError
                    }
                }
            }
        }
    }
}
