package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiVertex

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiChatModel
import dev.langchain4j.model.vertexai.gemini.VertexAiGeminiStreamingChatModel
import kotlinx.coroutines.CoroutineScope

@Service(Service.Level.APP)
class GeminiVertexClientService(private val cs: CoroutineScope): LLMClientService<GeminiClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiVertexClientService = service()
    }

    override suspend fun buildChatModel(client: GeminiClientConfiguration): ChatModel {
        return VertexAiGeminiChatModel.builder()
            .project(client.projectId)
            .location(client.location)
            .modelName(client.modelId)
            .temperature(client.temperature.toFloat())
            .topP(client.topP)
            .topK(client.topK)
            .build()
    }

    override suspend fun buildStreamingChatModel(client: GeminiClientConfiguration): StreamingChatModel {
        return VertexAiGeminiStreamingChatModel.builder()
            .project(client.projectId)
            .location(client.location)
            .modelName(client.modelId)
            .temperature(client.temperature.toFloat())
            .topP(client.topP)
            .topK(client.topK)
            .build()
    }
}
