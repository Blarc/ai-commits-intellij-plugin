package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.util.xmlb.annotations.Attribute
import com.intellij.util.xmlb.annotations.Transient
import javax.swing.Icon

abstract class LLMClientConfiguration(
    @Attribute var displayName: String,
    @Attribute var host: String,
    @Attribute var proxyUrl: String?,
    @Attribute var timeout: Int,
    @Attribute var modelId: String,
    @Attribute var temperature: String,
) : Cloneable, Comparable<LLMClientConfiguration> {
    @get:Transient
    var token: String
        get() = retrieveToken(displayName) ?: ""
        set(token) = saveToken(token)

    abstract fun getIcon(): Icon

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

    private fun saveToken(token: String) {
        try {
            PasswordSafe.instance.setPassword(getCredentialAttributes(displayName), token)
        } catch (e: Exception) {
            sendNotification(Notification.unableToSaveToken(e.message))
        }
    }

    override fun compareTo(other: LLMClientConfiguration): Int {
        return displayName.compareTo(other.displayName)
    }

}
