package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiVertex

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "GeminiClientSharedState", storages = [Storage("AICommitsGemini.xml")])
class GeminiVertexClientSharedState : PersistentStateComponent<GeminiVertexClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiVertexClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf(
        "gemini-pro",
        "gemini-ultra"
    )

    override fun getState(): GeminiVertexClientSharedState = this

    override fun loadState(state: GeminiVertexClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
