package com.github.blarc.ai.commits.intellij.plugin.settings.clients.huggingface

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection
import dev.langchain4j.model.huggingface.HuggingFaceModelName

@Service(Service.Level.APP)
@State(name = "HuggingFaceClientSharedState", storages = [Storage("AICommitsHuggingFace.xml")])
class HuggingFaceClientSharedState : PersistentStateComponent<HuggingFaceClientSharedState>, LlmClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): HuggingFaceClientSharedState = service()
    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts: MutableSet<String> = mutableSetOf()

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf(
        HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT
    )

    override fun getState(): HuggingFaceClientSharedState = this

    override fun loadState(state: HuggingFaceClientSharedState) {
        // Add all model IDs from enum in case they are not stored in xml
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
