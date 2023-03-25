package com.github.blarc.ai.commits.intellij.plugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vcs.VcsDataKeys
import git4idea.GitUtil
import git4idea.GitVcs.runInBackground
import git4idea.commands.Git
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class AICommitAction : AnAction(), DumbAware {
    @OptIn(DelicateCoroutinesApi::class)
    override fun actionPerformed(e: AnActionEvent) {
        val commitMessage = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext)

        val gitRepoManger = GitUtil.getRepositoryManager(e.project!!)
        var diff = ""
        gitRepoManger.repositories.forEach {
            diff += "git diff --cached".runCommand(File(it.root.canonicalPath!!))
        }

        val instance = OpenAIService.instance
        GlobalScope.launch(Dispatchers.Main) {
            val generatedCommitMessage = instance.generateCommitMessage("english", diff, 1)
            commitMessage?.setCommitMessage(generatedCommitMessage)
        }
    }

    private fun String.runCommand(workingDir: File): String? {
        return try {
            val parts = this.split("\\s".toRegex())
            val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(60, TimeUnit.MINUTES)
            proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            e.printStackTrace()
            null
        }
    }
}