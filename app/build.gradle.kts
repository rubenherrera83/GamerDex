import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}
android {
    namespace = "com.gamerdex"
    compileSdk = 35
    buildFeatures { compose = true; buildConfig = true }
    defaultConfig { applicationId = "com.gamerdex"; minSdk = 26; targetSdk = 35; versionCode = 22; versionName = "0.18.0"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    val properties = Properties().apply { rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) } }
    defaultConfig {
        buildConfigField("String", "RAWG_API_KEY", "\"" + properties.getProperty("RAWG_API_KEY", "").replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
        buildConfigField("String", "SCANDEX_API_TOKEN", "\"" + properties.getProperty("SCANDEX_API_TOKEN", "").replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
    }
    compileOptions { isCoreLibraryDesugaringEnabled=true; sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
base { archivesName.set("GamerDex-${android.defaultConfig.versionName}") }
kotlin { jvmToolchain(21); compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) } }
dependencies {
    coreLibraryDesugaring(libs.desugar.nio)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.code.scanner)
    implementation(project(":core:database"))
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:backup"))
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    implementation(libs.androidx.core.ktx)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.room.runtime)
    debugImplementation(libs.compose.ui.test.manifest)
}
