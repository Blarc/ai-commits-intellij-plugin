package com.github.blarc.ai.commits.intellij.plugin.settings.clients.amazonBedrock;

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.util.text.nullize
import dev.langchain4j.model.bedrock.BedrockChatModel
import dev.langchain4j.model.bedrock.BedrockChatRequestParameters
import dev.langchain4j.model.bedrock.BedrockStreamingChatModel
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient
import java.time.Duration

@Service(Service.Level.APP)
class AmazonBedrockClientService(private val cs: CoroutineScope) : LLMClientService<AmazonBedrockClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): AmazonBedrockClientService = service()
    }

    override suspend fun buildChatModel(client: AmazonBedrockClientConfiguration): ChatModel {
        val accessKey = client.accessKeyId.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        val credentials = AwsBasicCredentials.builder()
            .accessKeyId(client.accessKeyId)
            .secretAccessKey(accessKey)
            .build()

        return BedrockChatModel.builder()
            .modelId(client.modelId)
            .region(client.region)
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .client(BedrockRuntimeClient.builder()
                .region(client.region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials)).build())
            .defaultRequestParameters(
                BedrockChatRequestParameters.builder()
                    .topP(client.topP)
                    .topK(client.topK)
                    .temperature(client.temperature.toDouble())
                    .maxOutputTokens(client.maxOutputTokens)
                    .build()
            )
            .build()
    }

    override suspend fun buildStreamingChatModel(client: AmazonBedrockClientConfiguration): StreamingChatModel? {
        val accessKey = client.accessKeyId.nullize(true) ?: retrieveToken(client.id)?.toString(true)
        val credentials = AwsBasicCredentials.builder()
            .accessKeyId(client.accessKeyId)
            .secretAccessKey(accessKey)
            .build()

        return BedrockStreamingChatModel.builder()
            .modelId(client.modelId)
            .region(client.region)
            .timeout(Duration.ofSeconds(client.timeout.toLong()))
            .client(
                BedrockRuntimeAsyncClient.builder()
                .region(client.region)
                .credentialsProvider(StaticCredentialsProvider.create(credentials)).build())
            .defaultRequestParameters(
                BedrockChatRequestParameters.builder()
                    .topP(client.topP)
                    .topK(client.topK)
                    .temperature(client.temperature.toDouble())
                    .maxOutputTokens(client.maxOutputTokens)
                    .build()
            )
            .build()
    }

    fun saveToken(client: AmazonBedrockClientConfiguration, token: String) {
        cs.launch(Dispatchers.Default) {
            try {
                PasswordSafe.instance.setPassword(getCredentialAttributes(client.id), token)
                client.accessKeyIsStored = true
            } catch (e: Exception) {
                sendNotification(Notification.unableToSaveToken(e.message))
            }
        }
    }
}
