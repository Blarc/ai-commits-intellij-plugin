package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import java.util.*
import javax.swing.Icon

abstract class LLMClientConfiguration(
    @Attribute var name: String,
    @Attribute var host: String,
    @Attribute var proxyUrl: String?,
    @Attribute var timeout: Int,
    @Attribute var modelId: String,
    @Attribute var temperature: String,
) : Cloneable, Comparable<LLMClientConfiguration> {

    @Attribute
    var id: String = UUID.randomUUID().toString()

    abstract fun getClientName(): String

    abstract fun getClientIcon(): Icon

    abstract fun getSharedState(): LLMClientSharedState

    fun getHosts(): Set<String> {
        return getSharedState().hosts
    }

    fun getModelIds(): Set<String> {
        return getSharedState().modelIds
    }

    fun addHost(host: String) {
        getSharedState().hosts.add(host)
    }

    fun addModelId(modelId: String) {
        getSharedState().modelIds.add(modelId)
    }

    abstract fun generateCommitMessage(prompt: String, commitMessage: CommitMessage)

    abstract fun getRefreshModelsFunction(): ((ComboBox<String>) -> Unit)?

    public abstract override fun clone(): LLMClientConfiguration

    abstract fun panel(): LLMClientPanel

    override fun compareTo(other: LLMClientConfiguration): Int {
        return name.compareTo(other.name)
    }

    override fun equals(other: Any?): Boolean {
        return other is LLMClientConfiguration && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
