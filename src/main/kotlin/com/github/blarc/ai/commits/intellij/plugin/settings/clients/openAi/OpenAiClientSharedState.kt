package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openAi

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.XCollection
import dev.langchain4j.model.openai.OpenAiChatModelName

@Service(Service.Level.APP)
@State(name = "OpenAiClientSharedState", storages = [Storage("AICommitsOpenAi.xml")])
class OpenAiClientSharedState : PersistentStateComponent<OpenAiClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): OpenAiClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("https://api.openai.com/v1")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds = OpenAiChatModelName.entries.stream()
        .map { it.toString() }
        .toList()
        .toMutableSet()

    override fun getState(): OpenAiClientSharedState = this

    override fun loadState(state: OpenAiClientSharedState) {
        XmlSerializerUtil.copyBean(state, this)
    }
}
