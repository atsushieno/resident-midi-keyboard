enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(uri("https://jitpack.io")) // required for compose-markdown...
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "resident-midi-keyboard"
val CAC_DIR = "external/compose-audio-controls"
include(":app")
include(":compose-audio-controls")
include(":compose-audio-controls-midi")
project(":compose-audio-controls").projectDir = File("$CAC_DIR/compose-audio-controls")
project(":compose-audio-controls-midi").projectDir = File("$CAC_DIR/compose-audio-controls-midi")

