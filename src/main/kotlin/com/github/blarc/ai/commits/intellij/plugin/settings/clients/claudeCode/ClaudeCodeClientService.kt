package com.github.blarc.ai.commits.intellij.plugin.settings.clients.claudeCode

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.computeDiff
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.constructPrompt
import com.github.blarc.ai.commits.intellij.plugin.AICommitsUtils.getCommonBranch
import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.settings.ProjectSettings
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMClientService
import com.github.blarc.ai.commits.intellij.plugin.wrap
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.ui.components.JBLabel
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import com.intellij.vcs.commit.isAmendCommitMode
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import git4idea.GitCommit
import git4idea.history.GitHistoryUtils
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.File
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class ClaudeCodeClientService(private val cs: CoroutineScope) : LLMClientService<ClaudeCodeClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): ClaudeCodeClientService = service()

        private val CLAUDE_PATHS = listOf(
            "claude",  // In PATH
            System.getProperty("user.home") + "/.claude/local/claude",
            System.getProperty("user.home") + "/.local/bin/claude",
            System.getenv("LOCALAPPDATA")?.let { "$it\\Claude\\claude.exe" }
        ).filterNotNull()
    }

    override suspend fun buildChatModel(client: ClaudeCodeClientConfiguration): ChatModel {
        throw UnsupportedOperationException("Claude Code uses CLI invocation, not langchain4j ChatModel")
    }

    override suspend fun buildStreamingChatModel(client: ClaudeCodeClientConfiguration): StreamingChatModel? {
        return null  // CLI-based, no streaming model
    }

    fun findClaudePath(configuredPath: String): String? {
        // Use configured path if provided
        if (configuredPath.isNotBlank()) {
            val file = File(configuredPath)
            if (file.exists() && file.canExecute()) {
                return configuredPath
            }
            return null
        }

        // Try to find claude in common locations
        for (path in CLAUDE_PATHS) {
            try {
                // Check if it's in PATH using 'which' (Unix) or 'where' (Windows)
                if (path == "claude") {
                    val whichProcess = ProcessBuilder(
                        if (System.getProperty("os.name").lowercase().contains("win")) "where" else "which",
                        "claude"
                    ).start()
                    if (whichProcess.waitFor(5, TimeUnit.SECONDS) && whichProcess.exitValue() == 0) {
                        return whichProcess.inputStream.bufferedReader().readLine()?.trim()
                    }
                } else {
                    val file = File(path)
                    if (file.exists() && file.canExecute()) {
                        return path
                    }
                }
            } catch (e: Exception) {
                // Continue to next path
            }
        }
        return null
    }

    private suspend fun executeClaudeCli(
        client: ClaudeCodeClientConfiguration,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val claudePath = findClaudePath(client.cliPath)
            ?: return@withContext Result.failure(
                IllegalStateException(message("claudeCode.cliNotFound"))
            )

        val command = mutableListOf(claudePath, "-p", "--output-format", "json")

        // Add model if specified
        if (client.modelId.isNotBlank()) {
            command.add("--model")
            command.add(client.modelId)
        }

        // Add the prompt as the last argument
        command.add(prompt)

        try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val completed = process.waitFor(client.timeout.toLong(), TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                return@withContext Result.failure(
                    IllegalStateException(message("claudeCode.timeout"))
                )
            }

            val output = process.inputStream.bufferedReader().readText()

            if (process.exitValue() != 0) {
                return@withContext Result.failure(
                    IllegalStateException("CLI exited with code ${process.exitValue()}: $output")
                )
            }

            // Parse JSON response
            parseClaudeResponse(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseClaudeResponse(jsonOutput: String): Result<String> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val response = json.parseToJsonElement(jsonOutput).jsonObject

            val isError = response["is_error"]?.jsonPrimitive?.booleanOrNull ?: false
            val result = response["result"]?.jsonPrimitive?.contentOrNull

            if (result == null) {
                Result.failure(IllegalStateException("No result in Claude response"))
            } else if (isError) {
                Result.failure(IllegalStateException(result))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException("Failed to parse Claude response: ${e.message}"))
        }
    }

    fun generateCommitMessageCli(
        clientConfiguration: ClaudeCodeClientConfiguration,
        commitWorkflowHandler: AbstractCommitWorkflowHandler<*, *>,
        project: Project
    ) {
        val commitContext = commitWorkflowHandler.workflow.commitContext
        val includedChanges = commitWorkflowHandler.ui.getIncludedChanges().toMutableList()

        generateCommitMessageJob = cs.launch(ModalityState.current().asContextElement()) {
            withBackgroundProgress(project, message("action.background")) {
                if (commitContext.isAmendCommitMode) {
                    includedChanges += getLastCommitChanges(project)
                }

                val diff = computeDiff(includedChanges, false, project)
                if (diff.isBlank()) {
                    withContext(Dispatchers.EDT) {
                        sendNotification(Notification.emptyDiff())
                    }
                    return@withBackgroundProgress
                }

                val branch = getCommonBranch(includedChanges, project)
                val prompt = constructPrompt(
                    project.service<ProjectSettings>().activePrompt.content,
                    diff,
                    branch,
                    commitWorkflowHandler.getCommitMessage(),
                    project
                )

                val result = executeClaudeCli(clientConfiguration, prompt)

                result.fold(
                    onSuccess = { commitMessage ->
                        withContext(Dispatchers.EDT) {
                            commitWorkflowHandler.setCommitMessage(commitMessage)
                        }
                        AppSettings2.instance.recordHit()
                    },
                    onFailure = { error ->
                        withContext(Dispatchers.EDT) {
                            commitWorkflowHandler.setCommitMessage(error.message ?: message("unknown-error"))
                        }
                    }
                )
            }
        }
    }

    fun verifyConfigurationCli(client: ClaudeCodeClientConfiguration, label: JBLabel) {
        label.text = message("settings.verify.running")
        label.icon = AllIcons.General.InlineRefresh
        cs.launch(ModalityState.current().asContextElement()) {
            val claudePath = findClaudePath(client.cliPath)
            if (claudePath == null) {
                withContext(Dispatchers.EDT) {
                    label.text = message("claudeCode.cliNotFound").wrap(60)
                    label.icon = AllIcons.General.InspectionsError
                }
                return@launch
            }

            // Test with a simple prompt
            val result = executeClaudeCli(client, "Say 'OK' in exactly one word")
            withContext(Dispatchers.EDT) {
                result.fold(
                    onSuccess = {
                        label.text = message("settings.verify.valid")
                        label.icon = AllIcons.General.InspectionsOK
                    },
                    onFailure = { error ->
                        label.text = (error.message ?: message("unknown-error")).wrap(60)
                        label.icon = AllIcons.General.InspectionsError
                    }
                )
            }
        }
    }

    private suspend fun getLastCommitChanges(project: Project) = withContext(Dispatchers.IO) {
        GitRepositoryManager.getInstance(project).repositories.map { repo ->
            GitHistoryUtils.history(project, repo.root, "--max-count=1")
        }.filter { commits ->
            commits.isNotEmpty()
        }.map { commits ->
            (commits.first() as GitCommit).changes
        }.flatten()
    }
}