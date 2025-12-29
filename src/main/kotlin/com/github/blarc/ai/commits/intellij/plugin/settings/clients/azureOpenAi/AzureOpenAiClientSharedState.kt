package com.github.blarc.ai.commits.intellij.plugin.settings.clients.azureOpenAi;

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "AzureOpenAiClientSharedState", storages = [Storage("AICommitsAzureOpenAi.xml")])
class AzureOpenAiClientSharedState : PersistentStateComponent<AzureOpenAiClientSharedState>, LlmClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): AzureOpenAiClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts: MutableSet<String> = mutableSetOf()

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf()

    override fun getState(): AzureOpenAiClientSharedState = this

    override fun loadState(state: AzureOpenAiClientSharedState) {
        // Add all model IDs from enum in case they are not stored in xml
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
