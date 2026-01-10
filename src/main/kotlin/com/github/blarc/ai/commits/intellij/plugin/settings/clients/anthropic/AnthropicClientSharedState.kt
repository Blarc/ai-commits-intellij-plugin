package com.github.blarc.ai.commits.intellij.plugin.settings.clients.anthropic

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection
import dev.langchain4j.model.anthropic.AnthropicChatModelName

@Service(Service.Level.APP)
@State(name = "AnthropicClientSharedState", storages = [Storage("AICommitsAnthropic.xml")])
class AnthropicClientSharedState : PersistentStateComponent<AnthropicClientSharedState>, LlmClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): AnthropicClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("https://api.anthropic.com/v1/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = AnthropicChatModelName.entries.stream()
        .map { it.toString() }
        .toList()
        .toMutableSet()

    override fun getState(): AnthropicClientSharedState = this

    override fun loadState(state: AnthropicClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
