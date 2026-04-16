package com.github.blarc.ai.commits.intellij.plugin.settings.clients.claudeCode

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.github.blarc.ai.commits.intellij.plugin.settings.clients.LlmCliClientService
import com.github.blarc.ai.commits.intellij.plugin.wrap
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.asContextElement
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.JBLabel
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import java.io.File
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class ClaudeCodeClientService(private val cs: CoroutineScope) : LlmCliClientService<ClaudeCodeClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): ClaudeCodeClientService = service()
    }

    fun detectCliPath(): Result<String> {
        val command = if (SystemInfo.isWindows) listOf("where", "claude") else listOf("which", "claude")
        return try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor(10, TimeUnit.SECONDS)
            if (process.exitValue() != 0 || output.isBlank()) {
                Result.failure(IllegalStateException(message("claudeCode.cliNotFound")))
            } else {
                Result.success(output.lines().first().trim())
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(message("claudeCode.cliNotFound")))
        }
    }

    fun detectCliPathAsync(onResult: (Result<String>) -> Unit) {
        val modalityContext = ModalityState.current().asContextElement()
        cs.launch {
            val result = withContext(Dispatchers.IO) { detectCliPath() }
            withContext(Dispatchers.EDT + modalityContext) { onResult(result) }
        }
    }

    override suspend fun executeCli(
        client: ClaudeCodeClientConfiguration,
        prompt: String
    ): Result<String> {
        return executeClaudeCli(client, prompt)
    }

    private suspend fun executeClaudeCli(
        client: ClaudeCodeClientConfiguration,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        // Resolve the CLI path: use the configured path if set, otherwise auto-detect from PATH.
        val resolvedPath = if (client.cliPath.isNotBlank()) {
            client.cliPath
        } else {
            val detected = detectCliPath()
            if (detected.isFailure) {
                return@withContext Result.failure(detected.exceptionOrNull()!!)
            }
            detected.getOrThrow()
        }

        val file = File(resolvedPath)
        if (!file.exists() || !file.canExecute()) {
            return@withContext Result.failure(
                IllegalStateException(message("claudeCode.pathNotFound", resolvedPath))
            )
        }

        val command = mutableListOf(resolvedPath, "-p", "--output-format", "json")

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
            val element = json.parseToJsonElement(jsonOutput)

            val response: JsonObject = when (element) {
                is JsonObject -> element
                is JsonArray -> {
                    // Newer Claude CLI versions emit a JSON array of streaming messages.
                    // Find the message with "type": "result".
                    element.filterIsInstance<JsonObject>()
                        .firstOrNull { it["type"]?.jsonPrimitive?.contentOrNull == "result" }
                        ?: return Result.failure(IllegalStateException("No result message found in Claude response array"))
                }
                else -> return Result.failure(IllegalStateException("Unexpected Claude response format"))
            }

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
            // Test with a simple prompt - executeClaudeCli will validate the path
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
