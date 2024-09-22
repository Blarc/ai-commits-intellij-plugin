package com.github.blarc.ai.commits.intellij.plugin.settings.clients.azureOpenAi;

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.text.nullize
import dev.langchain4j.model.azure.AzureOpenAiChatModel
import dev.langchain4j.model.chat.ChatLanguageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration


@Service(Service.Level.APP)
class AzureOpenAiClientService(private val cs: CoroutineScope) : LLMClientService<AzureOpenAiClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): AzureOpenAiClientService = service()
    }

    override suspend fun buildChatModel(client: AzureOpenAiClientConfiguration): ChatLanguageModel {
        val token = client.token.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        return AzureOpenAiChatModel.builder()
            .deploymentName(client.modelId)
            .temperature(client.temperature.toDouble())
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .endpoint(client.host)
            .apiKey(token ?: "")
            .build()
    }

    fun saveToken(client: AzureOpenAiClientConfiguration, token: String) {
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
