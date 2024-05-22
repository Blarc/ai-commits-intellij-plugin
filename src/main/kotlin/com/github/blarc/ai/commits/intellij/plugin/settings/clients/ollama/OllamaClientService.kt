package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.*
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.naturalSorted
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaModels
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.swing.DefaultComboBoxModel

@Service(Service.Level.APP)
@State(name = "OllamaClientService", storages = [Storage("AICommitsOllama.xml")])
class OllamaClientService(
    @Transient private val cs: CoroutineScope?
):
    PersistentStateComponent<OllamaClientService>,
    LLMClientService<OllamaClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): OllamaClientService = service()
    }

    @XCollection(style = XCollection.Style.v2)
    val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    val modelIds: MutableSet<String> = mutableSetOf("llama3")

    override fun getState(): OllamaClientService = this

    override fun loadState(state: OllamaClientService) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun refreshModels(client: OllamaClientConfiguration, comboBox: ComboBox<String>) {
        val ollamaModels = OllamaModels.builder()
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .baseUrl(client.host)
            .build()

        cs!!.launch(Dispatchers.Default) {
            val availableModels = withContext(Dispatchers.IO) {
                ollamaModels.availableModels()
            }
            modelIds.addAll(availableModels.content()
                .map { it.name }
            )
            withContext(Dispatchers.EDT) {
                comboBox.model = DefaultComboBoxModel(client.getModelIds().naturalSorted().toTypedArray())
                comboBox.item = client.modelId
            }
        }
    }

    override fun buildChatModel(client: OllamaClientConfiguration): ChatLanguageModel {
        return OllamaChatModel.builder()
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .baseUrl(client.host)
            .build()
    }
}
