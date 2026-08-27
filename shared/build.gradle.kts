import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.File
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sqlDelight)
    alias(libs.plugins.composeMultiplatformPlugin)
}

// ─────────────────────────────────────────────────────────────────────────────
// BFF base URL, resolved at compile time and baked into BffBuildConfig:
//   1. -Pbff.base.url / -PBFF_BASE_URL
//   2. env BFF_BASE_URL (Codemagic / CI)
//   3. local.properties → bff.base.url (per-machine, gitignored)
//   4. empty → each platform falls back to its emulator/simulator default
// ─────────────────────────────────────────────────────────────────────────────
fun resolveBffBaseUrl(): String {
    fun gradleProp(name: String) = (findProperty(name) as? String)?.trim().orEmpty()

    val fromGradle = gradleProp("bff.base.url").ifBlank { gradleProp("BFF_BASE_URL") }
    if (fromGradle.isNotBlank()) return fromGradle

    val fromEnv = System.getenv("BFF_BASE_URL")?.trim().orEmpty()
    if (fromEnv.isNotBlank()) return fromEnv

    val localFile = rootProject.file("local.properties")
    if (!localFile.exists()) return ""

    val localProps = Properties()
    localFile.reader().use { localProps.load(it) }
    return localProps.getProperty("bff.base.url")?.trim().orEmpty()
}

val bffConfigOutputDir = layout.buildDirectory.dir("generated/bffConfig/kotlin")

fun writeBffBuildConfig(outputDir: File, url: String) {
    require(url.isEmpty() || url.startsWith("http://") || url.startsWith("https://")) {
        "bff.base.url must be an http(s) URL, got: $url"
    }
    val escaped = url.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$")
    val source = """
        |package com.petros.efthymiou.dailypulse.network
        |
        |internal object BffBuildConfig {
        |    const val OVERRIDE_BASE_URL: String = "$escaped"
        |}
        |
    """.trimMargin()

    val target = outputDir
        .resolve("com/petros/efthymiou/dailypulse/network")
        .also { it.mkdirs() }
        .resolve("BffBuildConfig.kt")

    if (!target.exists() || target.readText() != source) {
        target.writeText(source)
    }
}

// Written during configuration as well, so the file always exists for IDE sync —
// iOS targets are disabled on non-macOS hosts and would never trigger the task.
writeBffBuildConfig(bffConfigOutputDir.get().asFile, resolveBffBaseUrl())

val generateBffConfig by tasks.registering {
    group = "build"
    description = "Bakes the BFF base URL into BffBuildConfig"
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        inputs.file(localFile)
    }
    inputs.property("bffBaseUrl", provider { resolveBffBaseUrl() })
    outputs.dir(bffConfigOutputDir)
    doLast {
        writeBffBuildConfig(bffConfigOutputDir.get().asFile, resolveBffBaseUrl())
    }
}

// iOS targets only on macOS. Declaring them on Windows makes Android Studio run
// transformNativeMainCInteropDependenciesMetadataForIde, which currently NPEs
// during Gradle sync (path == null) and leaves commonMain unresolved.
val isMacOs = System.getProperty("os.name").orEmpty().startsWith("Mac", ignoreCase = true)

kotlin {
    compilerOptions {
        // expect/actual classes are still Beta in K2; this flag is required so
        // Platform and BaseViewModel pair correctly instead of failing analysis.
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    if (isMacOs) {
        listOf(
            iosX64(),
            iosArm64(),
            iosSimulatorArm64()
        ).forEach {
            it.binaries.framework {
                baseName = "shared"
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.koin.core)
                implementation(libs.sql.coroutines.extensions)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.components.resources)
                implementation(compose.material3)
                implementation(libs.compose.material)
                implementation(libs.compose.material.icons.core)
                implementation(libs.koin.compose)
                implementation(libs.kamel.image)
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.transitions)
            }
        }

        androidMain {
            kotlin.srcDir(bffConfigOutputDir)
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodel.ktx)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sql.android.driver)
            }
        }

        if (isMacOs) {
            iosMain {
                kotlin.srcDir(bffConfigOutputDir)
                dependencies {
                    implementation(libs.ktor.client.darwin)
                    implementation(libs.sql.native.driver)
                }
            }
        } else {
            // Windows/Linux hosts omit iOS targets (see isMacOs above). Without an
            // iosMain source set, Android Studio folds src/iosMain into the Android
            // `main` compilation, and K2 reports expect/actual in the same module.
            val iosMain by creating {
                dependsOn(getByName("commonMain"))
                kotlin.srcDir("src/iosMain/kotlin")
            }
            iosMain.kotlin.srcDir(bffConfigOutputDir)
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool<*>>().configureEach {
    dependsOn(generateBffConfig)
}

android {
    namespace = "com.petros.efthymiou.dailypulse"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create(name = "DailyPulseDatabase") {
            packageName.set("petros.efthymiou.dailypulse.db")
        }
    }
}
