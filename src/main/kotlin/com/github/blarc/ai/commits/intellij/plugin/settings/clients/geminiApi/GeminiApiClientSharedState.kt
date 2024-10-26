package com.github.blarc.ai.commits.intellij.plugin.settings.clients.geminiApi

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "GeminiApiClientSharedState", storages = [Storage("AICommitsGeminiApi.xml")])
class GeminiApiClientSharedState : PersistentStateComponent<GeminiApiClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): GeminiApiClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf(
        "gemini-1.5-pro-latest",
        "gemini-1.5-pro",
        "gemini-1.5-pro-001",
        "gemini-1.5-pro-002",
        "gemini-1.5-flash-latest",
        "gemini-1.5-flash",
        "gemini-1.5-flash-001",
        "gemini-1.5-flash-002",
        "gemini-1.5-flash-8b-latest",
        "gemini-1.5-flash-8b",
        "gemini-1.5-flash-8b-001",
        "gemini-1.5-flash-8b-exp-0924",
        "gemini-1.5-flash-8b-exp-0827",
        "gemini-1.5-flash-exp-0827"
    )

    override fun getState(): GeminiApiClientSharedState = this

    override fun loadState(state: GeminiApiClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}