package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.openapi.vcs.changes.Change
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.StringWriter

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

            if (commitMessage == null) {
                sendNotification(Notification.noCommitMessage())
                return@runBackgroundableTask
            }

            val openAIService = OpenAIService.instance
            val prompt = AppSettings.instance.getPrompt(diff)
            runBlocking(Dispatchers.Main) {
                try {
                    val generatedCommitMessage = openAIService.generateCommitMessage(prompt, 1)
                    commitMessage.setCommitMessage(generatedCommitMessage)
                    AppSettings.instance.recordHit()
                }
                catch (e: Exception) {
                    commitMessage.setCommitMessage(message("action.error"))
                    sendNotification(Notification.unsuccessfulRequest(e.message ?: message("action.unknown-error")))
                }
            }
        }
    }

    private fun computeDiff(
        includedChanges: List<Change>,
        project: Project
    ): String {

        val gitRepositoryManager = GitRepositoryManager.getInstance(project)

        // go through included changes, create a map of repository to changes and discard nulls
        val changesByRepository = includedChanges
            .mapNotNull { change ->
                change.virtualFile?.let { file ->
                    gitRepositoryManager.getRepositoryForFileQuick(
                        file
                    ) to change
                }
            }
            .groupBy({ it.first }, { it.second })


        // compute diff for each repository
        return changesByRepository
            .map { (repository, changes) ->
                repository?.let {
                    val filePatches = IdeaTextPatchBuilder.buildPatch(
                        project,
                        changes,
                        repository.root.toNioPath(), false, true
                    )

                    val stringWriter = StringWriter()
                    stringWriter.write("Repository: ${repository.root.path}\n")
                    UnifiedDiffWriter.write(project, filePatches, stringWriter, "\n", null)
                    stringWriter.toString()
                }
            }
            .joinToString("\n")
    }
}