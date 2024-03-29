package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.createColumn
import com.github.blarc.ai.commits.intellij.plugin.settings.AICommitsListCellRenderer
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBCardLayout
import com.intellij.ui.components.Panel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ListTableModel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.ListSelectionModel.SINGLE_SELECTION

class LLMClientTable {
    private var llmClients = AppSettings2.instance.llmClients
    private val tableModel = createTableModel()

    val table = TableView(tableModel).apply {
        setShowColumns(true)
        setSelectionMode(SINGLE_SELECTION)

        columnModel.getColumn(0).preferredWidth = 150
        columnModel.getColumn(0).maxWidth = 250

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e?.clickCount == 2) {
                    editLlmClient()
                }
            }
        })
    }

    private fun createTableModel(): ListTableModel<LLMClient> = ListTableModel(
        arrayOf(
            createColumn<LLMClient>(message("settings.llmClient.name")) { llmClient -> llmClient.displayName },
            createColumn(message("settings.llmClient.host")) { llmClient -> llmClient.host },
            createColumn(message("settings.llmClient.modelId")) { llmClient -> llmClient.modelId }
        ),
        llmClients.toList()
    )

    fun addLlmClient(): LLMClient? {
        val dialog = LLMClientDialog()

        if (dialog.showAndGet()) {
            llmClients = llmClients.plus(dialog.llmClient)
            refreshTableModel()
            return dialog.llmClient
        }
        return null
    }

    fun removeLlmClient(): LLMClient? {
        val selectedLlmClient = table.selectedObject ?: return null
        llmClients = llmClients.minus(selectedLlmClient)
        refreshTableModel()
        return selectedLlmClient

    }

    fun editLlmClient(): LLMClient? {
        val selectedLlmClient = table.selectedObject ?: return null
        val dialog = LLMClientDialog(selectedLlmClient)

        if (dialog.showAndGet()) {
            refreshTableModel()
            return selectedLlmClient
        }
        return null
    }

    private fun refreshTableModel() {
        tableModel.items = llmClients.toList()
    }

    fun reset() {
        llmClients = AppSettings2.instance.llmClients
        refreshTableModel()
    }

    fun isModified() = llmClients != AppSettings2.instance.llmClients

    fun apply() {
        AppSettings2.instance.llmClients = llmClients
    }

    private class LLMClientDialog(val newLlmClient: LLMClient? = null) : DialogWrapper(true) {
        private val cardLayout = JBCardLayout()
        private val cardPanel = Panel(null, cardLayout)
        private val llmClients: List<LLMClient> = getLlmClients(newLlmClient)
        var llmClient = newLlmClient ?: llmClients[0]

        init {
            title = newLlmClient?.let { "Edit LLM Client" } ?: "Add LLM Client"
            setOKButtonText(newLlmClient?.let { message("actions.update") } ?: message("actions.add"))

            llmClients.forEach {
                cardPanel.add(it.displayName, it.panel().create())
            }
            cardLayout.show(cardPanel, llmClients[0].displayName)
            init()
        }

        override fun createCenterPanel() = panel {
            row("Client") {
                comboBox(llmClients, AICommitsListCellRenderer())
                    .align(Align.FILL)
                    .applyToComponent {
                        addItemListener { cardLayout.show(cardPanel, (it.item as LLMClient).displayName) }
                    }
                    .applyToComponent {
                        isEnabled = newLlmClient == null
                    }
                    .bindItem(::llmClient.toNullableProperty())
            }
            row {
                cell(cardPanel)
            }
        }

        private fun getLlmClients(newLLMClient: LLMClient?): List<LLMClient> {
            return if (newLLMClient == null) {
                // TODO: Find a better way to create the list of all possible LLM Clients that implement LLMClient abstract class
                listOf(
                    OpenAIClient(),
                    TestAIClient()
                )
            } else {
                listOf(newLLMClient)
            }
        }
    }

}
