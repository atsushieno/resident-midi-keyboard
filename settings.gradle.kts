pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(uri("https://jitpack.io"))
        mavenLocal()
    }
}

rootProject.name = "resident-midi-keyboard"
val CAC_DIR = "external/compose-audio-controls"
include(":app")
include(":compose-audio-controls")
include(":compose-audio-controls-midi")
project(":compose-audio-controls").projectDir = File("$CAC_DIR/compose-audio-controls")
project(":compose-audio-controls-midi").projectDir = File("$CAC_DIR/compose-audio-controls-midi")

