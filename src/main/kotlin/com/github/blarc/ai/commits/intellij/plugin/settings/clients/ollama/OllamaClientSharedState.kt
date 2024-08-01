package com.github.blarc.ai.commits.intellij.plugin.settings.clients.ollama

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "OllamaClientSharedState", storages = [Storage("AICommitsOllama.xml")])
class OllamaClientSharedState : PersistentStateComponent<OllamaClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): OllamaClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf("llama3")

    override fun getState(): OllamaClientSharedState = this

    override fun loadState(state: OllamaClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
