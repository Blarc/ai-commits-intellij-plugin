package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle
import com.github.blarc.ai.commits.intellij.plugin.createColumn
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBCardLayout
import com.intellij.ui.components.Panel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ListTableModel
import javax.swing.DefaultComboBoxModel
import javax.swing.ListSelectionModel.SINGLE_SELECTION

class LLMClientTable {
    private var llmClients = AppSettings2.instance.llmClients
    private val tableModel = createTableModel()

    val table = TableView(tableModel).apply {
        setShowColumns(true)
        setSelectionMode(SINGLE_SELECTION)
    }

    private fun createTableModel(): ListTableModel<LLMClient> = ListTableModel(
        arrayOf(
            createColumn<LLMClient>(AICommitsBundle.message("settings.prompt.name")) { llmClient -> llmClient.displayName },
        ),
        llmClients.toList()
    )

    fun addLlmClient(): LLMClient? {
        val dialog = LLMClientDialog()

        if (dialog.showAndGet()) {
            return null
        }
        return null
    }

    private class LLMClientDialog(val newLlmClient: LLMClient? = null) : DialogWrapper(true) {
        private val cardLayout = JBCardLayout()
        private val cardPanel = Panel(null, cardLayout)
        private val llmClients: List<LLMClient> = getLlmClients(newLlmClient)
        private val llmClientsComboBox: ComboBox<LLMClient> = createLlmClientsComboBox(newLlmClient)

        init {
            title = newLlmClient?.let { "Edit LLM Client" } ?: "Add LLM Client"
            llmClients.forEach {
                cardPanel.add(it.displayName, it.panel().create())
            }
            cardLayout.show(cardPanel, llmClients[0].displayName)
            init()
        }

        override fun createCenterPanel() = panel {
            row("Type") {
                cell(llmClientsComboBox)
                    .align(Align.FILL)
                    .applyToComponent {
                        addItemListener { cardLayout.show(cardPanel, (it.item as LLMClient).displayName) }
                    }
            }
            row {
                cell(cardPanel)
            }
        }

        private fun getLlmClients(newLLMClient: LLMClient?) : List<LLMClient> {
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

        private fun createLlmClientsComboBox(newLLMClient: LLMClient?) : ComboBox<LLMClient> {
            val comboBoxModel = DefaultComboBoxModel(getLlmClients(newLLMClient).toTypedArray())
            with(ComboBox(comboBoxModel)){
                // Type of LLM Client can not be changed when editing a LLM Client
                if (newLLMClient != null) isEnabled = false
                return this
            }
        }
    }

}
