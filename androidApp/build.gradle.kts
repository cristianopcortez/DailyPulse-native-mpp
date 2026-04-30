plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatformPlugin)
}

android {
    namespace = "com.petros.efthymiou.dailypulse.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.petros.efthymiou.dailypulse.android"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/**"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Product flavors: switch between Compose Multiplatform UI ("mpp") and
    // native Jetpack Compose UI ("native"). Shared business logic is reused
    // by both flavors via the :shared module.
    // ─────────────────────────────────────────────────────────────────────────
    flavorDimensions += "ui"
    productFlavors {
        create("mpp") {
            dimension = "ui"
            applicationIdSuffix = ".mpp"
            versionNameSuffix = "-mpp"
            buildConfigField("String", "FLAVOR_LABEL", "\"Compose Multiplatform\"")
        }
        create("native") {
            dimension = "ui"
            applicationIdSuffix = ".native"
            versionNameSuffix = "-native"
            buildConfigField("String", "FLAVOR_LABEL", "\"Jetpack Compose (Native)\"")
        }
    }
}

dependencies {
    implementation(projects.shared)

    // Common Android dependencies (used by both flavors)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)

    // ── MPP flavor only ─────────────────────────────────────────────────────
    // The Compose Multiplatform App() composable lives in :shared and pulls in
    // koin-compose / voyager / kamel transitively, so no extra deps are needed
    // here besides the koin-compose multiplatform binding.
    "mppImplementation"(libs.koin.compose)

    // ── Native flavor only ──────────────────────────────────────────────────
    // Pure Jetpack Compose stack: navigation-compose for in-app navigation,
    // Coil for image loading, koin-androidx-compose for ViewModel injection.
    // Catalog keys avoid libs.androidx.navigation.* / libs.androidx.compose.* so Studio
    // does not confuse version-catalog accessors with the JetBrains Compose Gradle DSL.
    "nativeImplementation"(libs.navigation.compose)
    "nativeImplementation"(libs.coil.compose)
    "nativeImplementation"(libs.koin.androidx.compose)
    "nativeImplementation"(libs.material.compose.android) // androidx.compose.material (pullrefresh)
}
