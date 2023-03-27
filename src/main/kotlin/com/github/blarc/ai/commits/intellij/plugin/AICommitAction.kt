package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.AICommitsBundle.message
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.vcs.VcsDataKeys
import com.intellij.vcs.commit.AbstractCommitWorkflowHandler
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AICommitAction : AnAction(), DumbAware {
    @OptIn(DelicateCoroutinesApi::class)
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val commitMessage = VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.dataContext)
        val commitWorkflowHandler = e.getData(VcsDataKeys.COMMIT_WORKFLOW_HANDLER) as AbstractCommitWorkflowHandler<*, *>

        runBackgroundableTask(message("action.background"), project) {
            val diff = commitWorkflowHandler.ui.getIncludedChanges().joinToString("\n") {

                val repository = GitRepositoryManager.getInstance(project).getRepositoryForFile(it.virtualFile)
                val diffCommand = GitLineHandler(
                    project,
                    repository!!.root,
                    GitCommand.DIFF
                )
                diffCommand.addParameters(it.virtualFile!!.path)

                val commandResult = Git.getInstance().runCommand(diffCommand)
                commandResult.outputAsJoinedString
            }

            val openAIService = OpenAIService.instance
            GlobalScope.launch(Dispatchers.Main) {
                val generatedCommitMessage = openAIService.generateCommitMessage(diff, 1)
                commitMessage?.setCommitMessage(generatedCommitMessage)
            }
        }
    }
}