package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.notifications.Notification
import com.github.blarc.ai.commits.intellij.plugin.notifications.sendNotification
import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings2
import com.github.blarc.ai.commits.intellij.plugin.settings.ProjectSettings
import com.intellij.DynamicBundle
import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.OneTimeString
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.service
import com.intellij.openapi.diff.impl.patch.IdeaTextPatchBuilder
import com.intellij.openapi.diff.impl.patch.UnifiedDiffWriter
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.Change
import com.intellij.tasks.TaskManager
import com.intellij.util.text.DateFormatUtil
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.StringWriter
import java.nio.file.FileSystems
import java.util.*

object AICommitsUtils {

    fun isPathExcluded(path: String, project: Project): Boolean {
        return AppSettings2.instance.isPathExcluded(path) || project.service<ProjectSettings>().isPathExcluded(path)
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

    fun constructPrompt(promptContent: String, diff: String, branch: String, hint: String?, project: Project): String {
        var content = promptContent
        val locale = project.service<ProjectSettings>().locale
        content = content.replace("{locale}", locale.getDisplayLanguage(Locale.ENGLISH))
        content = content.replace("{branch}", branch)
        content = replaceHint(content, hint)

        // TODO @Blarc: If TaskManager is null, the prompt might be incorrect...
        TaskManager.getManager(project)?.let {
            val activeTask = it.activeTask
            content = content.replace("{taskId}", activeTask.id)
            content = content.replace("{taskSummary}", activeTask.summary)
            content = content.replace("{taskDescription}", activeTask.description.orEmpty())
            content = content.replace("{taskTimeSpent}", DateFormatUtil.formatTime(activeTask.totalTimeSpent))
        }

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

    suspend fun getCommonBranchOrDefault(changes: List<Change>, project: Project, showNotification: Boolean = true): String {
        var branch = getCommonBranch(changes, project)
        if (branch == null) {
            // Can't show notification in edit prompt dialog
            if (showNotification) {
                sendNotification(Notification.noCommonBranch())
            }
            // hardcoded fallback branch
            branch = "main"
        }
        return branch
    }

    suspend fun getCommonBranch(changes: List<Change>, project: Project): String? {
        val repositoryManager = GitRepositoryManager.getInstance(project)
        return withContext(Dispatchers.IO) {
            changes.map {
                repositoryManager.getRepositoryForFile(it.virtualFile)?.currentBranchName
            }.filterNotNull().groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
        }
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
                it.filePath()?.path?.let { path ->
                    !isPathExcluded(path, project)
                } ?: false
            }
            .mapNotNull { change ->
                change.filePath()?.let { filePath ->
                    gitRepositoryManager.getRepositoryForFileQuick(
                        filePath
                    ) to change
                }
            }
            .filter { !it.second.isSubmoduleChange(project) }
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

    // TODO @Blarc: This only works for OpenAI
//    fun isPromptTooLarge(prompt: String): Boolean {
//        val registry = Encodings.newDefaultEncodingRegistry()
//
//        /*
//         * Try to find the model type based on the model id by finding the longest matching model type
//         * If no model type matches, let the request go through and let the OpenAI API handle it
//         */
//        val modelType = ModelType.entries
//            .filter { AppSettings2.instance.getActiveLLMClient().modelId.contains(it.name) }
//            .maxByOrNull { it.name.length }
//            ?: return false
//
//        val encoding = registry.getEncoding(modelType.encodingType)
//        return encoding.countTokens(prompt) > modelType.maxContextLength
//    }

    suspend fun retrieveToken(title: String): OneTimeString? {
        val credentialAttributes = getCredentialAttributes(title)
        val credentials = withContext(Dispatchers.IO) {
            PasswordSafe.instance.get(credentialAttributes)
        }
        return credentials?.password
    }

    fun getCredentialAttributes(title: String): CredentialAttributes {
        return CredentialAttributes(
            title,
            null,
            this.javaClass,
            false
        )
    }

    fun getIDELocale(): Locale {
        return DynamicBundle.getLocale()
    }
}
