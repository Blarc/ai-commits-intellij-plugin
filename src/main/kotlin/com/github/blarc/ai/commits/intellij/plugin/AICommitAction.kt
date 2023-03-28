package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AICommitAction : AnAction(), DumbAware {
    @OptIn(DelicateCoroutinesApi::class)
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val commitWorkflowHandler =
            e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) as AbstractCommitWorkflowHandler<*, *>
        val includedChanges = commitWorkflowHandler.ui.getIncludedChanges()
        val commitMessage = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext)

        runBackgroundableTask(message("action.background"), project) {
            val diff = computeDiff(includedChanges, project)

            if (diff.isBlank()) {
                sendNotification(Notification.emptyDiff())
                return@runBackgroundableTask
            }

            val openAIService = OpenAIService.instance
            GlobalScope.launch(Dispatchers.Main) {
                val generatedCommitMessage = openAIService.generateCommitMessage(diff, 1)
                commitMessage?.setCommitMessage(generatedCommitMessage)
            }
        }
    }
    private fun computeDiff(
        includedChanges: List<Change>,
        project: Project
    ): String {
        val diff = includedChanges.joinToString("\n") {

            // Try to get the virtual file for the change and return an empty string if it doesn't exist
            val file = it.virtualFile ?: return@joinToString ""

            // Try to get the git repository for the file and return an empty string if it doesn't exist
            val repository = GitRepositoryManager.getInstance(project).getRepositoryForFile(file)
                ?: return@joinToString ""

            val diffCommand = GitLineHandler(
                project,
                repository.root,
                GitCommand.DIFF
            )
            diffCommand.addParameters("--cached")
            diffCommand.addParameters(file.path)

            val commandResult = Git.getInstance().runCommand(diffCommand)
            commandResult.outputAsJoinedString
        }
        return diff
    }
}