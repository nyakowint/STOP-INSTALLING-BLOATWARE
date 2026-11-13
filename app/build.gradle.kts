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
        versionCode = 1
        versionName = "1.0"
    }
}
