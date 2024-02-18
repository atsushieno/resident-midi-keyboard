plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.jetbrainsComposePlugin) apply false
    alias(libs.plugins.dokkaPlugin) apply false
    alias(libs.plugins.metalavaPlugin) apply false
}

// Use system environment variables
ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME") ?: ""
ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD") ?: ""
ext["sonatypeStagingProfileId"] = System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: ""
ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID") ?: ""
ext["signing.password"] = System.getenv("SIGNING_PASSWORD") ?: ""
ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_SECRET_KEY_RING_FILE") ?: ""

true