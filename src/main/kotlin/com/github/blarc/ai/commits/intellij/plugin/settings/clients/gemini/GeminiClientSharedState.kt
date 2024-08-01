package com.github.blarc.ai.commits.intellij.plugin.settings.clients.gemini

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "GeminiClientSharedState", storages = [Storage("AICommitsGemini.xml")])
class GeminiClientSharedState : PersistentStateComponent<GeminiClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf(
        "gemini-pro",
        "gemini-ultra"
    )

    override fun getState(): GeminiClientSharedState = this

    override fun loadState(state: GeminiClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
