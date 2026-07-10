package com.github.blarc.ai.commits.intellij.plugin.settings.prompts

enum class DefaultPrompts(val prompt: Prompt) {

    // Generate UUIDs for game objects in Mine.py and call the function in start_game().
    BASIC(
        Prompt(
            "Basic",
            "Basic prompt that generates a decent commit message.",
            "Write an insightful but concise Git commit message in a complete sentence in present tense for the " +
                    "following diff without prefacing it with anything, the response must be in the language {locale} and must " +
                    "NOT be longer than 74 characters. The sent text will be the differences between files, where deleted lines " +
                    "are prefixed with a single minus sign and added lines are prefixed with a single plus sign.\n" +
                    "{Use this hint to improve the commit message: \$hint}\n" +
                    "Previous commit messages:\n" +
                    "{previousCommitMessages}\n" +
                    "{diff}",
            false
        )
    ),

    // feat: generate unique UUIDs for game objects on Mine game start
    CONVENTIONAL(
        Prompt(
            "Conventional",
            "Prompt for commit message in the conventional commit convention.",
            "Write a commit message in the conventional commit convention. I'll send you an output " +
                    "of 'git diff --staged' command, and you convert it into a commit message. " +
                    "Lines must not be longer than 74 characters. Use {locale} language to answer. " +
                    "End commit title with issue number if you can get it from the branch name: " +
                    "{branch} in parenthesis.\n" +
                    "{Use this hint to improve the commit message: \$hint}\n" +
                    "Previous commit messages:\n" +
                    "{previousCommitMessages}\n" +
                    "{diff}",
            false
        )
    ),

    // author: ljgonzalez1
    // source: https://github.com/Blarc/ai-commits-intellij-plugin/discussions/18#discussioncomment-10718381
    // ✨ feat(conditions): add HpComparisonType enum and ICondition interface for unit comparison logic
    EMOJI(
        Prompt(
            "GitMoji",
            "Prompt for generating commit messages with GitMoji.",
            "Write a concise commit message from 'git diff --staged' output in the format " +
                    "`[EMOJI] [TYPE](file/topic): [description in {locale}]`. Use GitMoji emojis (e.g., ✨ → feat), " +
                    "present tense, active voice, max 120 characters per line, no code blocks.\n" +
                    "Previous commit messages:\n" +
                    "{previousCommitMessages}\n" +
                    "---\n" +
                    "{diff}",
            false
        )
    ),

    EMOJI_CONVENTIONAL(
        Prompt(
            "Emoji Conventional",
            "Conventional Commits 格式 + GitMoji 表情，并依据 diff 中代码注释的语言自适应选择中/英文。",
            "你是一个专业的 Git 提交信息生成器。请基于下面的 diff 生成一条符合 Conventional Commits 规范的提交信息。\n" +
                    "\n" +
                    "## 输出格式\n" +
                    "<type>(<scope>): <emoji> <subject>\n" +
                    "\n" +
                    "<body>\n" +
                    "\n" +
                    "<footer>\n" +
                    "\n" +
                    "## Type 与 Emoji 映射（必选其一）\n" +
                    "- feat: ✨ 新功能\n" +
                    "- fix: 🐛 修复 Bug\n" +
                    "- docs: 📝 仅文档变更\n" +
                    "- style: 💄 代码格式（无逻辑变化）\n" +
                    "- refactor: ♻️ 重构\n" +
                    "- perf: 🚀 性能优化\n" +
                    "- test: ✅ 增删测试\n" +
                    "- chore: 🔧 构建/依赖/工具变动\n" +
                    "- ci: 👷 CI 配置\n" +
                    "- build: 📦 构建系统/外部依赖\n" +
                    "- revert: ⏪ 回滚提交\n" +
                    "- breaking: 💥 破坏性变更（兼容性中断）\n" +
                    "\n" +
                    "## 规则\n" +
                    "- subject 使用祈使句、现在时态，结尾不加句号；英文首字母小写，中文正常书写；尽量不超过 50 个字符（中文保持一行简洁）。\n" +
                    "- emoji 放在 subject 开头的类型之后、与 subject 之间空一格，鼓励使用。\n" +
                    "- body 解释「为什么做」与「影响」，每行不超过 72 字符，与 header 之间空一行。\n" +
                    "- footer 用于关闭 Issue（如 Closes #123）或以 `BREAKING CHANGE:` 开头说明破坏性变更的迁移方案。\n" +
                    "- 一个 Commit 只做一件事（原子性），不要把无关修改混在一起。\n" +
                    "\n" +
                    "## 语言（关键：自适应）\n" +
                    "请根据 diff 中代码注释的语言决定回复语言：\n" +
                    "- 若 diff 含中文注释（如 `// 你好` 或 `# 注意`），使用中文撰写 subject 与 body。\n" +
                    "- 若 diff 含英文注释，使用英文。\n" +
                    "- 若 diff 无注释或极少，默认使用英文（国际通用、便于自动化解析）。\n" +
                    "- 禁止中英混合（例如 `fix: 修复 bug`）。\n" +
                    "{Use this hint to improve the commit message: \$hint}\n" +
                    "Previous commit messages:\n" +
                    "{previousCommitMessages}\n" +
                    "---\n" +
                    "{diff}",
            false
        )
    );

    companion object {
        fun toPromptsMap(): MutableMap<String, Prompt> {
            return entries.associateBy({ it.name.lowercase() }, { it.prompt }).toMutableMap()
        }
    }
}
