# Changelog

## [Unreleased]

## [1.1.0] - 2023-04-28

### Added
- Add `{branch}` variable for prompt customisation.

### Fixed
- Commit message generation does not respect locale.

## [1.0.0] - 2023-04-19

### Added
- Host setting for custom OpenAI compatible API endpoint.
- Proxy url hint in settings.
- Updated plugin description.

## [0.9.0] - 2023-04-16

### Added
- Add more prompts button to settings.

### Changed
- Use jtokkit library for getting max content length for a model and check if prompt is too large.

## [0.8.0] - 2023-04-14

### Added
- Add option to select OpenAI model.

### Changed
- Do not check if prompt is too large, but let OpenAI API do the validation.

## [0.7.0] - 2023-04-12

### Added
- Open AI proxy setting.

## [0.6.2] - 2023-04-11

### Fixed
- Locale is not used in prompt.

## [0.6.1] - 2023-04-10

### Fixed
- Commit workflow handler can be null.

## [0.6.0] - 2023-04-08

### Added
- Table for setting prompts.
- Different prompts to choose from.
- Bug report link to settings.
- Add generate commit action progress indicator.

### Changed
- Sort locales alphabetically.

### Fixed
- Changing token does not work.

## [0.5.1] - 2023-04-05

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

[Unreleased]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.9.0...v1.0.0
[0.9.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.8.0...v0.9.0
[0.8.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.7.0...v0.8.0
[0.7.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.6.2...v0.7.0
[0.6.2]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.6.1...v0.6.2
[0.6.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.6.0...v0.6.1
[0.6.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.5.1...v0.6.0
[0.5.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.5.0...v0.5.1
[0.5.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.4.0...v0.5.0
[0.4.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.3.0...v0.4.0
[0.3.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v0.2.0...v0.3.0
[0.2.0]: https://github.com/Blarc/ai-commits-intellij-plugin/commits/v0.2.0
