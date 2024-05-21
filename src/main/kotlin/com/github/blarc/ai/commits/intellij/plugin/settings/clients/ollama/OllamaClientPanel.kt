package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientPanel
import com.github.blarc.ai.commits.intellij.plugin.temperatureValid
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.naturalSorted
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumWidth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.swing.DefaultComboBoxModel

class OllamaClientPanel(private val client: OllamaClient) : LLMClientPanel {

    private val hostComboBox = ComboBox(client.getHosts().toTypedArray())
    private val socketTimeoutTextField = JBTextField()
    private var modelComboBox = ComboBox(client.getModelIds().toTypedArray())

    override fun create() = panel {
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
        row {
            label(message("settings.llmClient.timeout")).widthGroup("label")
            cell(socketTimeoutTextField)
                .applyToComponent { minimumWidth = 400 }
                .bindIntText(client::timeout)
                .resizableColumn()
                .widthGroup("input")
                .validationOnInput { isInt(it.text) }
        }
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

            client.getRefreshModelFunction().let { f ->
                button(message("settings.refreshModels")) {
                    CoroutineScope(Dispatchers.Default).launch {
                        f.invoke()
                        modelComboBox.model = DefaultComboBoxModel(client.getModelIds().naturalSorted().toTypedArray())
                        modelComboBox.item = client.modelId
                    }
                }
                    .align(AlignX.RIGHT)
                    .widthGroup("button")
            }
        }
        row {
            label(message("settings.llmClient.temperature"))
                .widthGroup("label")

            textField()
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
}
