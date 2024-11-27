package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.naturalSorted
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.chat.StreamingChatLanguageModel
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaModels
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.swing.DefaultComboBoxModel

@Service(Service.Level.APP)
class OllamaClientService(private val cs: CoroutineScope) : LLMClientService<OllamaClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): OllamaClientService = service()
    }

    fun refreshModels(client: OllamaClientConfiguration, comboBox: ComboBox<String>) {
        val ollamaModels = OllamaModels.builder()
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .baseUrl(client.host)
            .build()

        cs.launch(Dispatchers.Default) {
            val availableModels = withContext(Dispatchers.IO) {
                ollamaModels.availableModels()
            }

            OllamaClientSharedState.getInstance().modelIds.addAll(availableModels.content()
                .map { it.name }
            )

            // This can't be called from EDT thread, because dialog blocks the EDT thread
            val modelItems = client.getModelIds().naturalSorted().toTypedArray()
            comboBox.model = DefaultComboBoxModel(modelItems)
            comboBox.item = client.modelId
        }
    }

    override suspend fun buildChatModel(client: OllamaClientConfiguration): ChatLanguageModel {
        return OllamaChatModel.builder()
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .numCtx(client.numCtx)
            .numPredict(client.numPredict)
            .topK(client.topK)
            .topP(client.topP)
            .baseUrl(client.host)
            .build()
    }

    override suspend fun buildStreamingChatModel(client: OllamaClientConfiguration): StreamingChatLanguageModel {
        return OllamaStreamingChatModel.builder()
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .numCtx(client.numCtx)
            .numPredict(client.numPredict)
            .topK(client.topK)
            .topP(client.topP)
            .baseUrl(client.host)
            .build()
    }
}
