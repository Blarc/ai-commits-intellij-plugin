package com.github.blarc.ai.commits.intellij.plugin.settings.clients.openrouter

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "OpenRouterClientSharedState", storages = [Storage("AICommitsOpenRouter.xml")])
class OpenRouterClientSharedState : PersistentStateComponent<OpenRouterClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): OpenRouterClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("https://openrouter.ai/api/v1")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds = mutableSetOf(
        "nousresearch/hermes-3-llama-3.1-405b:free",
        "anthropic/claude-2:free",
        "google/palm-2-chat-bison:free",
        "meta-llama/llama-2-13b-chat:free",
        "openai/gpt-3.5-turbo:free"
    )

    override fun getState(): OpenRouterClientSharedState = this

    override fun loadState(state: OpenRouterClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}