package com.github.blarc.ai.commits.intellij.plugin.settings.prompts

data class Prompt(
        var name: String = "",
        var description: String = "",
        var content: String = "",
        var canBeChanged: Boolean = true
)
