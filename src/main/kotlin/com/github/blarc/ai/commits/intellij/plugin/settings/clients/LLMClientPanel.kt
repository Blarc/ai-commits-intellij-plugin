package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.*
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ValidationInfoBuilder
import kotlin.reflect.KMutableProperty0


abstract class LLMClientPanel(
    private val clientConfiguration: LLMClientConfiguration,
) {

    val hostComboBox = ComboBox(clientConfiguration.getHosts().toTypedArray())
    val socketTimeoutTextField = JBTextField()
    val modelComboBox = ComboBox(clientConfiguration.getModelIds().toTypedArray())
    val temperatureTextField = JBTextField()
    val verifyLabel = JBLabel()

    abstract fun create(): DialogPanel

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

    open fun Panel.modelIdRow(labelKey: String = "settings.llmClient.modelId", commentKey: String? = null) {
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
                .apply { commentKey?.let { comment(message(commentKey)) } }

            getRefreshModelsFunction()?.let { f ->
                button(message("settings.refreshModels")) {
                    f.invoke()
                }
                    .align(AlignX.RIGHT)
                    .widthGroup("button")
            }

            contextHelp(message("settings.llmClient.modelId.comment"))
                .align(AlignX.RIGHT)
        }
    }

    open fun Panel.temperatureRow(property: MutableProperty<String>, validation: ValidationInfoBuilder.(String) -> ValidationInfo? = ValidationInfoBuilder::temperatureValid) {
        row {
            label(message("settings.llmClient.temperature"))
                .widthGroup("label")

            cell(temperatureTextField)
                .bindText(property)
                .align(Align.FILL)
                .validationOnInput { validation(it.text) }
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

    open fun Panel.topKRow(topKField: JBTextField, property: MutableProperty<Int?>) {
        row {
            label(message("settings.llmClient.topK"))
                .widthGroup("label")
            cell(topKField)
                .bindText({ property.get()?.toString() ?: "" }, { s -> property.set(s.toInt()) })
                .align(Align.FILL)
                .validationOnInput { isInt(it.text) }
                .resizableColumn()
            contextHelp(message("settings.llmClient.topK.comment"))
                .align(AlignX.RIGHT)
        }
    }

    open fun Panel.topPDoubleRow(topPField: JBTextField, property: MutableProperty<Double?>) {
        row {
            label(message("settings.llmClient.topP"))
                .widthGroup("label")
            cell(topPField)
                .bindText({ property.get()?.toString() ?: "" }, { s -> property.set(s.toDouble()) })
                .align(Align.FILL)
                .validationOnInput { isDouble(it.text) }
                .resizableColumn()
            contextHelp(message("settings.llmClient.topP.comment"))
                .align(AlignX.RIGHT)
        }
    }

    open fun Panel.topPFloatRow(topPField: JBTextField, property: MutableProperty<Float?>) {
        row {
            label(message("settings.llmClient.topP"))
                .widthGroup("label")

            cell(topPField)
                .bindText({ property.get()?.toString() ?: "" }, { s -> property.set(s.toFloat()) })
                .align(Align.FILL)
                .validationOnInput { isFloat(it.text) }
                .resizableColumn()
            contextHelp(message("settings.llmClient.topP.comment"))
                .align(AlignX.RIGHT)
        }
    }

    abstract fun verifyConfiguration()

    open fun getRefreshModelsFunction(): (() -> Unit)? = null
}
