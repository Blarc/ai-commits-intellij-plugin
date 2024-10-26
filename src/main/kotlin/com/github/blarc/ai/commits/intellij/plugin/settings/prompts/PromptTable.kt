package com.github.blarc.ai.commits.intellij.plugin.settings.prompts

import ai.grazie.utils.applyIf
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.commonBranch
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.computeDiff
import com.github.blarc.ai.commits.intellij.plugin.createColumn
import com.github.blarc.ai.commits.intellij.plugin.notBlank
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama.OllamaClientSharedState
import com.github.blarc.ai.commits.intellij.plugin.unique
import com.intellij.dvcs.repo.VcsRepositoryManager
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ListTableModel
import git4idea.branch.GitBranchWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ListSelectionModel.SINGLE_SELECTION
import kotlin.math.max

class PromptTable(private val cs: CoroutineScope) {
    private var prompts = AppSettings2.instance.prompts
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
            createColumn<Prompt, String>(message("settings.prompt.name")) { prompt -> prompt.name },
            createColumn(message("settings.prompt.description")) { prompt -> prompt.description },
        ),
        prompts.values.toList()
    )

    fun addPrompt(): Prompt? {
        val dialog = PromptDialog(prompts.keys.toSet(), cs)

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
        val dialog = PromptDialog(prompts.keys.toSet(), cs, selectedPrompt.copy())

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
        prompts = AppSettings2.instance.prompts
        refreshTableModel()
    }

    fun isModified() = prompts != AppSettings2.instance.prompts

    fun apply() {
        AppSettings2.instance.prompts = prompts
    }

    private class PromptDialog(val prompts: Set<String>, private val cs: CoroutineScope, val newPrompt: Prompt? = null) : DialogWrapper(true) {
        val prompt = newPrompt ?: Prompt("")
        val promptNameTextField = JBTextField()
        val promptDescriptionTextField = JBTextField()
        val promptHintTextField = JBTextField()
        val promptContentTextArea = JBTextArea()
        val promptPreviewTextArea = JBTextArea()
        lateinit var branch: String
        lateinit var diff: String
        lateinit var project: Project
        private val logger = Logger.getInstance(PromptDialog::class.java)

        init {
            val sizeState = PromptDialogSizeState.getInstance()
            setSize(sizeState.width, sizeState.height)

            title = newPrompt?.let { message("settings.prompt.edit.title") } ?: message("settings.prompt.add.title")
            setOKButtonText(newPrompt?.let { message("actions.update") } ?: message("actions.add"))

            promptContentTextArea.wrapStyleWord = true
            promptContentTextArea.lineWrap = true
            promptContentTextArea.rows = 5
            promptContentTextArea.autoscrolls = false

            if (!prompt.canBeChanged) {
                isOKActionEnabled = false
                promptNameTextField.isEditable = false
                promptDescriptionTextField.isEditable = false
                promptContentTextArea.isEditable = false
            }

            promptPreviewTextArea.wrapStyleWord = true
            promptPreviewTextArea.lineWrap = true
            promptPreviewTextArea.isEditable = false
            promptPreviewTextArea.rows = 5
            promptPreviewTextArea.columns = 100
            promptPreviewTextArea.autoscrolls = false

            DataManager.getInstance().getDataContext(rootPane).getData(CommonDataKeys.PROJECT)?.let { project ->
                this.project = project
                getChangesAndSetPreview(project)
            }

            init()
        }

        fun getSharedState(): PromptDialogSizeState {
            return PromptDialogSizeState.getInstance()
        }

        override fun doOKAction() {
            val sizeState = getSharedState()
            sizeState.width = size.width
            sizeState.height = size.height

            super.doOKAction()
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
            row(message("settings.prompt.hint")) {
                cell(promptHintTextField)
                    .align(Align.FILL)
                    .text("This is a hint.")
                    .onChanged { setPreview(promptContentTextArea.text, it.text) }
                    .comment(message("settings.prompt.hint.comment"))
            }
            row {
                label(message("settings.prompt.content"))
            }
            row {
                scrollCell(promptContentTextArea)
                    .bindText(prompt::content)
                    .validationOnApply { notBlank(it.text) }
                    .onChanged { setPreview(it.text, promptHintTextField.text) }
                    .align(Align.FILL)
            }.resizableRow()
            row {
                label("Preview")
            }
            row {
                scrollCell(promptPreviewTextArea)
                    .align(Align.FILL)
            }.resizableRow()
            row {
                comment(message("settings.prompt.comment"))
            }
        }

        private fun getChangesAndSetPreview(project: Project) = cs.launch(Dispatchers.IO + ModalityState.stateForComponent(rootPane).asContextElement()) {
            val repositories = VcsRepositoryManager.getInstance(project).repositories
            if (repositories.isEmpty()) {
                logger.warn("No Git repositories found in the project.")
                return@launch
            }

            val changes = repositories.asSequence()
                .filter { it.currentBranchName != null }
                .flatMap { repository ->
                    val branchName = repository.currentBranchName!!
                    try {
                        GitBranchWorker.loadTotalDiff(repository, branchName).asSequence()
                    } catch (e: Exception) {
                        logger.error("Failed to load diff for branch '$branchName' in repository: ${repository.presentableUrl}", e)
                        emptySequence()
                    }
                }
                .toList()

            if (changes.isEmpty()) {
                logger.warn("No changes found for the current branch.")
            }

            branch = commonBranch(changes, project, false)
            diff = computeDiff(changes, true, project)

            withContext(Dispatchers.EDT) {
                setPreview(prompt.content, promptHintTextField.text)
            }
        }

        private fun setPreview(promptContent: String, hint: String) {
            val constructPrompt = AICommitsUtils.constructPrompt(promptContent, diff, branch, hint, project)
            promptPreviewTextArea.text = constructPrompt.substring(0, constructPrompt.length.coerceAtMost(10000))
            promptPreviewTextArea.caretPosition = max(0, promptPreviewTextArea.caretPosition - 10)
        }

    }
}
