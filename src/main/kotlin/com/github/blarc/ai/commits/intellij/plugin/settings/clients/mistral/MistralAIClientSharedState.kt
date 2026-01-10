package com.github.blarc.ai.commits.intellij.plugin.settings.clients.mistral

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection
import dev.langchain4j.model.mistralai.MistralAiChatModelName

@Service(Service.Level.APP)
@State(name = "MistralAIClientSharedState", storages = [Storage("AICommitsMistralAI.xml")])
class MistralAIClientSharedState : PersistentStateComponent<MistralAIClientSharedState>, LlmClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): MistralAIClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = MistralAiChatModelName.entries.stream()
        .map { it.toString() }
        .toList()
        .toMutableSet()

    override fun getState(): MistralAIClientSharedState = this

    override fun loadState(state: MistralAIClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
