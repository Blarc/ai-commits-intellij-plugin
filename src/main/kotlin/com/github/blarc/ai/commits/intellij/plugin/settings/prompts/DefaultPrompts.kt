package com.github.blarc.ai.commits.intellij.plugin.settings.prompts

enum class DefaultPrompts(val title: String, val description: String, val content: String) {

    // Generate UUIDs for game objects in Mine.py and call the function in start_game().
    BASIC(
        "Basic",
        "Basic prompt that generates a decent commit message.",
        "Write an insightful but concise Git commit message in a complete sentence in present tense for the " +
                "following diff without prefacing it with anything, the response must be in the language {locale} and must " +
                "NOT be longer than 74 characters. The sent text will be the differences between files, where deleted lines " +
                "are prefixed with a single minus sign and added lines are prefixed with a single plus sign.\n" +
                "{Use this hint to improve this commit message: \$hint\n}" +
                "{diff}"
    ),
    // feat: generate unique UUIDs for game objects on Mine game start
    CONVENTIONAL(
        "Conventional",
        "Prompt for commit message in the conventional commit convention.",
        "Write a commit message in the conventional commit convention. I'll send you an output " +
                "of 'git diff --staged' command, and you convert it into a commit message. " +
                "Lines must not be longer than 74 characters. Use {locale} language to answer. " +
                "End commit title with issue number if you can get it from the branch name: " +
                "{branch} in parenthesis.\n" +
                "{Use this hint to improve this commit message: \$hint\n}" +
                "{diff}",
    ),
    // ✨ feat(mine): Generate objects UUIDs and start team timers on game start
    EMOJI(
        "Emoji",
        "Prompt for commit message in the conventional commit convention with GitMoji convention.",
        "Write a clean and comprehensive commit message in the conventional commit convention. " +
                "I'll send you an output of 'git diff --staged' command, and you convert " +
                "it into a commit message. " +
                "Use GitMoji convention to preface the commit. " +
                "Do NOT add any descriptions to the commit, only commit message. " +
                "Use the present tense. " +
                "Lines must not be longer than 74 characters. " +
                "{Use this hint to improve this commit message: \$hint\n}" +
                "Use {locale} language to answer.\n" +
                "{diff}",
    );

    companion object {
        fun toPromptsMap(): MutableMap<String, Prompt> {
            return entries.associateBy({ it.name.lowercase() }, DefaultPrompts::toPrompt).toMutableMap()
        }
    }

    fun toPrompt(): Prompt {
        return Prompt(
            this.title,
            this.content,
            this.description,
            false
        )
    }
}
