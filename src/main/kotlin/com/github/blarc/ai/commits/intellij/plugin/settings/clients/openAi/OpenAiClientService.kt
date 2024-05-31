package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import kotlinx.coroutines.CoroutineScope
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.time.Duration

@Service(Service.Level.APP)
class OpenAiClientService(cs: CoroutineScope) : LLMClientService<OpenAiClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): OpenAiClientService = service()
    }

    override fun buildChatModel(client: OpenAiClientConfiguration): ChatLanguageModel {
        val builder = OpenAiChatModel.builder()
            .apiKey(client.token)
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


}
