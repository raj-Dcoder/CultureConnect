import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("com.google.gms.google-services") // 👈 This MUST be applied
    id("com.google.dagger.hilt.android") // Apply Hilt
}

android {
    namespace = "com.rajveer.cultureconnect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rajveer.cultureconnect"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Read API key / Web Client ID from local.properties
        val properties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField(
            "String",
            "WEB_CLIENT_ID",
            "\"${properties.getProperty("WEB_CLIENT_ID", "")}\""
        )
        // Maps API Key for Places/Maps SDK (NEW)
        manifestPlaceholders["MAPS_API_KEY"] = properties.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Firebase Cloud Functions
    implementation("com.google.firebase:firebase-functions-ktx:20.4.0")

    // Accompanist - Permissions (for Compose permission handling)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
   

    // Google Places SDK (for location search/autocomplete)
    implementation("com.google.android.libraries.places:places:3.3.0")

    // Google Maps SDK (for showing maps)
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // For FlowRow layout (wrapping chips)
    implementation("com.google.accompanist:accompanist-flowlayout:0.32.0")

    // Google Sign-In via Credential Manager
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // coil - for image loading
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Extended icons library (contains SwapVert)
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    // datastore
    implementation(libs.androidx.datastore.preferences)
    implementation("androidx.datastore:datastore-core:1.1.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // In the 'dependencies' block of your app-level build.gradle
    implementation("com.google.android.gms:play-services-location:21.3.0")
}