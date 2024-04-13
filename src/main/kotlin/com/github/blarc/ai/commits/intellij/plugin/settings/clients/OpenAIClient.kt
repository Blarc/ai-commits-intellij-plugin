package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.Icons
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.intellij.util.xmlb.annotations.Attribute
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.time.Duration
import javax.swing.Icon

class OpenAIClient(displayName: String = "OpenAI") : LLMClient(
    displayName,
    "https://api.openai.com/v1",
    null,
    30,
    "gpt-3.5-turbo",
    "0.7"
) {

    companion object {
        // TODO @Blarc: Static fields probably can't be attributes...
        @Attribute
        val hosts = mutableSetOf("https://api.openai.com/v1")
        @Attribute
        val modelIds = OpenAiChatModelName.entries.stream()
            .map { it.toString() }
            .toList()
            .toMutableSet()
    }

    override fun getIcon(): Icon {
        return Icons.OPEN_AI
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
        val openAI = OpenAiChatModel.builder()
            .apiKey(token)
            .modelName(modelId)
            .temperature(temperature.toDouble())
            .timeout(Duration.ofSeconds(timeout.toLong()))
            .baseUrl(AppSettings.instance.openAIHost)
            .build()

        val response = openAI.generate(
            listOf(
                UserMessage.from(
                    "user",
                    prompt
                )
            )
        )
        return response.content().text()
    }

    override fun getRefreshModelFunction(): (suspend () -> Unit)? {
        // Model names are retrieved from Enum and do not need to be refreshed.
        return null
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

    override suspend fun verifyConfiguration(
        newHost: String,
        newProxy: String?,
        newTimeout: String,
        newModelId: String,
        newToken: String
    ) {

        val openAiBuilder = OpenAiChatModel.builder()
            .apiKey(newToken)
            .modelName(newModelId)
            .temperature(temperature.toDouble())
            .timeout(Duration.ofSeconds(newTimeout.toLong()))

        newHost.takeIf { it.isNotBlank() }?.let { openAiBuilder.baseUrl(it) }
        newProxy?.takeIf { it.isNotBlank() }?.let {
            val uri = URI(it)
            openAiBuilder.proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(uri.host, uri.port)))
        }

        val openAi = openAiBuilder.build()
        openAi.generate(
            listOf(
                UserMessage.from(
                    "user",
                    "t"
                )
            )
        )
    }

    override fun panel(): LLMClientPanel {
        return OpenAIClientPanel(this)
    }
}
