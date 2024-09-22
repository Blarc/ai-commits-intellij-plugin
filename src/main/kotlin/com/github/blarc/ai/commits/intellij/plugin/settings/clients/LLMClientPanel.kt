package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.isInt
import com.github.blarc.ai.commits.intellij.plugin.notBlank
import com.github.blarc.ai.commits.intellij.plugin.temperatureValid
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
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
                .align(Align.FILL)
                .validationOnInput { notBlank(it.text) }
        }
    }

    open fun Panel.hostRow(property: MutableProperty<String?>, labelKey: String = "settings.llmClient.host") {
        row {
            label(message("settings.llmClient.host"))
                .widthGroup("label")
            cell(hostComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem(property)
                .align(Align.FILL)
                .onApply { clientConfiguration.addHost(hostComboBox.item) }
        }
    }

    open fun Panel.timeoutRow(property: KMutableProperty0<Int>) {
        row {
            label(message("settings.llmClient.timeout")).widthGroup("label")
            cell(socketTimeoutTextField)
                .bindIntText(property)
                .resizableColumn()
                .align(Align.FILL)
                .validationOnInput { isInt(it.text) }
        }
    }

    open fun Panel.modelIdRow(labelKey: String = "settings.llmClient.modelId") {
        row {
            label(message(labelKey))
                .widthGroup("label")

            cell(modelComboBox)
                .applyToComponent {
                    isEditable = true
                }
                .bindItem({ clientConfiguration.modelId }, {
                    if (it != null) {
                        clientConfiguration.addModelId(modelComboBox.item)
                        clientConfiguration.modelId = it
                    }
                })
                .align(Align.FILL)
                .resizableColumn()

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
                .align(Align.FILL)
                .validationOnInput { temperatureValid(it.text) }
                .resizableColumn()

            contextHelp(message("settings.llmClient.temperature.comment"))
                .align(AlignX.RIGHT)
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
                .align(AlignY.TOP)
                .widthGroup("button")
        }
    }

    abstract fun verifyConfiguration()
}
