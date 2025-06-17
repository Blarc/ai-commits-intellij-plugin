package com.github.blarc.ai.commits.intellij.plugin.settings.clients.amazonBedrock;

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "AmazonBedrockClientSharedState", storages = [Storage("AICommitsAmazonBedrock.xml")])
class AmazonBedrockClientSharedState : PersistentStateComponent<AmazonBedrockClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): AmazonBedrockClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("http://localhost:11434/")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf("us.amazon.nova-lite-v1:0")

    override fun getState(): AmazonBedrockClientSharedState = this

    override fun loadState(state: AmazonBedrockClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
