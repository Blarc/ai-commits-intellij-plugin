package com.github.blarc.ai.commits.intellij.plugin.settings.providers

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCredentialAttributes
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.retrieveToken
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.intellij.ide.passwordSafe.PasswordSafe

interface LLMClient {

    var host: String
    var hosts: MutableSet<String>
    var proxyUrl: String?
    var timeout: Int
    var modelId: String
    var modelIds: List<String>
    var temperature: String
    var token: String
        get() = retrieveToken(displayName()) ?: ""
        set(token) = saveToken(token)

    fun displayName(): String

    suspend fun generateCommitMessage(prompt: String): String

    suspend fun refreshModels()

    @Throws(Exception::class)
    suspend fun verifyConfiguration(
        newHost: String,
        newProxy: String?,
        newTimeout: String,
        newToken: String
    )

    private fun saveToken(token: String) {
        try {
            PasswordSafe.instance.setPassword(getCredentialAttributes(displayName()), token)
        } catch (e: Exception) {
            sendNotification(Notification.unableToSaveToken())
        }
    }
}
