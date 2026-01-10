package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.computeDiff
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.constructPrompt
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCommonBranch
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.ProjectSettings
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.isAmendCommitMode
import git4idea.GitCommit
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext

/**
 * Base class for all LLM service implementations, providing shared functionality
 * for commit message generation regardless of execution model (API-based or CLI-based).
 */
abstract class LlmServiceBase<C : LlmClientConfiguration>(protected val coroutineScope: CoroutineScope) {

    var generateCommitMessageJob: Job? = null

    /**
     * Prepares the commit message request by computing diff and constructing prompt.
     * Returns (diff, prompt) pair or null if diff is empty.
     */
    protected suspend fun prepareCommitMessageRequest(
        commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>,
        project: Project
    ): Pair<String, String>? {
        val commitContext = commitWorkflowHandler.workflow.commitContext
        val includedChanges = commitWorkflowHandler.ui.getIncludedChanges().toMutableList()

        if (commitContext.isAmendCommitMode) {
            includedChanges += getLastCommitChanges(project)
        }

        val diff = computeDiff(includedChanges, false, project)
        if (diff.isBlank()) {
            withContext(Dispatchers.EDT) {
                sendNotification(Notification.emptyDiff())
            }
            return null
        }

        val branch = getCommonBranch(includedChanges, project)
        val prompt = constructPrompt(
            project.service<ProjectSettings>().activePrompt.content,
            diff, branch, commitWorkflowHandler.getCommitMessage(), project
        )

        return Pair(diff, prompt)
    }

    protected suspend fun getLastCommitChanges(project: Project): List<Change> {
        return withContext(Dispatchers.IO) {
            GitRepositoryManager.getInstance(project).repositories.map { repo ->
                GitHistoryUtils.history(project, repo.root, "--max-count=1")
            }.filter { commits ->
                commits.isNotEmpty()
            }.map { commits ->
                (commits.first() as GitCommit).changes
            }.flatten()
        }
    }

    abstract fun generateCommitMessage(
        clientConfiguration: C,
        commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>,
        project: Project
    )
}
