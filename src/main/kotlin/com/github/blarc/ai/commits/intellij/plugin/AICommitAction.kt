package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.commonBranch
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.computeDiff
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.constructPrompt
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.isPromptTooLarge
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.ModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AICommitAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val commitWorkflowHandler = e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) as AbstractCommitWorkflowHandler<*, *>?
        if (commitWorkflowHandler == null) {
            sendNotification(Notification.noCommitMessage())
            return
        }

        val includedChanges = commitWorkflowHandler.ui.getIncludedChanges()
        val commitMessage = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext)

        runBackgroundableTask(message("action.background"), project) {
            val diff = computeDiff(includedChanges, project)
            if (diff.isBlank()) {
                sendNotification(Notification.emptyDiff())
                return@runBackgroundableTask
            }

            val branch = commonBranch(includedChanges, project)
            val prompt = constructPrompt(AppSettings.instance.currentPrompt.content, diff, branch)
            if (isPromptTooLarge(prompt)) {
                sendNotification(Notification.promptTooLarge())
                return@runBackgroundableTask
            }

            if (commitMessage == null) {
                sendNotification(Notification.noCommitMessage())
                return@runBackgroundableTask
            }

            val openAIService = OpenAIService.instance
            runBlocking(Dispatchers.Main) {
                try {
                    val generatedCommitMessage = openAIService.generateCommitMessage(prompt, 1)
                    commitMessage.setCommitMessage(generatedCommitMessage)
                    AppSettings.instance.recordHit()
                } catch (e: Exception) {
                    commitMessage.setCommitMessage(e.message ?: message("action.error"))
                    sendNotification(Notification.unsuccessfulRequest(e.message ?: message("action.unknown-error")))
                }
            }
        }
    }
}
