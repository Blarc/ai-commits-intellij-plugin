package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.createColumn
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.anthropic.AnthropicClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.gemini.GeminiClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama.OllamaClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi.OpenAiClientConfiguration
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.qianfan.QianfanClientConfiguration
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Splitter
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.popup.ListItemDescriptorAdapter
import com.intellij.ui.JBCardLayout
import com.intellij.ui.components.JBList
import com.intellij.ui.popup.list.GroupedItemsListRenderer
import com.intellij.ui.table.TableView
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.ListSelectionModel.SINGLE_SELECTION
import javax.swing.table.DefaultTableCellRenderer
import kotlin.math.max

class LLMClientTable {
    private var llmClients = AppSettings2.instance.llmClientConfigurations
    private val tableModel = createTableModel()

    val table = TableView(tableModel).apply {
        setShowColumns(true)
        setSelectionMode(SINGLE_SELECTION)

        columnModel.getColumn(0).cellRenderer = IconTextCellRenderer()
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

    private fun createTableModel(): ListTableModel<LLMClientConfiguration> = ListTableModel(
        arrayOf(
            createColumn<LLMClientConfiguration, LLMClientConfiguration>(message("settings.llmClient.name")) { llmClient -> llmClient },
            createColumn<LLMClientConfiguration, String>(message("settings.llmClient.modelId")) { llmClient -> llmClient.modelId },
            createColumn(message("settings.llmClient.temperature")) { llmClient -> llmClient.temperature }
        ),
        llmClients.toList()
    )

    fun addLlmClient(): LLMClientConfiguration? {
        val dialog = LLMClientDialog()

        if (dialog.showAndGet()) {
            llmClients = llmClients.plus(dialog.llmClient)
            refreshTableModel()
            return dialog.llmClient
        }
        return null
    }

    fun removeLlmClient(): LLMClientConfiguration? {
        val selectedLlmClient = table.selectedObject ?: return null
        llmClients = llmClients.minus(selectedLlmClient)
        refreshTableModel()
        return selectedLlmClient

    }

    fun editLlmClient(): Pair<LLMClientConfiguration, LLMClientConfiguration>? {
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
        llmClients = AppSettings2.instance.llmClientConfigurations
        refreshTableModel()
    }

    fun isModified() = llmClients != AppSettings2.instance.llmClientConfigurations

    fun apply() {
        AppSettings2.instance.llmClientConfigurations = llmClients
    }

    private class LLMClientDialog(val newLlmClientConfiguration: LLMClientConfiguration? = null) : DialogWrapper(true) {

        private val llmClientConfigurations: List<LLMClientConfiguration> = getLlmClients(newLlmClientConfiguration)
        var llmClient = newLlmClientConfiguration ?: llmClientConfigurations[0]

        private val cardLayout = JBCardLayout()

        init {
            title = newLlmClientConfiguration?.let { "Edit LLM Client" } ?: "Add LLM Client"
            setOKButtonText(newLlmClientConfiguration?.let { message("actions.update") } ?: message("actions.add"))
            init()
        }

        override fun doOKAction() {
            if (newLlmClientConfiguration == null) {
                (cardLayout.findComponentById(llmClient.getClientName()) as DialogPanel).apply()
            }
            // TODO: Figure out how to call apply of the currently active panel
            super.doOKAction()
        }

        override fun createCenterPanel() = if (newLlmClientConfiguration == null) {
            createCardSplitter()
        } else {
            llmClient.panel().create()
        }.apply {
            isResizable = false
            minimumSize = Dimension(max(size.width, 500), max(size.height, 300))
        }

        private fun getLlmClients(newLLMClientConfiguration: LLMClientConfiguration?): List<LLMClientConfiguration> {
            return if (newLLMClientConfiguration == null) {
                // TODO(@Blarc): Is there a better way to create the list of all possible LLM Clients that implement LLMClient abstract class
                listOf(
                    OpenAiClientConfiguration(),
                    OllamaClientConfiguration(),
                    QianfanClientConfiguration(),
                    GeminiClientConfiguration(),
                    AnthropicClientConfiguration()
                )
            } else {
                listOf(newLLMClientConfiguration)
            }
        }

        private fun createCardSplitter(): JComponent {
            return Splitter(false, 0.25f).apply {

                val cardPanel = JPanel(cardLayout).apply {
                    llmClientConfigurations.forEach {
                        add(it.getClientName(), it.panel().create())
                    }
                }

                val cardsList = JBList(llmClientConfigurations).apply {
                    val descriptor = object : ListItemDescriptorAdapter<LLMClientConfiguration>() {
                        override fun getTextFor(value: LLMClientConfiguration) = value.getClientName()
                        override fun getIconFor(value: LLMClientConfiguration) = value.getClientIcon()
                    }
                    cellRenderer = object : GroupedItemsListRenderer<LLMClientConfiguration>(descriptor) {
                        override fun customizeComponent(list: JList<out LLMClientConfiguration>?, value: LLMClientConfiguration?, isSelected: Boolean) {
                            myTextLabel.border = JBUI.Borders.empty(4)
                        }
                    }
                    addListSelectionListener {
                        llmClient = selectedValue
                        cardLayout.show(cardPanel, llmClient.getClientName())

                        // Register validators of the currently active cards
                        val dialogPanel = cardLayout.findComponentById(llmClient.getClientName()) as DialogPanel
                        dialogPanel.registerValidators(myDisposable) {
                            isOKActionEnabled = ContainerUtil.and(it.values) { info: ValidationInfo -> info.okEnabled }
                        }
                    }
                    setSelectedValue(llmClient, true)
                }

                firstComponent = cardsList
                secondComponent = cardPanel
            }
        }
    }

    class IconTextCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            val llmClientConfiguration = value as LLMClientConfiguration
            return JLabel(llmClientConfiguration.name, llmClientConfiguration.getClientIcon(), SwingConstants.LEFT)
        }
    }

}
