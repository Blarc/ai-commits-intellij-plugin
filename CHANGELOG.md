# Changelog

## [Unreleased]

## [2.19.1] - 2026-02-23

### Fixed

- Commit message generation fails when SVN is not installed.

## [2.19.0] - 2026-02-22

### Added

- Prompt variable `{previousCommitMessages}` for prompt customization that is replaced with commit messages from the
  previous commits.
- Setting for a number of previous commit messages to include in the prompt in prompt settings.
- Notification if Task Manager is null and prompt variables for the task cannot be resolved.

## [2.18.0] - 2026-02-06

### Added

- Support for Codex CLI (thanks to @Algomorph)

## [2.17.0] - 2026-01-10

### Added

- Support for Claude Code CLI (thanks to @Algomorph)
- Setting for a cleanup regex that can be used to remove unwanted text from the generated commit message.
- Use JetBrains Exception Analyzer (EA) as a backend service for reporting plugin exceptions.

### Fixed

- Editing an LLM client configuration (e.g., changing the model) would not take effect until the IDE restarts.
- Model IDs are not correctly updated after refreshing models via API.

## [2.16.1] - 2025-11-13

### Fixed

- Method actionPerformed violates override-only method usage.
- Invocation of unresolved method PasswordSafe.getAsync(CredentialAttributes) in AppSettings2.

## [2.16.0] - 2025-09-04

### Added

