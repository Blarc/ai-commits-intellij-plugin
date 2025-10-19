package com.github.blarc.ai.commits.intellij.plugin.settings.clients.gigachat

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "GigachatClientSharedState", storages = [Storage("AICommitsGigachat.xml")])
class GigachatClientSharedState : PersistentStateComponent<GigachatClientSharedState>, LLMClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): GigachatClientSharedState = service()

        val MODELS: List<String> = listOf("GigaChat", "GigaChat-2", "GigaChat-Pro", "GigaChat-2-Pro", "GigaChat-Max", "GigaChat-2-Max")

    }

    @XCollection(style = XCollection.Style.v2)
    override val hosts = mutableSetOf("https://gigachat.devices.sberbank.ru/api/v1")

    @XCollection(style = XCollection.Style.v2)
    val authUrls = mutableSetOf("https://ngw.devices.sberbank.ru:9443/api/v2")

    @XCollection(style = XCollection.Style.v2)
    override val modelIds = MODELS.toMutableSet()

    override fun getState(): GigachatClientSharedState = this

    override fun loadState(state: GigachatClientSharedState) {
        // Add all model IDs from enum in case they are not stored in xml
        modelIds += state.modelIds
        hosts += state.hosts
        authUrls += state.authUrls
    }
}