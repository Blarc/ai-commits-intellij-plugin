import org.jetbrains.changelog.Changelog

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.intellij") version "1.17.4"
    kotlin("plugin.serialization") version "2.1.10"

    // Gradle Changelog Plugin
    id("org.jetbrains.changelog") version "2.2.1"
}

group = properties("pluginGroup")
version = properties("pluginVersion")

// Configure project's dependencies
repositories {
    mavenCentral()
}

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(properties("javaVersion").toInt())
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    updateSinceUntilBuild.set(false)

    plugins.set(
        properties("platformPlugins").split(',')
            .map(String::trim)
            .filter(String::isNotEmpty)
    )
}

changelog {
//    version.set(properties("pluginVersion"))
    groups.empty()
    repositoryUrl.set(properties("pluginRepositoryUrl"))
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        // untilBuild.set(properties("pluginUntilBuild"))

        // Get the latest available change notes from the changelog file
        changeNotes.set(provider {
            with(changelog) {
                renderItem(
                    getOrNull(properties("pluginVersion")) ?: getUnreleased()
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        })
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        dependsOn("patchChangelog")
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
//    implementation("com.aallam.openai:openai-client:3.7.2") {
//        exclude(group = "org.slf4j", module = "slf4j-api")
//        // Prevents java.lang.LinkageError: java.lang.LinkageError: loader constraint violation:when resolving method 'long kotlin.time.Duration.toLong-impl(long, kotlin.time.DurationUnit)'
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
//    }
//    implementation("io.ktor:ktor-client-cio:2.3.11") {
//        exclude(group = "org.slf4j", module = "slf4j-api")
//        // Prevents java.lang.LinkageError: java.lang.LinkageError: loader constraint violation: when resolving method 'long kotlin.time.Duration.toLong-impl(long, kotlin.time.DurationUnit)'
//        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
//    }
//
//    implementation("com.knuddels:jtokkit:1.0.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    // langchain4j integrations
    implementation(platform("dev.langchain4j:langchain4j-bom:0.36.2"))
    implementation("dev.langchain4j:langchain4j-open-ai")
    implementation("dev.langchain4j:langchain4j-ollama")
    // The Baidu Qianfan Large Model Platform, including the ERNIE series, can be accessed at https://docs.langchain4j.dev/integrations/language-models/qianfan/.
    implementation("dev.langchain4j:langchain4j-qianfan")
    implementation("dev.langchain4j:langchain4j-vertex-ai-gemini")
    implementation("dev.langchain4j:langchain4j-anthropic")
    implementation("dev.langchain4j:langchain4j-azure-open-ai")
    implementation("dev.langchain4j:langchain4j-hugging-face")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini")
    implementation("dev.langchain4j:langchain4j-github-models")
    implementation("dev.langchain4j:langchain4j-mistral-ai")
    // tests
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
