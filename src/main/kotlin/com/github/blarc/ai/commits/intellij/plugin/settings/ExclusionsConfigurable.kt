package com.github.blarc.ai.commits.intellij.plugin.settings

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.createColumn
import com.intellij.openapi.components.service
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ListTableModel
import javax.swing.ListSelectionModel

class ExclusionsConfigurable(val project: Project) : BoundConfigurable(message("settings.exclusions.group.title")) {

    private var appExclusions = AppSettings2.instance.appExclusions.toMutableSet()
    private var projectExclusions = project.service<ProjectSettings>().projectExclusions.toMutableSet()

    private val appTableModel = createTableModel()
    private val projectTableModel = createTableModel()

    private val appTable = TableView(appTableModel).apply {
        setShowColumns(false)
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    }

    private val projectTable = TableView(projectTableModel).apply {
        setShowColumns(false)
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
    }

    override fun createPanel() = panel {
        row {
            label(message("settings.exclusions.app.title"))
        }
        row {
            cell(
                    ToolbarDecorator.createDecorator(appTable)
                            .setAddAction { addExclusion(appExclusions) }
                            .setEditAction { editExclusion(appTable, appExclusions) }
                            .setRemoveAction { removeExclusion(appTable, appExclusions) }
                            .disableUpAction()
                            .disableDownAction()
                            .createPanel()
            ).align(Align.FILL)
        }.resizableRow()

        row {
            label(message("settings.exclusions.project.title"))
        }
        row {
            cell(
                    ToolbarDecorator.createDecorator(projectTable)
                            .setAddAction { addExclusion(projectExclusions) }
                            .setEditAction { editExclusion(projectTable, projectExclusions) }
                            .setRemoveAction { removeExclusion(projectTable, projectExclusions) }
                            .disableUpAction()
                            .disableDownAction()
                            .createPanel()
            ).align(Align.FILL)
        }.resizableRow()
    }

    private fun createTableModel(): ListTableModel<String> = ListTableModel(
        arrayOf(
            createColumn<String>(message("settings.exclusions.app.exclusion")) { exclusion -> exclusion }
        ),
        appExclusions.toList()
    )

    private fun addExclusion(exclusions: MutableSet<String>) {
        val dialog = ExclusionDialog()

        if (dialog.showAndGet()) {
            exclusions.add(dialog.exclusion)
            refreshTableModel()
        }
    }

    private fun removeExclusion(table: TableView<String>, exclusions: MutableSet<String>) {
        val exclusion = table.selectedObject ?: return
        exclusions.remove(exclusion)
        refreshTableModel()
    }

    private fun editExclusion(table: TableView<String>, exclusions: MutableSet<String>) {
        val exclusion = table.selectedObject ?: return

        val dialog = ExclusionDialog(exclusion)
        if (dialog.showAndGet()) {
            if (dialog.exclusion.isEmpty()) {
                exclusions.remove(exclusion)
            } else {
                exclusions.remove(exclusion)
                exclusions.add(dialog.exclusion)
            }
            refreshTableModel()
        }
    }

    override fun reset() {
        super.reset()
        appExclusions = AppSettings2.instance.appExclusions.toMutableSet()
        projectExclusions = project.service<ProjectSettings>().projectExclusions.toMutableSet()
        refreshTableModel()
    }

    override fun apply() {
        super.apply()
        AppSettings2.instance.appExclusions = appExclusions
        project.service<ProjectSettings>().projectExclusions = projectExclusions
    }
    override fun isModified(): Boolean {
        return super.isModified() ||
                appExclusions != AppSettings2.instance.appExclusions.toList() ||
                projectExclusions != project.service<ProjectSettings>().projectExclusions.toList()
    }

    private fun refreshTableModel() {
        appTableModel.items = appExclusions.toList()
        projectTableModel.items = projectExclusions.toList()
    }

    private class ExclusionDialog(var exclusion: String = "") : DialogWrapper(true) {
        init {
            title = message("settings.exclusions.app.dialog.title")
            if (exclusion.isEmpty()) {
                setOKButtonText(message("actions.add"))
            } else {
                setOKButtonText(message("actions.update"))
            }
            init()
        }

        override fun createCenterPanel() = panel {
            row {
                label(message("settings.exclusions.app.dialog.label"))
                textField()
                        .text(exclusion)
                        .bindText({ exclusion }, { exclusion = it })
                        .focused()
            }
        }
    }
}
