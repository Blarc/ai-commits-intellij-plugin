package com.github.blarc.ai.commits.intellij.plugin.notifications

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.openPluginSettings
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.ide.browsers.BrowserLauncher
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.net.URI

data class Notification(
    val title: String? = null,
    val message: String,
    val actions: Set<NotificationAction> = setOf(),
    val type: Type = Type.PERSISTENT
) {
    enum class Type {
        PERSISTENT,
        TRANSIENT
    }

    companion object {
        private val DEFAULT_TITLE = message("notifications.title")

        fun welcome(version: String) = Notification(message = message("notifications.welcome", version), type = Type.TRANSIENT)

        fun star() = Notification(
            message = """
                Finding AI Commit useful? Show your support 💖 and ⭐ the repository 🙏.
            """.trimIndent(),
            actions = setOf(
                NotificationAction.openRepository() {
                    service<AppSettings2>().requestSupport = false;
                },
                NotificationAction.doNotAskAgain() {
                    service<AppSettings2>().requestSupport = false;
                }
            )
        )

        fun emptyDiff() = Notification(DEFAULT_TITLE, message = message("notifications.empty-diff"))
        fun promptTooLarge() = Notification(DEFAULT_TITLE, message = message("notifications.prompt-too-large"))
        fun unsuccessfulRequest(message: String) = Notification(message = message("notifications.unsuccessful-request", message))
        fun noCommitMessage() = Notification(message = message("notifications.no-commit-message"))
        fun unableToSaveToken(message: String?) = Notification(message = message("notifications.unable-to-save-token", message ?: "Unknown error"))
        fun noCommonBranch() = Notification(message = message("notifications.no-common-branch"))

    }
}

data class NotificationAction(val title: String, val run: (dismiss: () -> Unit) -> Unit) {
    companion object {
        fun settings(project: Project, title: String = message("settings.title")) = NotificationAction(title) { dismiss ->
            dismiss()
            openPluginSettings(project)
        }

        fun openRepository(onComplete: () -> Unit) = NotificationAction(message("actions.sure-take-me-there")) { dismiss ->
            AICommitsBundle.openRepository()
            dismiss()
            onComplete()
        }

        fun doNotAskAgain(onComplete: () -> Unit) = NotificationAction(message("actions.do-not-ask-again")) { dismiss ->
            dismiss()
            onComplete()
        }

        fun openUrl(url: URI, title: String = message("actions.take-me-there")) = NotificationAction(title) { dismiss ->
            dismiss()
            BrowserLauncher.instance.open(url.toString());
        }
    }
}
