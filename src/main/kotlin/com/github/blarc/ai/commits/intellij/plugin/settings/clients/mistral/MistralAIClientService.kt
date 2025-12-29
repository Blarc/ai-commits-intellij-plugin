package com.github.blarc.ai.commits.intellij.plugin.settings.clients.mistral;

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientService
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.text.nullize
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.mistralai.MistralAiChatModel
import dev.langchain4j.model.mistralai.MistralAiModels
import dev.langchain4j.model.mistralai.MistralAiStreamingChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Service(Service.Level.APP)
class MistralAIClientService(private val cs: CoroutineScope) : LlmClientService<MistralAIClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): MistralAIClientService = service()
    }

    override suspend fun getAvailableModels(client: MistralAIClientConfiguration): List<String> {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        val mistralAiModels = MistralAiModels.builder()
            .apiKey(token)
            .build()

        val availableModels = withContext(Dispatchers.IO) {
            mistralAiModels.availableModels().content()
        }
        return availableModels.map { it.id }
    }

    override suspend fun buildChatModel(client: MistralAIClientConfiguration): ChatModel {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        return MistralAiChatModel.builder()
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .maxTokens(client.maxTokens)
            .topP(client.topP)
            .apiKey(token)
            .build()
    }

    override suspend fun buildStreamingChatModel(client: MistralAIClientConfiguration): StreamingChatModel? {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        return MistralAiStreamingChatModel.builder()
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .maxTokens(client.maxTokens)
            .topP(client.topP)
            .apiKey(token)
            .build()
    }

    fun saveToken(client: MistralAIClientConfiguration, token: String) {
        cs.launch(Dispatchers.Default) {
            try {
                PasswordSafe.instance.setPassword(getCredentialAttributes(client.id), token)
                client.tokenIsStored = true
            } catch (e: Exception) {
                sendNotification(Notification.unableToSaveToken(e.message))
            }
        }
    }
}
