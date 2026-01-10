package com.github.blarc.ai.commits.intellij.plugin.settings.clients

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Base class for CLI-based LLM agents (e.g., Claude Code, Codex).
 * These agents execute via command-line interface rather than HTTP APIs.
 */
abstract class LlmCliClientService<C : LlmClientConfiguration>(coroutineScope: CoroutineScope)
    : LlmServiceBase<C>(coroutineScope) {

    /**
     * Execute CLI command with the given prompt and return the result.
     */
    protected abstract suspend fun executeCli(
        client: C,
        prompt: String
    ): Result<String>

    override fun generateCommitMessage(
        clientConfiguration: C,
        commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>,
        project: Project
    ) {
        generateCommitMessageJob = coroutineScope.launch(ModalityState.current().asContextElement()) {
            withBackgroundProgress(project, message("action.background")) {
                val (_, prompt) = prepareCommitMessageRequest(
                    commitWorkflowHandler, project
                ) ?: return@withBackgroundProgress

                val result = executeCli(clientConfiguration, prompt)

                result.fold(
                    onSuccess = { commitMessage ->
                        withContext(Dispatchers.EDT) {
                            commitWorkflowHandler.setCommitMessage(commitMessage)
                        }
                        AppSettings2.instance.recordHit()
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.EDT) {
                            commitWorkflowHandler.setCommitMessage(
                                error.message ?: message("unknown-error")
                            )
                        }
                    }
                )
            }
        }
    }

    /**
     * Verify CLI configuration by executing a test command.
     */
    abstract fun verifyConfiguration(client: C, label: JBLabel)
}
