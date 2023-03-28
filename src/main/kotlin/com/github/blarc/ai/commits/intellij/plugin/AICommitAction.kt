package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
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
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.StringWriter

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