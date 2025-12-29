package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

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
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration

@Service(Service.Level.APP)
class OpenAiClientService(private val cs: CoroutineScope) : LlmClientService<OpenAiClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): OpenAiClientService = service()
    }

    override suspend fun buildChatModel(client: OpenAiClientConfiguration): ChatModel {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        val builder = OpenAiChatModel.builder()
            .apiKey(token ?: "")
            .modelName(client.modelId)
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .topP(client.topP)
            .baseUrl(client.host)

        client.temperature.takeIf { it.isNotBlank() }?.let {
            builder.temperature(it.toDouble())
        }

        client.organizationId?.takeIf { it.isNotBlank() }?.let {
            builder.organizationId(it)
        }

        return builder.build()
    }

    override suspend fun buildStreamingChatModel(client: OpenAiClientConfiguration): StreamingChatModel {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        val builder = OpenAiStreamingChatModel.builder()
            .apiKey(token ?: "")
            .modelName(client.modelId)
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .topP(client.topP)
            .baseUrl(client.host)

        client.temperature.takeIf { it.isNotBlank() }?.let {
            builder.temperature(it.toDouble())
        }

        client.organizationId?.takeIf { it.isNotBlank() }?.let {
            builder.organizationId(it)
        }

        return builder.build()
    }

    fun saveToken(client: OpenAiClientConfiguration, token: String) {
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
