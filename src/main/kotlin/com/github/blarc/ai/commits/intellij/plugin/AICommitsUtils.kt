package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.settings.AppSettings
import com.github.blarc.ai.commits.intellij.plugin.settings.ProjectSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.nio.file.FileSystems

object AICommitsUtils {

    fun isPathExcluded(path: String, project: Project) : Boolean {
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
}