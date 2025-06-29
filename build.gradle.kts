import org.jetbrains.changelog.Changelog
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.6.0"
    id("org.jetbrains.changelog") version "2.2.1"
}

group = "com.github.AllePilli"
version = "0.2.15"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        jetbrainsRuntime()
        marketplace()
    }
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
// Read more version 2.x: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        pycharmCommunity("2025.1.1.1")
        jetbrainsRuntime()

        bundledPlugin("PythonCore")
        bundledPlugin("com.intellij.dev")
        bundledPlugin("Git4Idea")
        plugin("com.jetbrains.hackathon.indices.viewer", "1.30")
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "251"
            untilBuild = "251.*"
        }
    }

    signing {
        certificateChain = System.getenv("CERTIFICATE_CHAIN")
        privateKey = System.getenv("PRIVATE_KEY")
        password = System.getenv("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
    }
}

var showCompleteChangelog = false
var changelogOutputType = Changelog.OutputType.HTML

tasks {
    patchPluginXml {
        changeNotes.set(provider {
            if (showCompleteChangelog) changelog.render(changelogOutputType)
            else changelog.renderItem(
                    changelog.get(version as String)
                            .withEmptySections(false),
                    changelogOutputType
            )
        })
    }
}