package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.text.nullize
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.time.Duration

@Service(Service.Level.APP)
class OpenAiClientService(private val cs: CoroutineScope) : LLMClientService<OpenAiClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): OpenAiClientService = service()
    }

    override suspend fun buildChatModel(client: OpenAiClientConfiguration): ChatLanguageModel {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        val builder = OpenAiChatModel.builder()
            .apiKey(token ?: "")
            .modelName(client.modelId)
            .temperature(client.temperature.toDouble())
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .baseUrl(client.host)

        client.proxyUrl?.takeIf { it.isNotBlank() }?.let {
            val uri = URI(it)
            builder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(uri.host, uri.port)))
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
