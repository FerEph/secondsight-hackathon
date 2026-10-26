plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ca.kofservices.secondsight"
    compileSdk = 34

    defaultConfig {
        applicationId = "ca.kofservices.secondsight"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // TODO(hackathon): wire the Meta Wearables DAT display dependency here
    // (mwdat-display) once its coordinates are confirmed from the DAT
    // developer docs. The HUD renders through the HudRenderer interface in
    // hud/HudState.kt, so no app code changes are needed to plug it in.
    implementation("androidx.core:core-ktx:1.13.1")
}
