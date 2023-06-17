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
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.ModelType
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

            var branch = commonBranch(includedChanges, project)
            if (branch == null) {
                sendNotification(Notification.noCommonBranch())
                // hardcoded fallback branch
                branch = "main"
            }

            val prompt = AppSettings.instance.getPrompt(diff, branch)
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

    private fun computeDiff(
            includedChanges: List<Change>,
            project: Project
    ): String {

        val gitRepositoryManager = GitRepositoryManager.getInstance(project)

        // go through included changes, create a map of repository to changes and discard nulls
        val changesByRepository = includedChanges
                .filter {
                    it.virtualFile?.path?.let { path ->
                        AICommitsUtils.isPathExcluded(path, project)
                    } ?: false
                }
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

    private fun isPromptTooLarge(prompt: String): Boolean {
        val registry = Encodings.newDefaultEncodingRegistry()

        /*
         * Try to find the model type based on the model id by finding the longest matching model type
         * If no model type matches, let the request go through and let the OpenAI API handle it
         */
        val modelType = ModelType.values()
                .filter { AppSettings.instance.openAIModelId.contains(it.name) }
                .maxByOrNull { it.name.length }
                ?: return false

        val encoding = registry.getEncoding(modelType.encodingType)
        return encoding.countTokens(prompt) > modelType.maxContextLength
    }

    private fun commonBranch(changes: List<Change>, project: Project): String? {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        return changes.map {
            repositoryManager.getRepositoryForFileQuick(it.virtualFile)?.currentBranchName
        }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
    }
}