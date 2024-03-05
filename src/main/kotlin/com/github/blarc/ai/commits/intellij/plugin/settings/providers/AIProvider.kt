package com.github.blarc.ai.commits.intellij.plugin.settings.providers

import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe

interface AIProvider {

    var host: String
    var hosts: Set<String>
    var proxyUrl: String?
    var timeout: Int
    var modelId: String
    var modelIds: List<String>
    var temperature: String
    var token: String
        get() { return getToken() }
        set(token) { saveToken(token) }

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

    private fun getToken(): String {
        val credentials: Credentials? = PasswordSafe.instance.get(getCredentialAttributes(displayName()))
        return credentials?.getPasswordAsString() ?: throw Exception("${displayName()} token is not set.")
    }

    private fun getCredentialAttributes(title: String): CredentialAttributes {
        return CredentialAttributes(
            title,
            null,
            this.javaClass,
            false
        )
    }
}
