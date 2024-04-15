package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import dev.langchain4j.model.openai.OpenAiChatModelName

@Service
@State(name = "OpenAiClientService", storages = [Storage("AICommitsOpenAi.xml")])
class OpenAiClientService : PersistentStateComponent<OpenAiClientService> {

    val hosts = mutableSetOf("https://api.openai.com/v1")
    val modelIds = OpenAiChatModelName.entries.stream()
        .map { it.toString() }
        .toList()
        .toMutableSet()

    override fun getState(): OpenAiClientService = this

    override fun loadState(state: OpenAiClientService) {
        XmlSerializerUtil.copyBean(state, this)
    }

}
