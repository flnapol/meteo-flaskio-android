plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    namespace = "it.flaskio.meteo"
    compileSdk = 35
    defaultConfig {
        applicationId = "it.flaskio.meteo"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
    }
}
dependencies {
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
