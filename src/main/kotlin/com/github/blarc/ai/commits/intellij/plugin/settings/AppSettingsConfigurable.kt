package com.github.blarc.ai.commits.intellij.plugin.settings

import com.aallam.openai.api.exception.OpenAIAPIException
import com.github.blarc.ai.commits.intellij.plugin.*
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.Prompt
import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.PromptTable
import com.intellij.icons.AllIcons
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.naturalSorted
import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.util.minimumWidth
import kotlinx.coroutines.*
import java.util.*
import javax.swing.DefaultComboBoxModel

class AppSettingsConfigurable : BoundConfigurable(message("settings.general.group.title")) {

    private val hostComboBox = ComboBox<String>()
    private val tokenPasswordField = JBPasswordField()
    private val verifyLabel = JBLabel()
    private val proxyTextField = JBTextField()
    private val socketTimeoutTextField = JBTextField()
    private var modelComboBox = ComboBox<String>()
    private val promptTable = PromptTable()
    private lateinit var toolbarDecorator: ToolbarDecorator
    private lateinit var promptComboBox: Cell<ComboBox<Prompt>>

    init {
        hostComboBox.isEditable = true
        hostComboBox.model = DefaultComboBoxModel(Vector(AppSettings.instance.openAIHosts.naturalSorted()))
        modelComboBox.model = DefaultComboBoxModel(Vector(AppSettings.instance.openAIModelIds.naturalSorted()))
        modelComboBox.renderer = AppSettingsListCellRenderer()
    }

    override fun createPanel() = panel {

        group(JBLabel("OpenAI")) {
            row {
                label(message("settings.openAIHost"))
                        .widthGroup("label")

                cell(hostComboBox)
                        .bindItem(AppSettings.instance::openAIHost.toNullableProperty())
                        .widthGroup("input")
            }
            row {
                label(message("settings.openAIProxy")).widthGroup("label")
                cell(proxyTextField)
                        .bindText(AppSettings.instance::proxyUrl.toNonNullableProperty(""))
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
                    .bindIntText(AppSettings.instance::openAISocketTimeout)
                    .applyToComponent { minimumWidth = 400 }
                    .resizableColumn()
                    .widthGroup("input")
                    .validationOnInput { isInt(it.text) }
            }
            row {
                label(message("settings.openAIToken"))
                        .widthGroup("label")
                cell(tokenPasswordField)
                        .bindText(
                                { AppSettings.instance.getOpenAIToken().orEmpty() },
                                { AppSettings.instance.saveOpenAIToken(it) }
                        )
                        .emptyText(message("settings.openAITokenExample"))
                        .align(Align.FILL)
                        .resizableColumn()
                        .focused()
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
                label(message("settings.openAIModel")).widthGroup("label")

                cell(modelComboBox)
                        .bindItem({ AppSettings.instance.openAIModelId }, {
                            if (it != null) {
                                AppSettings.instance.openAIModelId = it
                            }
                        })
                        .resizableColumn()
                        .align(Align.FILL)
                button(message("settings.refreshModels")) {
                    runBackgroundableTask(message("settings.loadingModels")) {
                        runBlocking(Dispatchers.IO) {
                            OpenAIService.instance.refreshOpenAIModelIds()
                            modelComboBox.model = DefaultComboBoxModel(Vector(AppSettings.instance.openAIModelIds.naturalSorted()))
                            modelComboBox.item = AppSettings.instance.openAIModelId
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
                        .bindText(AppSettings.instance::openAITemperature)
                        .applyToComponent { minimumWidth = 400 }
                        .resizableColumn()
                        .widthGroup("input")
                        .validationOnInput { temperatureValid(it.text) }

                contextHelp(message("settings.openAITemperatureComment"))
            }

            row {
                cell(verifyLabel)
                        .align(AlignX.RIGHT)
            }
        }

        group(JBLabel("Prompt")) {
            row {
                label(message("settings.locale")).widthGroup("labelPrompt")
                comboBox(Locale.getAvailableLocales()
                        .distinctBy { it.displayLanguage }
                        .sortedBy { it.displayLanguage },
                        AppSettingsListCellRenderer()
                )
                        .bindItem(AppSettings.instance::locale.toNullableProperty())
                browserLink(message("settings.more-prompts"), AICommitsBundle.URL_PROMPTS_DISCUSSION.toString())
                        .align(AlignX.RIGHT)
            }
            row {
                label(message("settings.prompt")).widthGroup("labelPrompt")
                promptComboBox = comboBox(AppSettings.instance.prompts.values, AppSettingsListCellRenderer())
                        .bindItem(AppSettings.instance::currentPrompt.toNullableProperty())
            }
            row {
                toolbarDecorator = ToolbarDecorator.createDecorator(promptTable.table)
                        .setAddAction {
                            promptTable.addPrompt().let {
                                promptComboBox.component.addItem(it)
                            }
                        }
                        .setEditAction {
                            promptTable.editPrompt()?.let {
                                val editingSelected = promptComboBox.component.selectedItem == it.first
                                promptComboBox.component.removeItem(it.first)
                                promptComboBox.component.addItem(it.second)

                                if (editingSelected) {
                                    promptComboBox.component.selectedItem = it.second
                                }
                            }
                        }
                        .setEditActionUpdater {
                            updateActionAvailability(CommonActionsPanel.Buttons.EDIT)
                            true
                        }
                        .setRemoveAction {
                            promptTable.removePrompt()?.let {
                                promptComboBox.component.removeItem(it)
                            }
                        }
                        .setRemoveActionUpdater {
                            updateActionAvailability(CommonActionsPanel.Buttons.REMOVE)
                            true
                        }
                        .disableUpDownActions()

                cell(toolbarDecorator.createPanel())
                        .align(Align.FILL)
            }.resizableRow()
        }.resizableRow()

        row {
            browserLink(message("settings.report-bug"), AICommitsBundle.URL_BUG_REPORT.toString())
        }
    }

    private fun updateActionAvailability(action: CommonActionsPanel.Buttons) {
        val selectedRow = promptTable.table.selectedRow
        val selectedPrompt = promptTable.table.items[selectedRow]
        toolbarDecorator.actionsPanel.setEnabled(action, selectedPrompt.canBeChanged)
    }

    override fun isModified(): Boolean {
        return super.isModified() || promptTable.isModified()
    }

    override fun apply() {
        AppSettings.instance.openAIHosts.add(hostComboBox.item)
        promptTable.apply()
        super.apply()
    }

    override fun reset() {
        promptTable.reset()
        super.reset()
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
                        OpenAIService.instance.verifyOpenAIConfiguration(hostComboBox.item, String(tokenPasswordField.password), proxyTextField.text, socketTimeoutTextField.text)
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
