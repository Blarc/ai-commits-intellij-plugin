package com.github.blarc.ai.commits.intellij.plugin.settings.clients

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
import com.intellij.util.xmlb.annotations.Attribute
import kotlin.time.Duration.Companion.seconds

class OpenAIClient(displayName: String = "OpenAI") : LLMClient(
    displayName,
    OpenAIHost.OpenAI.baseUrl,
    null,
    30,
    "gpt-3.5-turbo",
    "0.7"
) {

    companion object {
        // TODO @Blarc: Static fields probably can't be attributes...
        @Attribute
        val hosts = mutableSetOf(OpenAIHost.OpenAI.baseUrl)
        @Attribute
        val modelIds = mutableSetOf("gpt-3.5-turbo", "gpt-4")
    }

    override fun getHosts(): Set<String> {
        return hosts
    }

    override fun getModelIds(): Set<String> {
        return modelIds
    }

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
        openAI.models()
            .map { it.id.id }
            .forEach { modelIds.add(it) }
    }

    override fun clone(): LLMClient {
        val copy = OpenAIClient(displayName)
        copy.host = host
        copy.proxyUrl = proxyUrl
        copy.timeout = timeout
        copy.modelId = modelId
        copy.temperature = temperature
        return copy
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

    override fun panel(): LLMClientPanel {
        return OpenAIClientPanel(this)
    }

    private fun openAIConfig() = OpenAIConfig(
        token,
        host = host.takeIf { it.isNotBlank() }?.let { OpenAIHost(it) } ?: OpenAIHost.OpenAI,
        proxy = proxyUrl?.takeIf { it.isNotBlank() }?.let { ProxyConfig.Http(it) },
        timeout = Timeout(socket = timeout.seconds)
    )
}
