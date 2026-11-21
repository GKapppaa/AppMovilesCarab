import java.util.Properties
import org.gradle.api.tasks.Exec

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.appcarab"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.appcarab"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val apiBaseUrl = providers.gradleProperty("apiBaseUrl").orElse("http://127.0.0.1:8081/api/v1/").get()
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // El idioma se elige dentro de la app: el bundle debe incluir todas las traducciones.
    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.pdfbox.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.androidx.work.runtime.ktx)


    // Biblioteca principal de Room.
    implementation("androidx.room:room-runtime:2.8.5") // Permite administrar una base de datos SQLite en Android.

    // Extensiones de Room para trabajar con Kotlin.
    implementation("androidx.room:room-ktx:2.8.5")
    implementation(libs.material) // Proporciona integración con corrutinas

    // Para generar automáticamente código adicional durante la compilación
    ksp("androidx.room:room-compiler:2.8.5")

    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// The emulator reaches the host through adb reverse. Run this after installDebug so
// Android Studio's Run action also prepares the API port without a manual command.
val localProperties = rootProject.file("local.properties")
val localSdkPath = if (localProperties.exists()) {
    Properties().apply { localProperties.inputStream().use(::load) }.getProperty("sdk.dir")
} else {
    null
}
val androidSdkPath = providers.environmentVariable("ANDROID_SDK_ROOT").orNull
    ?: providers.environmentVariable("ANDROID_HOME").orNull
    ?: localSdkPath
val adbName = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) "adb.exe" else "adb"
val adbExecutable = androidSdkPath?.let { "$it/platform-tools/$adbName" }
val adbSerial = providers.environmentVariable("ANDROID_SERIAL").orNull

val prepareApiPortForwarding = tasks.register<Exec>("prepareApiPortForwarding") {
    group = "installation"
    description = "Forwards the backend port to connected Android devices."
    if (adbExecutable == null) {
        commandLine("adb", "reverse", "tcp:8081", "tcp:8081")
    } else if (adbSerial.isNullOrBlank()) {
        commandLine(adbExecutable, "reverse", "tcp:8081", "tcp:8081")
    } else {
        commandLine(adbExecutable, "-s", adbSerial, "reverse", "tcp:8081", "tcp:8081")
    }
    isIgnoreExitValue = true
}

tasks.configureEach {
    if (name == "installDebug") finalizedBy(prepareApiPortForwarding)
}
