import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.tasks.VerifyPluginTask.FailureLevel

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "edu.unc.learnwithai"
version = "1.0.1"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.2.5")
    }
}

intellijPlatform {
    // The plugin has no settings pages, so there is nothing to index; skipping
    // this saves a headless IDE launch on every build (including CI).
    buildSearchableOptions = false

    // Checks the built plugin against real IDE builds for classes or APIs that
    // don't exist there. Covers the oldest supported build through the newest.
    pluginVerification {
        // Fail on real breakage only. The INTERNAL_API_USAGES it also reports are
        // bridge methods Kotlin generates for ToolWindowFactory's default methods
        // on older IDEs, not calls this plugin makes.
        failureLevel = listOf(FailureLevel.COMPATIBILITY_PROBLEMS, FailureLevel.INVALID_PLUGIN)
        ides {
            create(IntelliJPlatformType.IntellijIdeaCommunity, "2024.2.5")
            create(IntelliJPlatformType.IntellijIdea, "2026.1.5")
            create(IntelliJPlatformType.IntellijIdea, "2026.2.3")
        }
    }

    pluginConfiguration {
        id = "edu.unc.learnwithai"
        name = "LearnWithAI"
        version = project.version.toString()
        ideaVersion {
            sinceBuild = "242"
            untilBuild = provider { null }
        }
    }
}

kotlin {
    jvmToolchain(21)
}
