package com.github.blarc.ai.commits.intellij.plugin.settings.providers

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.aallam.openai.client.ProxyConfig
import kotlin.time.Duration.Companion.seconds

data class OpenAIClient(
    override var host: String = OpenAIHost.OpenAI.baseUrl,
    override var hosts: MutableSet<String> = mutableSetOf(OpenAIHost.OpenAI.baseUrl),
    override var proxyUrl: String? = null,
    override var timeout: Int = 30,
    override var modelId: String = "gpt-3.5-turbo",
    override var modelIds: List<String> =  listOf("gpt-3.5-turbo", "gpt-4"),
    override var temperature: String = "0.7"
) : LLMClient {

    companion object {
        val instance = OpenAIClient()
    }

    override fun displayName() = "OpenAI"

    override suspend fun generateCommitMessage(
        prompt: String
    ): String {

        val openAI = OpenAI(openAIConfig())
        val chatCompletionRequest = ChatCompletionRequest(
            ModelId(modelId),
            listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            ),
            temperature = temperature.toDouble(),
            topP = 1.0,
            frequencyPenalty = 0.0,
            presencePenalty = 0.0,
            maxTokens = 200,
            n = 1
        )

        val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)
        return completion.choices[0].message.content ?: "API returned an empty response."
    }

    override suspend fun refreshModels() {
        val openAI = OpenAI(openAIConfig())
        modelIds = openAI.models().map { it.id.id }
    }

    @Throws(Exception::class)
    override suspend fun verifyConfiguration(
        newHost: String,
        newProxy: String?,
        newTimeout: String,
        newToken: String
    ) {

        val newConfig = OpenAIConfig(
            newToken,
            host = newHost.takeIf { it.isNotBlank() }?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
            proxy = newProxy?.takeIf { it.isNotBlank() }?.let { ProxyConfig.Http(it) },
            timeout = Timeout(socket = newTimeout.toInt().seconds)
        )
        val openAI = OpenAI(newConfig)
        openAI.models()
    }

    private fun openAIConfig() = OpenAIConfig(
        token,
        host = host.takeIf { it.isNotBlank() }?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
        proxy = proxyUrl?.takeIf { it.isNotBlank() }?.let { ProxyConfig.Http(it) },
        timeout = Timeout(socket = timeout.seconds)
    )
}
