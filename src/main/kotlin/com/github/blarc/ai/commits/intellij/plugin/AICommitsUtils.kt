package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.github.blarc.ai.commits.intellij.plugin.settings.ProjectSettings
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.ModelType
import git4idea.repo.GitRepositoryManager
import java.io.StringWriter
import java.nio.file.FileSystems

object AICommitsUtils {

    fun isPathExcluded(path: String, project: Project): Boolean {
        return !AppSettings.instance.isPathExcluded(path) && !project.service<ProjectSettings>().isPathExcluded(path)
    }

    fun matchesGlobs(text: String, globs: Set<String>): Boolean {
        val fileSystem = FileSystems.getDefault()
        for (globString in globs) {
            val glob = fileSystem.getPathMatcher("glob:$globString")
            if (glob.matches(fileSystem.getPath(text))) {
                return true
            }
        }
        return false
    }

    fun constructPrompt(promptContent: String, diff: String, branch: String, hint: String?): String {
        var content = promptContent
        content = content.replace("{locale}", AppSettings.instance.locale.displayLanguage)
        content = content.replace("{branch}", branch)
        content = replaceHint(content, hint)

        return if (content.contains("{diff}")) {
            content.replace("{diff}", diff)
        } else {
            "$content\n$diff"
        }
    }

    fun replaceHint(promptContent: String, hint: String?): String {
        val hintRegex = Regex("\\{[^{}]*(\\\$hint)[^{}]*}")

        hintRegex.find(promptContent, 0)?.let {
            if (!hint.isNullOrBlank()) {
                var hintValue = it.value.replace("\$hint", hint)
                hintValue = hintValue.replace("{", "")
                hintValue = hintValue.replace("}", "")
                return promptContent.replace(it.value, hintValue)
            } else {
                return promptContent.replace(it.value, "")
            }
        }
        return promptContent.replace("{hint}", hint.orEmpty())
    }

    fun commonBranch(changes: List<Change>, project: Project): String {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        var branch = changes.map {
            repositoryManager.getRepositoryForFileQuick(it.virtualFile)?.currentBranchName
        }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

        if (branch == null) {
            sendNotification(Notification.noCommonBranch())
            // hardcoded fallback branch
            branch = "main"
        }
        return branch
    }

    fun computeDiff(
        includedChanges: List<Change>,
        reversePatch: Boolean,
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
                        repository.root.toNioPath(), reversePatch, true
                    )

                    val stringWriter = StringWriter()
                    stringWriter.write("Repository: ${repository.root.path}\n")
                    UnifiedDiffWriter.write(project, filePatches, stringWriter, "\n", null)
                    stringWriter.toString()
                }
            }
            .joinToString("\n")
    }

    fun isPromptTooLarge(prompt: String): Boolean {
        val registry = Encodings.newDefaultEncodingRegistry()

        /*
         * Try to find the model type based on the model id by finding the longest matching model type
         * If no model type matches, let the request go through and let the OpenAI API handle it
         */
        val modelType = ModelType.entries
            .filter { AppSettings.instance.currentLlmProvider.modelId.contains(it.name) }
            .maxByOrNull { it.name.length }
            ?: return false

        val encoding = registry.getEncoding(modelType.encodingType)
        return encoding.countTokens(prompt) > modelType.maxContextLength
    }

    fun retrieveToken(title: String): String? {
        val credentials: Credentials? = PasswordSafe.instance.get(getCredentialAttributes(title))
        return credentials?.getPasswordAsString()
    }

    fun getCredentialAttributes(title: String): CredentialAttributes {
        return CredentialAttributes(
            title,
            null,
            this.javaClass,
            false
        )
    }
}
