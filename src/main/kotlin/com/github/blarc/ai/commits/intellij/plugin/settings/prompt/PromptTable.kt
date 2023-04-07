package com.github.blarc.ai.commits.intellij.plugin.settings.prompt

import ai.grazie.utils.applyIf
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.createColumn
import com.github.blarc.ai.commits.intellij.plugin.notBlank
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.github.blarc.ai.commits.intellij.plugin.unique
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ListTableModel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ListSelectionModel.SINGLE_SELECTION

class PromptTable {
    private var prompts = AppSettings.instance.prompts
    private val tableModel = createTableModel()

    val table = TableView(tableModel).apply {
        setShowColumns(true)
        setSelectionMode(SINGLE_SELECTION)

        columnModel.getColumn(0).preferredWidth = 150
        columnModel.getColumn(0).maxWidth = 250

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e?.clickCount == 2) {
                    editPrompt()
                }
            }
        })
    }

    private fun createTableModel(): ListTableModel<Prompt> = ListTableModel(
            arrayOf(
                    createColumn<Prompt>(message("settings.prompt.name")) { prompt -> prompt.name },
                    createColumn(message("settings.prompt.description")) { prompt -> prompt.description },
            ),
            prompts.values.toList()
    )

    fun addPrompt(): Prompt? {
        val dialog = PromptDialog(prompts.keys.toSet())

        if (dialog.showAndGet()) {
            prompts = prompts.plus(dialog.prompt.name.lowercase() to dialog.prompt).toMutableMap()
            refreshTableModel()
            return dialog.prompt
        }
        return null
    }

    fun removePrompt(): Prompt? {
        val selectedPrompt = table.selectedObject ?: return null
        prompts = prompts.minus(selectedPrompt.name.lowercase()).toMutableMap()
        refreshTableModel()
        return selectedPrompt
    }

    fun editPrompt(): Pair<Prompt, Prompt>? {
        val selectedPrompt = table.selectedObject ?: return null
        val dialog = PromptDialog(prompts.keys.toSet(), selectedPrompt.copy())

        if (dialog.showAndGet()) {
            prompts = prompts.minus(selectedPrompt.name.lowercase()).toMutableMap()
            prompts[dialog.prompt.name.lowercase()] = dialog.prompt
            refreshTableModel()
            return selectedPrompt to dialog.prompt
        }
        return null
    }

    private fun refreshTableModel() {
        tableModel.items = prompts.values.toList()
    }

    fun reset() {
        prompts = AppSettings.instance.prompts
        refreshTableModel()
    }

    fun isModified() = prompts != AppSettings.instance.prompts

    fun apply() {
        AppSettings.instance.prompts = prompts
    }

    private class PromptDialog(val prompts: Set<String>, val newPrompt: Prompt? = null) : DialogWrapper(true) {

        val prompt = newPrompt ?: Prompt("")
        val promptNameTextField = JBTextField()
        val promptDescriptionTextField = JBTextField()
        val promptContentTextArea = JBTextArea()

        init {
            title = newPrompt?.let { message("settings.prompt.edit.title") } ?: message("settings.prompt.add.title")
            setOKButtonText(newPrompt?.let { message("actions.update") } ?: message("actions.add"))
            setSize(700, 500)

            promptContentTextArea.wrapStyleWord = true
            promptContentTextArea.lineWrap = true

            if (!prompt.canBeChanged) {
                isOKActionEnabled = false
                promptNameTextField.isEditable = false
                promptDescriptionTextField.isEditable = false
                promptContentTextArea.isEditable = false
            }

            init()
        }

        override fun createCenterPanel() = panel {
            row(message("settings.prompt.name")) {
                cell(promptNameTextField)
                        .align(Align.FILL)
                        .bindText(prompt::name)
                        .applyIf(prompt.canBeChanged) { focused() }
                        .validationOnApply { notBlank(it.text) }
                        .applyIf(newPrompt == null) { validationOnApply { unique(it.text.lowercase(), prompts) } }
            }
            row(message("settings.prompt.description")) {
                cell(promptDescriptionTextField)
                        .align(Align.FILL)
                        .bindText(prompt::description)
                        .validationOnApply { notBlank(it.text) }
            }
            row {
                label(message("settings.prompt.content"))
            }
            row() {
                cell(promptContentTextArea)
                        .align(Align.FILL)
                        .bindText(prompt::content)
                        .validationOnApply { notBlank(it.text) }
                        .resizableColumn()
            }.resizableRow()
            row {
                comment(message("settings.prompt.comment"))
            }
        }

    }
}