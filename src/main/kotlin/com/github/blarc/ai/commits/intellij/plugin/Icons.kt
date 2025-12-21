package com.github.blarc.ai.commits.intellij.plugin

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.JBColor
import javax.swing.Icon

object Icons {

    data class AICommitsIcon(val bright: String, val dark: String?) {

        fun getThemeBasedIcon(): Icon {
            return if (JBColor.isBright() || dark == null) {
                IconLoader.getIcon(bright, javaClass)
            } else {
                IconLoader.getIcon(dark, javaClass)
            }
        }
    }

    val AI_COMMITS = AICommitsIcon("/icons/aiCommits15.svg", null)
    val OPEN_AI = AICommitsIcon("/icons/openai15bright.svg", "/icons/openai15dark.svg")
    val OLLAMA = AICommitsIcon("/icons/ollama15bright.svg", "/icons/ollama15dark.svg")
    val QIANFAN = AICommitsIcon("/icons/qianfan.png", null)
    val GEMINI_VERTEX = AICommitsIcon("/icons/geminiVertex.svg", null)
    val GEMINI_GOOGLE = AICommitsIcon("/icons/geminiGoogle.svg", null)
    val ANTHROPIC = AICommitsIcon("/icons/anthropic15bright.svg", "/icons/anthropic15dark.svg")
    val AZURE_OPEN_AI = AICommitsIcon("/icons/azureOpenAi.svg", null)
    val HUGGING_FACE = AICommitsIcon("/icons/huggingface.svg", null)
    val GITHUB = AICommitsIcon("/icons/github15bright.svg", "/icons/github15dark.svg")
    val MISTRAL = AICommitsIcon("/icons/mistral.svg", null)
    val AMAZON_BEDROCK = AICommitsIcon("/icons/amazonBedrock15.svg", "/icons/amazonBedrock15.svg")
    val GIGACHAT = AICommitsIcon("/icons/gigachat15.svg", "/icons/gigachat15.svg")

    object Process {
        val STOP = AICommitsIcon("/icons/stop.svg", "/icons/stop_dark.svg")
    }
}
