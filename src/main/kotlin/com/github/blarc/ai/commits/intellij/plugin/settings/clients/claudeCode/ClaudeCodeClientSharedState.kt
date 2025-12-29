package com.github.blarc.ai.commits.intellij.plugin.settings.clients.claudeCode

import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmClientSharedState
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.annotations.XCollection

@Service(Service.Level.APP)
@State(name = "ClaudeCodeClientSharedState", storages = [Storage("AICommitsClaudeCode.xml")])
class ClaudeCodeClientSharedState : PersistentStateComponent<ClaudeCodeClientSharedState>, LlmClientSharedState {

    companion object {
        @JvmStatic
        fun getInstance(): ClaudeCodeClientSharedState = service()
    }

    // Claude Code CLI doesn't have host configuration
    @XCollection(style = XCollection.Style.v2)
    override val hosts: MutableSet<String> = mutableSetOf()

    // Common model aliases for Claude Code CLI
    @XCollection(style = XCollection.Style.v2)
    override val modelIds: MutableSet<String> = mutableSetOf(
        "",  // Empty = use CLI default
        "sonnet",
        "opus",
        "haiku"
    )

    override fun getState(): ClaudeCodeClientSharedState = this

    override fun loadState(state: ClaudeCodeClientSharedState) {
        modelIds += state.modelIds
        hosts += state.hosts
    }
}