- Support AWS SDK default credential locations for Bedrock Integration (#368).

## [2.15.0] - 2025-08-30

### Changed

- Generate commit action triggered via shortcut no longer opens the dropdown but instead runs the commit generation with
  the currently selected LLM client (#370).

### Removed

- Remove deprecated Gemini models (#376).

## [2.14.1] - 2025-07-15

### Fixed

- Invalid first-keystroke attribute value in plugin.xml causes PluginException (#366).

## [2.14.0] - 2025-07-08

### Added

- Add a shortcut (`CMD + option + C` / `control + alt + C`) for generating a commit message (#362).

### Changed

- Temperature is no longer a required field for OpenAI client configuration (#360).

## [2.13.0] - 2025-06-17

### Added

- Support Amazon Bedrock client.
- Ollama icon for dark theme and Anthropic & OpenAI icons for dark theme in SVG format (thanks to @yaoxinghuo).

## [2.12.0] - 2025-06-05

### Added

- Generate commit action button is now a split button with a dropdown from which the user can change the LLM client from
  the commit dialog.
- Sort LLM clients in settings dropdown.

## [2.11.1] - 2025-04-25

### Fixed

- Make SVN dependency optional.

## [2.11.0] - 2025-04-24

### Added

- Support for SVN.

## [2.10.1] - 2025-04-13

### Fixed

- Common branch is not detected when most files are unassociated with branches, despite at least one file belonging to a
  branch.
- Cannot generate commit message when updating a submodule.

## [2.10.0] - 2025-04-08

### Added

- Variable `{taskTimeSpent}` in `HH:mm` format for prompt customization that is replaced with value from the
  active [task](https://www.jetbrains.com/help/idea/managing-tasks-and-context.html#work-with-tasks).

## [2.9.1] - 2025-03-23

### Fixed

- Common branch is not computed.
- Align language handling with IDE settings (thanks to @Canario5).
- Locale dropdown incorrectly displays the first alphabetical locale instead of the actual default (e.q. "English") (
  thanks to @Canario5).

## [2.9.0] - 2024-12-01

### Added

- More options for configuring LLM clients.
- **Use the chosen LLM client icon as the generate commit message action's icon.**
- Option to stop the commit message generation by clicking the action icon again.
- Setting for HuggingFace client to automatically remove prompt from the generated commit message.
- Show progress and result when refreshing models via API.
- Support for Mistral AI.

### Fixed

- The progress bar for generating commit message continues running after the user creates the commit.

## [2.8.0] - 2024-11-13

### Added

- Support streaming mode for Gemini Google.
- Support GitHub models client.
- Theme based icons for better visibility.

### Fixed

- Project specific locale is not used when creating prompt.
- Properties topP and topK are not used when verifying Gemini Google client configuration.

## [2.7.1] - 2024-11-04

### Added

- Option to set top K and top P in Gemini Google client settings.

### Fixed

- Unable to submit request to Gemini Google because it has a topK value of 64 but the supported range is from 1 (
  inclusive) to 41 (exclusive).

## [2.7.0] - 2024-11-01

### Added

- Support for Gemini Google.
- Save the size of the dialog for adding prompts, if it's resized by the user.

### Changed

- Rename Gemini to Gemini Vertex.
- Use the correct icon for Gemini Vertex.

### Fixed

- Project's specific prompt is not saved properly.

## [2.6.0] - 2024-10-09

### Added

- Support streaming response.
- Support for Hugging Face.

## [2.5.0] - 2024-09-22

### Added

- Support for Azure OpenAI.
- Sort LLM client configurations by provider name and configuration name.

### Changed

- Update default prompt for generating commit messages with GitMoji.

### Fixed

- Open AI configuration setting `organizationId` is not used when verifying configuration.
- Gemini configuration settings `projectId` and `location` are not used when verifying configuration.
- Notification about common branch is shown after the prompt dialog is closed.
- Invalid caret position for prompt preview.

## [2.4.1] - 2024-09-19

### Fixed

- Setting LLM client configuration or prompt as project specific does not work.

## [2.4.0] - 2024-09-13

### Added

- Option to choose prompt per project.
- Amending commits now adds the changes from previous commit to the prompt.

### Fixed

- Prompt does not contain diff for new files.

## [2.3.1] - 2024-09-11

### Fixed

- NPE when retrieving TaskManager for prompt construction.

## [2.3.0] - 2024-09-09

### Added

- Variables `{taskId}`, `{taskSummary}` and `{taskDescription}` for prompt customization that are replaced with values
  from the active [task](https://www.jetbrains.com/help/idea/managing-tasks-and-context.html#work-with-tasks).
- Option to configure LLM client configuration per project.

### Changed

- Rethrow generic exceptions when generating commit messages.
- Replace `executeOnPooledThread` with coroutines and `ModalityState`.

### Fixed

- NPE when verifying LLM client configuration.

## [2.2.0] - 2024-08-01

### Added

- Support for Anthropic.

### Fixed

- Dialog is too small to show full verification result.

## [2.1.1] - 2024-07-31

### Fixed

- GPT4o-mini is missing from OpenAI model list.

## [2.1.0] - 2024-07-19

### Added

- Support for Qianfan (Ernie).
- Support for Gemini.
- Icon next to name in clients table.

### Fixed

- Ollama icon size to 15x15.
- Input is not validated when switching between clients in add and edit dialog.
- GitBranchWorker.loadTotalDiff should not wait for built-in server on EDT.

## [2.0.0] - 2024-06-08

### Added

- LLM clients table for saving multiple clients configurations.
- Support for Ollama.
- Setting for Open AI's organization ID.
- New persistent state settings component for saving multiple LLM clients.

### Changed

- Replace `com.aallam.openai:openai-client` with `langchain4j`.
- Refactor the code with generic implementation of LLM clients that makes adding new clients easier.
- Use Kotlin's coroutines to prevent blocking of EDT thread.
- Plugin now supports IDE versions 233 and higher.

### Fixed

- Language Pack plugin makes the AI commits plugin unusable.

## [1.6.0] - 2024-03-05

### Added

- Variable `{hint}` for prompt customization that is filled with content from commit message dialog.

### Fixed

- Selected prompt resets after editing custom prompt.

## [1.5.1] - 2024-01-27

### Fixed

- Diff "before" and "after" is swapped in "Edit Prompt" settings window.

## [1.5.0] - 2023-12-26

### Added

- OpenAI session timeout setting.

## [1.4.0] - 2023-12-18

### Added

- Show example OpenAI token value when field is empty.
- Show prompt preview when editing a prompt.

## [1.3.0] - 2023-11-11

### Changed

- Update dependencies.
- Change word order of comment about prompt setting.

## [1.2.1] - 2023-06-17

### Fixed

- Set error message as commit message when OpenAI API returns error, because notification seems unreliable.

## [1.2.0] - 2023-06-15

### Added

- Add OpenAI temperature setting.
- Update preset conventional commit prompt.
- Add exclusion globs table to settings.

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
- 
    - This allows to compute diff only from files and **lines** selected in the commit dialog.

## [0.2.0] - 2023-03-27

### Added

- Basic action for generating commit message.
- Settings for locale and OpenAI token.
- Create commit message only for selected files.

[Unreleased]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.19.1...HEAD
[2.19.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.19.0...v2.19.1
[2.19.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.18.0...v2.19.0
[2.18.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.17.0...v2.18.0
[2.17.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.16.1...v2.17.0
[2.16.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.16.0...v2.16.1
[2.16.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.15.0...v2.16.0
[2.15.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.14.1...v2.15.0
[2.14.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.14.0...v2.14.1
[2.14.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.13.0...v2.14.0
[2.13.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.12.0...v2.13.0
[2.12.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.11.1...v2.12.0
[2.11.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.11.0...v2.11.1
[2.11.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.10.1...v2.11.0
[2.10.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.10.0...v2.10.1
[2.10.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.9.1...v2.10.0
[2.9.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.9.0...v2.9.1
[2.9.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.8.0...v2.9.0
[2.8.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.7.1...v2.8.0
[2.7.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.7.0...v2.7.1
[2.7.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.6.0...v2.7.0
[2.6.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.5.0...v2.6.0
[2.5.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.4.1...v2.5.0
[2.4.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.4.0...v2.4.1
[2.4.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.3.1...v2.4.0
[2.3.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.3.0...v2.3.1
[2.3.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.2.0...v2.3.0
[2.2.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.1.1...v2.2.0
[2.1.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.1.0...v2.1.1
[2.1.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v2.0.0...v2.1.0
[2.0.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.6.0...v2.0.0
[1.6.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.5.1...v1.6.0
[1.5.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.5.0...v1.5.1
[1.5.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.4.0...v1.5.0
[1.4.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.2.1...v1.3.0
[1.2.1]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/Blarc/ai-commits-intellij-plugin/compare/v1.1.0...v1.2.0
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
