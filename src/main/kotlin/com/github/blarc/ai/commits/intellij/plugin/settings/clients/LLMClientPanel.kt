package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama.OllamaClientService
import com.github.blarc.ai.commits.intellij.plugin.temperatureValid
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumWidth


abstract class LLMClientPanel(private val client: LLMClientConfiguration) {

    val hostComboBox = ComboBox(client.getHosts().toTypedArray())
    val proxyTextField = JBTextField()
    val socketTimeoutTextField = JBTextField()
    val modelComboBox = ComboBox(client.getModelIds().toTypedArray())
    val temperatureTextField = JBTextField()
    val verifyLabel = JBLabel()

    open fun create() = panel {
        hostRow()
        timeoutRow()
        modelIdRow()
        temperatureRow()
        verifyRow()
    }

    open fun Panel.hostRow() {
        row {
            label(message("settings.llmClient.host"))
                .widthGroup("label")
            cell(hostComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem(client::host.toNullableProperty())
                .widthGroup("input")
        }
    }

    open fun Panel.proxyRow() {
        row {
            label(message("settings.llmClient.proxy"))
                .widthGroup("label")
            cell(proxyTextField)
                .applyToComponent { minimumWidth = 400 }
                .bindText(client::proxyUrl.toNonNullableProperty(""))
                .resizableColumn()
                .widthGroup("input")
        }
        row {
            comment(message("settings.llmClient.proxy.comment"))
        }
    }

    open fun Panel.timeoutRow() {
        row {
            label(message("settings.llmClient.timeout")).widthGroup("label")
            cell(socketTimeoutTextField)
                .applyToComponent { minimumWidth = 400 }
                .bindIntText(client::timeout)
                .resizableColumn()
                .widthGroup("input")
                .validationOnInput { isInt(it.text) }
        }
    }

    open fun Panel.modelIdRow() {
        row {
            label(message("settings.llmClient.modelId"))
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
                .onApply { service<OllamaClientService>().modelIds.add(modelComboBox.item) }

            client.getRefreshModelsFunction()?.let { f ->
                button(message("settings.refreshModels")) {
                    f.invoke(modelComboBox)
                }
                    .align(AlignX.RIGHT)
                    .widthGroup("button")
            }
        }
    }

    open fun Panel.temperatureRow() {
        row {
            label(message("settings.llmClient.temperature"))
                .widthGroup("label")

            cell(temperatureTextField)
                .bindText(client::temperature)
                .applyToComponent { minimumWidth = 400 }
                .resizableColumn()
                .widthGroup("input")
                .validationOnInput { temperatureValid(it.text) }

            contextHelp(message("settings.llmClient.temperature.comment"))
                .resizableColumn()
                .align(AlignX.LEFT)
        }
    }

    open fun Panel.verifyRow() {
        row {
            cell(verifyLabel)
                .applyToComponent { setAllowAutoWrapping(true) }
                .align(AlignX.LEFT)

            button(message("settings.verifyToken")) { verifyConfiguration() }
                .align(AlignX.RIGHT)
                .widthGroup("button")
        }
    }

    abstract fun verifyConfiguration()
}
