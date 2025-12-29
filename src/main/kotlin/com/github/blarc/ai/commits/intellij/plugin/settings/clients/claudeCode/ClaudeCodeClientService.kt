package com.github.blarc.ai.commits.intellij.plugin.settings.clients.claudeCode

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LLMCliClientService
import com.github.blarc.ai.commits.intellij.plugin.wrap
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.ui.components.JBLabel
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.File
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class ClaudeCodeClientService(private val cs: CoroutineScope) : LLMCliClientService<ClaudeCodeClientConfiguration>(cs) {

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

    override suspend fun executeCli(
        client: ClaudeCodeClientConfiguration,
        prompt: String
    ): Result<String> {
        return executeClaudeCli(client, prompt)
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

            // Close stdin immediately to prevent CLI from waiting for input
            process.outputStream.close()

            // Read output in a separate thread to prevent buffer deadlock
            val outputFuture = java.util.concurrent.CompletableFuture.supplyAsync {
                process.inputStream.bufferedReader().readText()
            }

            val completed = process.waitFor(client.timeout.toLong(), TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                outputFuture.cancel(true)
                return@withContext Result.failure(
                    IllegalStateException(message("claudeCode.timeout"))
                )
            }

            val output = try {
                outputFuture.get(5, TimeUnit.SECONDS)
            } catch (e: Exception) {
                val cause = (e as? java.util.concurrent.ExecutionException)?.cause ?: e
                return@withContext Result.failure(
                    IllegalStateException("Failed to read CLI output: ${cause.message}", cause)
                )
            }

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

    override fun verifyConfiguration(client: ClaudeCodeClientConfiguration, label: JBLabel) {
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
}
