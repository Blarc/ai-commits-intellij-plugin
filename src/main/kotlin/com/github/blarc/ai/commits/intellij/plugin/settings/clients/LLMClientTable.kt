package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.createColumn
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.ui.popup.ListItemDescriptorAdapter
import com.intellij.ui.JBCardLayout
import com.intellij.ui.components.JBList
import com.intellij.ui.popup.list.GroupedItemsListRenderer
import com.intellij.ui.table.TableView
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel
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

    fun editLlmClient(): Pair<LLMClient, LLMClient>? {
        val selectedLlmClient = table.selectedObject ?: return null
        val dialog = LLMClientDialog(selectedLlmClient.clone())

        if (dialog.showAndGet()) {
            llmClients = llmClients.minus(selectedLlmClient)
            llmClients = llmClients.plus(dialog.llmClient)
            refreshTableModel()
            return selectedLlmClient to dialog.llmClient
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

        private val llmClients: List<LLMClient> = getLlmClients(newLlmClient)
        var llmClient = newLlmClient ?: llmClients[0]

        private val cardLayout = JBCardLayout().apply {
            // Register validators of the currently active cards
//            (findComponentById(llmClient.displayName) as DialogPanel).registerValidators(myDisposable) {
//                isOKActionEnabled = ContainerUtil.and(it.values) { info: ValidationInfo -> info.okEnabled }
//            }
        }

        init {
            title = newLlmClient?.let { "Edit LLM Client" } ?: "Add LLM Client"
            setOKButtonText(newLlmClient?.let { message("actions.update") } ?: message("actions.add"))
            init()
        }

        override fun doOKAction() {
            (cardLayout.findComponentById(llmClient.displayName) as DialogPanel).apply()
            super.doOKAction()
        }

        override fun createCenterPanel() = if (newLlmClient == null) {
            createCardSplitter()
        } else {
            llmClient.panel().create()
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

        private fun createCardSplitter(): JComponent {
            return Splitter(false, 0.25f).apply {

                val cardPanel = JPanel(cardLayout).apply {
                    preferredSize = JBUI.size(640, 480)
                    llmClients.forEach {
                        add(it.displayName, it.panel().create())
                    }
                }

                val cardsList = JBList(llmClients).apply {
                    val descriptor = object : ListItemDescriptorAdapter<LLMClient>() {
                        override fun getTextFor(value: LLMClient) = value.displayName
                        override fun getIconFor(value: LLMClient) = value.getIcon()
                    }
                    cellRenderer = object : GroupedItemsListRenderer<LLMClient>(descriptor) {
                        override fun createItemComponent() = super.createItemComponent().apply {
                            border = JBUI.Borders.empty(4, 4, 4, 10)
                        }
                    }
                    addListSelectionListener {
                        llmClient = selectedValue
                        cardLayout.show(cardPanel, llmClient.displayName)
                    }
                }

                firstComponent = cardsList
                secondComponent = cardPanel
            }
        }
    }

}
