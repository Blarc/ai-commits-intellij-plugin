package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName
import kotlinx.coroutines.CoroutineScope
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.time.Duration

@Service(Service.Level.APP)
@State(name = "OpenAiClientService", storages = [Storage("AICommitsOpenAi.xml")])
class OpenAiClientService(@Transient private val cs: CoroutineScope?) :
    PersistentStateComponent<OpenAiClientService>,
    LLMClientService<OpenAiClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): OpenAiClientService = service()
    }

    @XCollection(style = XCollection.Style.v2)
    val hosts = mutableSetOf("https://api.openai.com/v1")

    @XCollection(style = XCollection.Style.v2)
    val modelIds = OpenAiChatModelName.entries.stream()
        .map { it.toString() }
        .toList()
        .toMutableSet()

    override fun getState(): OpenAiClientService = this

    override fun loadState(state: OpenAiClientService) {
        XmlSerializerUtil.copyBean(state, this)
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
        return builder.build()
    }


}
