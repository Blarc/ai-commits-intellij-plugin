package com.github.blarc.ai.commits.intellij.plugin

import com.github.blarc.ai.commits.intellij.plugin.settings.prompts.DefaultPrompts
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class HintRegexTest {

    companion object {
        @JvmStatic
        fun hintsProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "Create a commit message. Here is a hint: {hint}.",
                "hint",
                "Create a commit message. Here is a hint: hint."
            ),
            Arguments.of(
                "Create a commit message. Here is a hint: {hint}.",
                null,
                "Create a commit message. Here is a hint: ."
            ),
            Arguments.of(
                "{My hint for you is: \$hint.}",
                "this is my hint",
                "My hint for you is: this is my hint."
            ),
            Arguments.of(
                "{\$hint}",
                "this is my hint",
                "this is my hint"
            ),
            Arguments.of(
                "{My hint for you is: \$hint.}",
                null,
                ""
            ),
            Arguments.of(
                "Create a commit message. {Here is an additional hint: \$hint.}",
                "hint",
                "Create a commit message. Here is an additional hint: hint."
            ),
            Arguments.of(
                "Create a commit message. {Here is an additional hint: \$hint.}",
                null,
                "Create a commit message. "
            ),
            Arguments.of(
                "Create a commit message. {Here is an additional hint: \$hint.}",
                null,
                "Create a commit message. "
            ),
            Arguments.of(
                DefaultPrompts.BASIC.prompt.content,
                "this is a hint",
                DefaultPrompts.BASIC.prompt.content.replace(
                    "{Use this hint to improve the commit message: \$hint}\n",
                    "Use this hint to improve the commit message: this is a hint\n"
                )
            ),
            Arguments.of(
                DefaultPrompts.CONVENTIONAL.prompt.content,
                "this is a hint",
                DefaultPrompts.CONVENTIONAL.prompt.content.replace(
                    "{Use this hint to improve the commit message: \$hint}\n",
                    "Use this hint to improve the commit message: this is a hint\n"
                )
            ),
            Arguments.of(
                DefaultPrompts.EMOJI.prompt.content,
                "this is a hint",
                DefaultPrompts.EMOJI.prompt.content.replace(
                    "{Use this hint to improve the commit message: \$hint}\n",
                    "Use this hint to improve the commit message: this is a hint\n"
                )
            )
        )
    }

    @ParameterizedTest
    @MethodSource("hintsProvider")
    fun testReplaceHint(prompt: String, hint: String?, expectedPrompt: String) {
        assertEquals(expectedPrompt, AICommitsUtils.replaceHint(prompt, hint))
    }
}
