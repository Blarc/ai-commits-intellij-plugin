package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.notBlank
import com.github.blarc.ai.commits.intellij.plugin.temperatureValid
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumWidth
import kotlin.reflect.KMutableProperty0


abstract class LLMClientPanel(
    private val clientConfiguration: LLMClientConfiguration,
) {

    val hostComboBox = ComboBox(clientConfiguration.getHosts().toTypedArray())
    val socketTimeoutTextField = JBTextField()
    val modelComboBox = ComboBox(clientConfiguration.getModelIds().toTypedArray())
    val temperatureTextField = JBTextField()
    val verifyLabel = JBLabel()

    open fun create() = panel {
        nameRow()
        modelIdRow()
        temperatureRow()
        verifyRow()
    }

    open fun Panel.nameRow() {
        row {
            label(message("settings.llmClient.name"))
                .widthGroup("label")
            textField()
                .bindText(clientConfiguration::name)
                .widthGroup("input")
                .validationOnInput { notBlank(it.text) }
        }
    }

    open fun Panel.hostRow(property: MutableProperty<String?>) {
        row {
            label(message("settings.llmClient.host"))
                .widthGroup("label")
            cell(hostComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem(property)
                .widthGroup("input")
                .onApply { clientConfiguration.addHost(hostComboBox.item) }
        }
    }

    open fun Panel.timeoutRow(property: KMutableProperty0<Int>) {
        row {
            label(message("settings.llmClient.timeout")).widthGroup("label")
            cell(socketTimeoutTextField)
                .applyToComponent { minimumWidth = 400 }
                .bindIntText(property)
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
                .bindItem({ clientConfiguration.modelId }, {
                    if (it != null) {
                        clientConfiguration.modelId = it
                    }
                })
                .widthGroup("input")
                .resizableColumn()
                .onApply { clientConfiguration.addModelId(modelComboBox.item) }

            clientConfiguration.getRefreshModelsFunction()?.let { f ->
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
                .bindText(clientConfiguration::temperature)
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
                .applyToComponent {
                    setAllowAutoWrapping(true)
                    setCopyable(true)
                }
                .align(AlignX.LEFT)

            button(message("settings.verifyToken")) { verifyConfiguration() }
                .align(AlignX.RIGHT)
                .widthGroup("button")
        }
    }

    abstract fun verifyConfiguration()
}
