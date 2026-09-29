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
import com.intellij.openapi.project.Project
import com.intellij.util.text.DateFormatUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    fun constructPrompt(
        promptContent: String,
        diff: String,
        branch: String?,
        hint: String?,
        previousCommitMessages: List<String>,
        project: Project
    ): String {
        var content = promptContent
        val locale = project.service<ProjectSettings>().locale
        content = content.replace("{locale}", locale.getDisplayLanguage(Locale.ENGLISH))
        content = replaceBranch(content, branch)
        content = replaceHint(content, hint)
        content = content.replace("{previousCommitMessages}", previousCommitMessages.joinToString("\n"))
        content = replaceTask(content, project)

        return if (content.contains("{diff}")) {
            content.replace("{diff}", diff)
        } else {
            "$content\n$diff"
        }
    }

    fun replaceBranch(promptContent: String, branch: String?): String {
        if (promptContent.contains("{branch}")) {
            if (branch != null) {
                return promptContent.replace("{branch}", branch)
            } else {
                sendNotification(Notification.noCommonBranch())
                return promptContent.replace("{branch}", "main")
            }
        }
        return promptContent
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

    fun replaceTask(promptContent: String, project: Project): String {
        var content = promptContent
        val activeTask = getActiveTask(project)

        if (activeTask != null) {
            content = content.replace("{taskId}", activeTask.id)
            content = content.replace("{taskSummary}", activeTask.summary)
            content = content.replace("{taskDescription}", activeTask.description)
            content = content.replace("{taskTimeSpent}", DateFormatUtil.formatTime(activeTask.totalTimeSpent))
        } else if (content.contains("{taskId}") || content.contains("{taskSummary}") || content.contains("{taskDescription}") || content.contains("{taskTimeSpent}")) {
            sendNotification(Notification.taskManagerIsNull())
        }

        return content
    }

    /**
     * The Tasks API is no longer bundled in every IntelliJ Platform product. Keep
     * task prompt variables available where the API exists without making the
     * entire plugin unloadable on products where it does not.
     */
    private fun getActiveTask(project: Project): ActiveTask? = runCatching {
        val taskManagerClass = Class.forName("com.intellij.tasks.TaskManager")
        val taskManager = taskManagerClass
            .getMethod("getManager", Project::class.java)
            .invoke(null, project)
            ?: return null
        val task = taskManagerClass.getMethod("getActiveTask").invoke(taskManager)

        ActiveTask(
            id = task.javaClass.getMethod("getId").invoke(task) as String,
            summary = task.javaClass.getMethod("getSummary").invoke(task) as String,
            description = task.javaClass.getMethod("getDescription").invoke(task) as? String ?: "",
            totalTimeSpent = task.javaClass.getMethod("getTotalTimeSpent").invoke(task) as Long,
        )
    }.getOrNull()

    private data class ActiveTask(
        val id: String,
        val summary: String,
        val description: String,
        val totalTimeSpent: Long,
    )

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
            false
        )
    }

    fun getIDELocale(): Locale {
        return DynamicBundle.getLocale()
    }
}
