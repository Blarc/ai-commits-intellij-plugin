package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.commonBranch
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.computeDiff
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.constructPrompt
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.isPromptTooLarge
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
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
        val commitMessage = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext) as CommitMessage?

        runBackgroundableTask(message("action.background"), project) {
            val diff = computeDiff(includedChanges, false, project)
            if (diff.isBlank()) {
                sendNotification(Notification.emptyDiff())
                return@runBackgroundableTask
            }

            val branch = commonBranch(includedChanges, project)
            val hint = commitMessage?.text
            val prompt = constructPrompt(AppSettings2.instance.activePrompt.content, diff, branch, hint)
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
                    val generatedCommitMessage = openAIService.generateCommitMessage(prompt)
                    commitMessage.setCommitMessage(generatedCommitMessage)
                    AppSettings2.instance.recordHit()
                } catch (e: Exception) {
                    commitMessage.setCommitMessage(e.message ?: message("action.error"))
                    sendNotification(Notification.unsuccessfulRequest(e.message ?: message("action.unknown-error")))
                }
            }
        }
    }
}
