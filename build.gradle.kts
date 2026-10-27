plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "edu.unc.learnwithai"
version = "1.0.0"

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
