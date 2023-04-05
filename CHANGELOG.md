# Changelog

## [Unreleased]
### Fixed
- Use prompt instead of diff when making request to Open AI API.

## [0.5.0] - 2023-04-04

### Added
- Add button for verifying Open AI token in settings.
- Check if prompt is too large for Open AI API.
- Welcome and star notification.

### Changed
- Set default Locale to English.
- Target latest intellij version (2023.1).
- Improve error handling.

### Fixed
- Properly serialize Locale when saving settings.

## [0.4.0] - 2023-03-29

### Changed
- Removed unused `org.jetbrains.plugins.yaml` platform plugin.

### Fix
- Plugin should not have until build number.

## [0.3.0] - 2023-03-28

### Added
- Show notification when diff is empty.
- - This allows to compute diff only from files and **lines** selected in the commit dialog.

## [0.2.0] - 2023-03-27

### Added
- Basic action for generating commit message.
- Settings for locale and OpenAI token.
- Create commit message only for selected files.

[Unreleased]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Blarc/ai-commits-intellij-plugin/commits/v0.2.0
