package com.github.blarc.ai.commits.intellij.plugin.settings.clients.codexCli

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

@Service(Service.Level.APP)
class CodexCliClientService(private val cs: CoroutineScope) : LlmCliClientService<CodexCliClientConfiguration>(cs) {

    companion object {
        @JvmStatic
        fun getInstance(): CodexCliClientService = service()
    }

    override suspend fun executeCli(
        client: CodexCliClientConfiguration,
        prompt: String
    ): Result<String> {
        return executeCodexCli(client, prompt)
    }

    private suspend fun executeCodexCli(
        client: CodexCliClientConfiguration,
        prompt: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val resolvedPath = try {
            resolveCliPath(client)
        } catch (e: IllegalStateException) {
            return@withContext Result.failure(e)
        }
        val file = File(resolvedPath)
        if (!isExecutable(file)) {
            return@withContext Result.failure(
                IllegalStateException(message("codexCli.pathNotFound", resolvedPath))
            )
        }

        val outputFile = Files.createTempFile("codex-cli-", ".txt").toFile()
        outputFile.deleteOnExit()

        val command = mutableListOf(
            resolvedPath,
            "exec",
            "--skip-git-repo-check",
            "--output-last-message",
            outputFile.absolutePath
        )

        if (client.modelId.isNotBlank()) {
            command.add("--model")
            command.add(client.modelId)
        }

        val reasoningValue = normalizeReasoningLevel(resolveReasoningLevel(client))
        if (reasoningValue.isNotBlank()) {
            command.add("--config")
            command.add("model_reasoning_effort=\"$reasoningValue\"")
        }

        command.add("--")
        command.add(prompt)

        try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            // Close stdin immediately to prevent CLI from waiting for input.
            process.outputStream.close()

            // Read output in a separate thread to prevent buffer deadlock.
            val outputFuture = java.util.concurrent.CompletableFuture.supplyAsync {
                process.inputStream.bufferedReader().readText()
            }

            val completed = process.waitFor(client.timeout.toLong(), TimeUnit.SECONDS)
            if (!completed) {
                process.destroyForcibly()
                outputFuture.cancel(true)
                return@withContext Result.failure(
                    IllegalStateException(message("codexCli.timeout"))
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

            val messageText = readOutputMessage(outputFile)
            parseCodexResponse(messageText, output)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            outputFile.delete()
        }
    }

    private fun resolveCliPath(client: CodexCliClientConfiguration): String {
        val configuredPath = client.cliPath.trim()
        if (configuredPath.isNotBlank()) {
            return configuredPath
        }
        throw IllegalStateException(message("codexCli.pathNotConfigured"))
    }

    private fun isExecutable(file: File): Boolean {
        return if (SystemInfo.isWindows) {
            file.isFile
        } else {
            file.isFile && file.canExecute()
        }
    }

    private fun readOutputMessage(outputFile: File): String {
        return try {
            if (outputFile.exists()) {
                outputFile.readText()
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseCodexResponse(messageText: String, output: String): Result<String> {
        val candidate = messageText.trim().ifBlank { output.trim() }
        return if (candidate.isBlank()) {
            Result.failure(IllegalStateException("No result from Codex CLI"))
        } else {
            Result.success(candidate)
        }
    }

    private fun normalizeReasoningLevel(level: String): String {
        if (level.isBlank()) {
            return ""
        }
        return level.trim()
            .lowercase()
            .replace(" ", "_")
    }

    private fun resolveReasoningLevel(client: CodexCliClientConfiguration): String {
        val allowedLevels = if (client.modelId == "gpt-5.1-codex-mini") {
            listOf("Medium", "High")
        } else {
            CodexCliClientConfiguration.REASONING_LEVELS
        }
        return if (client.reasoningLevel in allowedLevels) {
            client.reasoningLevel
        } else {
            CodexCliClientConfiguration.DEFAULT_REASONING_LEVEL
        }
    }

    override fun verifyConfiguration(client: CodexCliClientConfiguration, label: JBLabel) {
        label.text = message("settings.verify.running")
        label.icon = AllIcons.General.InlineRefresh
        cs.launch(ModalityState.current().asContextElement()) {
            val result = executeCodexCli(client, "Say 'OK' in exactly one word")
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
