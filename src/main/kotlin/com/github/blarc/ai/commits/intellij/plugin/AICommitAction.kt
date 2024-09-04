package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.commonBranch
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.computeDiff
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.constructPrompt
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.ui.CommitMessage
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler

class AICommitAction : AnAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        val llmClient = AppSettings2.instance.getActiveLLMClientConfiguration()
        if (llmClient == null) {
            Notification.clientNotSet()
            return
        }
        val project = e.project ?: return

        val commitWorkflowHandler = e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) as AbstractCommitWorkflowHandler<*, *>?
        if (commitWorkflowHandler == null) {
            sendNotification(Notification.noCommitMessage())
            return
        }

        val includedChanges = commitWorkflowHandler.ui.getIncludedChanges()
        val commitMessage = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext) as CommitMessage?

        val diff = computeDiff(includedChanges, false, project)
        if (diff.isBlank()) {
            sendNotification(Notification.emptyDiff())
            return
        }

        val branch = commonBranch(includedChanges, project)
        val hint = commitMessage?.text

        val prompt = constructPrompt(AppSettings2.instance.activePrompt.content, diff, branch, hint, project)

        // TODO @Blarc: add support for different clients
//            if (isPromptTooLarge(prompt)) {
//                sendNotification(Notification.promptTooLarge())
//                return@runBackgroundableTask
//            }

        if (commitMessage == null) {
            sendNotification(Notification.noCommitMessage())
            return
        }

        llmClient.generateCommitMessage(prompt, project, commitMessage)
    }
}
