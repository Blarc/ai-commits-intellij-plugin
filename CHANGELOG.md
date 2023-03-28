# Changelog

## [Unreleased]
### Added
- Show notification when diff is empty.
- Compute diff using `IdeaTextPatchBuilder` instead of `git diff`.
  - This allows to compute diff only from files and **lines** selected in the commit dialog.

## [0.2.0] - 2023-03-27

### Added
- Basic action for generating commit message.
- Settings for locale and OpenAI token.
- Create commit message only for selected files.

[Unreleased]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.2.0...HEAD
[0.2.0]: https://github.com/Blarc/ai-commits-intellij-plugin/commits/v0.2.0
