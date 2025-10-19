package com.github.blarc.ai.commits.intellij.plugin.settings.clients.gigachat

import chat.giga.client.GigaChatClientImpl
import chat.giga.client.auth.AuthClient
import chat.giga.client.auth.AuthClientBuilder
import chat.giga.client.auth.AuthClientBuilder.OAuthBuilder
import chat.giga.langchain4j.GigaChatChatModel
import chat.giga.langchain4j.GigaChatChatRequestParameters
import chat.giga.langchain4j.GigaChatStreamingChatModel
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Service(Service.Level.APP)
class GigachatClientService(private val cs: CoroutineScope) : LLMClientService<GigachatClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): GigachatClientService = service()
    }

    override suspend fun buildChatModel(client: GigachatClientConfiguration): ChatModel {
        return GigaChatChatModel.builder()
            .apiUrl(client.apiUrl)
            .defaultChatRequestParameters(
                GigaChatChatRequestParameters.builder()
                    .modelName(client.modelId)
                    .build()
            )
            .authClient(
                AuthClient.builder()
                    .withOAuth(
                        OAuthBuilder.builder()
                            .scope(client.scope)
                            .authKey(client.token)
                            .authApiUrl(client.authUrl)
                            .verifySslCerts(false)
                            .build()
                    )
                    .build()
            )
            .logRequests(true)
            .logResponses(true)
            .build()
    }

    override suspend fun buildStreamingChatModel(client: GigachatClientConfiguration): StreamingChatModel? {
        return GigaChatStreamingChatModel.builder()
            .apiUrl(client.apiUrl)
            .defaultChatRequestParameters(
                GigaChatChatRequestParameters.builder()
                    .modelName(client.modelId)
                    .temperature(client.temperature.toDouble())
                    .build()
            )
            .authClient(
                AuthClient.builder()
                    .withOAuth(
                        OAuthBuilder.builder()
                            .scope(client.scope)
                            .authKey(client.token)
                            .authApiUrl(client.authUrl)
                            .build()
                    )
                    .build()
            )
            .logRequests(true)
            .logResponses(true)
            .build()
    }

    fun saveToken(client: GigachatClientConfiguration, token: String) {
        cs.launch(Dispatchers.Default) {
            try {
                PasswordSafe.instance.setPassword(getCredentialAttributes(client.id), token)
                client.tokenIsStored = true
            } catch (e: Exception) {
                sendNotification(Notification.unableToSaveToken(e.message))
            }
        }
    }

    override suspend fun getAvailableModels(client: GigachatClientConfiguration): List<String> {
        val gigaChatClient1 = GigaChatClientImpl.builder()
            .apiUrl(client.apiUrl)
            .authClient(
                AuthClientBuilder.builder()
                    .withOAuth(
                        OAuthBuilder.builder()
                            .scope(client.scope)
                            .authKey(client.token)
                            .authApiUrl(client.authUrl)
                            .build()
                    )
                    .build()
            )
            .build()

        val availableModels = withContext(Dispatchers.IO) {
            gigaChatClient1.models().data()
        }
        return availableModels
            .filter { it.type().equals("chat") }
            .map { it.id() }
    }
}