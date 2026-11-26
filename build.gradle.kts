plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.20.0"
}

group = "com.obsidian"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.2.4")
        bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
            untilBuild = provider { null }
        }
    }
}

tasks.runIde {
    jvmArgs("-Dijent.enabled=false")
}