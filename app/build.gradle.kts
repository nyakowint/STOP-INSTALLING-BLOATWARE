plugins {
    id("com.android.application")
}

android {
    namespace = "dev.installguard"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.installguard"
        minSdk = 30
        targetSdk = 37
        // CI run number, so each release installs as an update over the last
        val build = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 1
        versionCode = build
        versionName = "1.0.$build"
    }
}
