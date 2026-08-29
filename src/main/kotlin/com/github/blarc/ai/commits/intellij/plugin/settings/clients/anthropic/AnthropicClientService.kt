package com.github.blarc.ai.commits.intellij.plugin.settings.clients.anthropic

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientService
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.text.nullize
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration

@Service(Service.Level.APP)
class AnthropicClientService(private val cs: CoroutineScope) : LlmClientService<AnthropicClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): AnthropicClientService = service()
    }

    override suspend fun buildChatModel(client: AnthropicClientConfiguration): ChatModel {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        val builder = AnthropicChatModel.builder()
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .apiKey(token ?: "")
            .baseUrl(client.host)
            .topP(client.topP)
            .topK(client.topK)
            .timeout(Duration.ofSeconds(client.timeout.toLong()))

        client.version?.takeIf { it.isNotBlank() }?.let {
            builder.version(it)
        }

        client.beta?.takeIf { it.isNotBlank() }?.let {
            builder.beta(it)
        }

        return builder.build()

    }

    override suspend fun buildStreamingChatModel(client: AnthropicClientConfiguration): StreamingChatModel {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        val builder = AnthropicStreamingChatModel.builder()
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .apiKey(token ?: "")
            .baseUrl(client.host)
            .topP(client.topP)
            .topK(client.topK)
            .timeout(Duration.ofSeconds(client.timeout.toLong()))

        client.version?.takeIf { it.isNotBlank() }?.let {
            builder.version(it)
        }

        client.beta?.takeIf { it.isNotBlank() }?.let {
            builder.beta(it)
        }

        return builder.build()
    }

    fun saveToken(client: AnthropicClientConfiguration, token: String) {
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
