package com.github.blarc.ai.commits.intellij.plugin.settings.clients.codexCli

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "CodexCliClientSharedState", storages = [Storage("AICommitsCodexCli.xml")])
class CodexCliClientSharedState : PersistentStateComponent<CodexCliClientSharedState>, LlmClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): CodexCliClientSharedState = service()
    }

    // Codex CLI doesn't have host configuration.
    @XCollection(style = XCollection.Style.v2)
    override val hosts: MutableSet<String> = mutableSetOf()

    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf(
        "gpt-5.2-codex",
        "gpt-5.1-codex-max",
        "gpt-5.1-codex-mini",
        "gpt-5.2"
    )

    override fun getState(): CodexCliClientSharedState = this

    override fun loadState(state: CodexCliClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}
