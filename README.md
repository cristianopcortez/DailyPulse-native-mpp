# DailyPulse — KMP Portfolio Edition

[![Codemagic build status](https://api.codemagic.io/apps/69f8c24fe73167699549e9f5/kmp-workflow/status_badge.svg)](https://codemagic.io/app/69f8c24fe73167699549e9f5/kmp-workflow/latest_build)

[Read this in Portuguese (PT-BR) →](./README.pt-BR.md)

> A news-reader app demonstrating **two ways of building UI on top of the same Kotlin Multiplatform business core**:
> 1. **Compose Multiplatform** — one Kotlin/Compose UI tree shipped to Android, iOS, Desktop and Web.
> 2. **Native UI** — Jetpack Compose on Android and SwiftUI on iOS, each consuming the same shared ViewModels.

The goal of this fork is not to teach KMP from scratch; it is to **showcase, in one repository, the architectural trade-offs between sharing the UI and keeping it native**, while reusing 100% of the business logic.

---

## Courses (Udemy)

The original **DailyPulse** exercise repository and its progressive branches are from **Petros Efthymiou**. The listings below are **paid courses** on Udemy:

1. [**Kotlin Multiplatform Masterclass — KMP, KMM — Android, iOS**](https://www.udemy.com/course/kotlin-multiplatform-masterclass/)
2. [**Full-stack Compose Multiplatform Masterclass — KMP**](https://www.udemy.com/course/fullstack-compose-multiplatform-masterclass-kmp/)

Upstream source code: [github.com/petros-efthymiou/DailyPulse](https://github.com/petros-efthymiou/DailyPulse). This fork adds the Android `native` / `mpp` flavor split, iOS UI switching, and portfolio-focused documentation.

The `kmp-workflow` name matches `workflows.kmp-workflow` in [`codemagic.yaml`](./codemagic.yaml).

---

## Table of contents

- [Courses (Udemy)](#courses-udemy)
- [What is shared, what is per-flavor](#what-is-shared-what-is-per-flavor)
- [Build flavors at a glance](#build-flavors-at-a-glance)
- [Project layout](#project-layout)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Running the app](#running-the-app)
  - [Android — `mpp` (Compose Multiplatform)](#android--mpp-compose-multiplatform)
  - [Android — `native` (Jetpack Compose)](#android--native-jetpack-compose)
  - [iOS — `mpp` (Compose Multiplatform)](#ios--mpp-compose-multiplatform)
  - [iOS — `native` (SwiftUI)](#ios--native-swiftui)
- [How the flavor switch works](#how-the-flavor-switch-works)
- [Verifying Gradle dependencies](#verifying-gradle-dependencies)
- [Course branches reference](#course-branches-reference)
- [Author](#author)
- [License](#license)

---

## What is shared, what is per-flavor

```text
┌─────────────────────────────────────────────────────────────────┐
│   :shared (Kotlin Multiplatform)                                │
│                                                                 │
│   commonMain                                                    │
│     ├── articles/  sources/   (UseCases, Repositories, DTOs)    │
│     ├── presentation/         (ArticlesViewModel, *State)       │
│     ├── di/                   (Koin modules)                    │
│     ├── db/                   (SQLDelight schema)               │
│     └── ui/                   (Compose Multiplatform screens)   │
│                               (consumed only by the mpp flavor) │
│                                                                 │
│   androidMain   iosMain   (Ktor engine, SQL driver, Platform)   │
└─────────────────────────────────────────────────────────────────┘
                              ▲                ▲
                              │                │
                ┌─────────────┘                └────────────┐
                │                                           │
   ┌────────────┴──────────┐                  ┌─────────────┴───────────┐
   │  androidApp           │                  │  iosApp                 │
   │  ┌────────┐ ┌───────┐ │                  │  ┌─────────┐ ┌────────┐ │
   │  │ src/   │ │ src/  │ │                  │  │ Content │ │ Native │ │
   │  │ mpp/   │ │native/│ │                  │  │ View    │ │ Root   │ │
   │  │  └ App │ │ └ JC  │ │                  │  │ (CMP)   │ │ View   │ │
   │  └────────┘ └───────┘ │                  │  └─────────┘ └────────┘ │
   └───────────────────────┘                  └─────────────────────────┘
        Android Product Flavors                  Swift compile flag
        (mpp / native)                           (-D MPP_UI)
```

Everything from the **ViewModel down** lives in `:shared/commonMain` and is reused by both flavors. The two UI flavors only differ in *how the same `ArticlesViewModel.articlesState` is rendered*.

---

## Build flavors at a glance

| Flavor | Android UI | iOS UI | Image loading | Navigation | Application ID |
|--------|------------|--------|---------------|------------|----------------|
| `mpp`    | Compose Multiplatform `App()` from `:shared` | `MainViewController()` from `:shared` (wrapped in `UIViewControllerRepresentable`) | Kamel | Voyager | `…dailypulse.android.mpp` |
| `native` | Jetpack Compose written in `androidApp/src/native/` | SwiftUI screens under `iosApp/iosApp/Screens/` | Coil (Android), `AsyncImage` (iOS) | `androidx.navigation.compose` (Android), `NavigationStack` (iOS) | `…dailypulse.android.native` |

Both Android flavors install side-by-side because they ship distinct application IDs.

---

## Project layout

```text
DailyPulse/
├── shared/                                  # Kotlin Multiplatform module
│   └── src/
│       ├── commonMain/kotlin/.../
│       │   ├── articles/                    # business logic (use cases, repo, VM)
│       │   ├── sources/
│       │   ├── di/                          # Koin modules (shared)
│       │   ├── db/                          # SQLDelight database
│       │   └── ui/                          # Compose Multiplatform screens (mpp only)
│       ├── androidMain/                     # Ktor Android engine, SQL driver
│       └── iosMain/                         # Ktor Darwin engine, SQL driver,
│                                            # KoinInitializer, MainViewController
│
├── androidApp/
│   └── src/
│       ├── main/                            # AndroidManifest, Application class,
│       │                                    # Koin modules shared by both flavors
│       ├── mpp/java/.../                    # ⇨ MainActivity that hosts shared App()
│       └── native/java/.../                 # ⇨ MainActivity + Jetpack Compose screens
│
├── iosApp/
│   └── iosApp/
│       ├── iOSApp.swift                     # Switches via #if MPP_UI
│       ├── ContentView.swift                # MPP entry (UIViewControllerRepresentable)
│       ├── NativeRootView.swift             # Native entry (NavigationStack)
│       └── Screens/                         # SwiftUI screens (consumed by native)
│
└── gradle/libs.versions.toml                # Single source of truth for versions
```

---

## Tech stack

| Layer | Library |
|-------|---------|
| Language | Kotlin 1.9.22, Swift 5 |
| Async | kotlinx.coroutines, kotlinx.datetime |
| Networking | Ktor 2.3 (Android engine + Darwin engine) |
| Persistence | SQLDelight 2.0 |
| DI | Koin 3.6 (`koin-core`, `koin-android`, `koin-compose`, `koin-androidx-compose`) |
| UI — `mpp` flavor | Compose Multiplatform 1.6.1, Voyager 1.1, Kamel |
| UI — `native` flavor (Android) | Jetpack Compose (Material 3 1.2.1), `androidx.navigation.compose`, Coil |
| UI — `native` flavor (iOS) | SwiftUI, `NavigationStack`, `AsyncImage` |

---

## Architecture

```
┌──────────────────────────────────────────────────────────┐
│  UI (Compose Multiplatform │ Jetpack Compose │ SwiftUI)  │  ← per flavor
├──────────────────────────────────────────────────────────┤
│  Presentation — ArticlesViewModel, SourcesViewModel       │  ── shared
│  Application  — UseCases, domain models                   │  ── shared
│  Data         — Repositories, Ktor service, SQLDelight    │  ── shared
│  Infrastructure (Platform, DB drivers, Ktor engine)       │  ── per platform
└──────────────────────────────────────────────────────────┘
```

The pattern is **Clean Architecture + MVI-style state**, with a single `StateFlow<XxxState>` per screen. Pull-to-refresh, error handling and loading states all live in shared code.

---

## Running the app

### Prerequisites

- JDK 17
- Android Studio Hedgehog (or newer) with the Android SDK 34
- Xcode 15+ (for the iOS targets)
- A `local.properties` with `sdk.dir=…`
- A News API key (the project ships with one for the course; replace as needed)

### Android — `mpp` (Compose Multiplatform)

```bash
./gradlew :androidApp:assembleMppDebug
./gradlew :androidApp:installMppDebug         # installs onto a connected device
```

Or, in Android Studio:

1. Open the Build Variants tool window (`View → Tool Windows → Build Variants`).
2. For the `androidApp` module, pick the variant **`mppDebug`**.
3. Run the `androidApp` configuration.

The application ID for this flavor is `com.petros.efthymiou.dailypulse.android.mpp`, so it can coexist with the native flavor on the same device.

### Android — `native` (Jetpack Compose)

```bash
./gradlew :androidApp:assembleNativeDebug
./gradlew :androidApp:installNativeDebug
```

Or pick variant **`nativeDebug`** in the Build Variants panel.

This flavor uses `androidx.compose.material3`, `androidx.navigation.compose`, Coil and `koin-androidx-compose`. It does **not** depend on Compose Multiplatform, Voyager or Kamel.

### iOS — `mpp` (Compose Multiplatform)

The iOS framework is built by Gradle and consumed by Xcode:

```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

Then in Xcode:

1. Open `iosApp/iosApp.xcodeproj`.
2. Select the `iosApp` target → **Build Settings** → **Other Swift Flags**.
3. Add `-D MPP_UI` for the *Debug* and *Release* configurations of the **MPP scheme** (see below).
4. Run the `iosApp` scheme.

`iOSApp.swift` will pick `ContentView()`, which wraps the Kotlin-side `MainViewController()` — the same Compose Multiplatform tree that runs on Android.

### iOS — `native` (SwiftUI)

1. Open `iosApp/iosApp.xcodeproj`.
2. Make sure `MPP_UI` is **not** defined in *Other Swift Flags* (this is the default).
3. Run the `iosApp` scheme.

`iOSApp.swift` will pick `NativeRootView()`, which renders the SwiftUI screens under `iosApp/iosApp/Screens/`. They consume `ArticlesViewModel`, `SourcesViewModel` and `Platform` directly from the shared framework via the `*Injector` Koin helpers.

> **Recommended Xcode setup**: duplicate the default `iosApp` scheme into two — `iosApp-MPP` and `iosApp-Native`. Add `-D MPP_UI` only to *Other Swift Flags* on `iosApp-MPP`. From then on, switching flavors on iOS is a one-click operation.

---

## How the flavor switch works

### Android (Gradle product flavors)

```kotlin
// androidApp/build.gradle.kts
android {
    flavorDimensions += "ui"
    productFlavors {
        create("mpp") {
            dimension = "ui"
            applicationIdSuffix = ".mpp"
            buildConfigField("String", "FLAVOR_LABEL", "\"Compose Multiplatform\"")
        }
        create("native") {
            dimension = "ui"
            applicationIdSuffix = ".native"
            buildConfigField("String", "FLAVOR_LABEL", "\"Jetpack Compose (Native)\"")
        }
    }
}

dependencies {
    "mppImplementation"(libs.koin.compose)
    "nativeImplementation"(libs.androidx.navigation.compose)
    "nativeImplementation"(libs.coil.compose)
    "nativeImplementation"(libs.koin.androidx.compose)
    "nativeImplementation"(libs.androidx.compose.material) // pull-to-refresh
}
```

`MainActivity` is **not** in `src/main/`. It exists once in `src/mpp/java/…` (delegating to `App()` from `:shared`) and once in `src/native/java/…` (driving an `androidx.navigation.compose` graph). The `AndroidManifest.xml` and `DailyPulseApp` Application class stay in `src/main/` and are shared.

### iOS (Swift compile flag)

```swift
// iosApp/iosApp/iOSApp.swift
@main
struct iOSApp: App {
    init() { KoinInitializerKt.doInitKoin() }
    var body: some Scene {
        WindowGroup {
            #if MPP_UI
            ContentView()        // Compose Multiplatform
            #else
            NativeRootView()     // SwiftUI
            #endif
        }
    }
}
```

The MPP entry point is the unchanged `ContentView` that wraps `MainIOSKt.MainViewController()`. The Native entry point is the new `NativeRootView`, which puts the existing SwiftUI screens (`ArticlesScreen`, `SourcesScreen`, `AboutScreen`) inside a `NavigationStack`.

---

## Verifying Gradle dependencies

The `gradle/libs.versions.toml` catalog acts as a single source of truth. The relevant additions for this portfolio fork are:

| Key | Used by | Purpose |
|-----|---------|---------|
| `androidx-navigation-compose` | `nativeImplementation` | Navigation in the native flavor |
| `coil-compose` | `nativeImplementation` | Image loading in the native flavor |
| `koin-androidx-compose` | `nativeImplementation` | `koinViewModel()` in `@Composable`s |
| `androidx-compose-material` | `nativeImplementation` | `pullrefresh` APIs for the native flavor |
| `koin-compose` | `mppImplementation` | `koinInject()` inside Compose Multiplatform |
| `compose.runtime/foundation/material3`, `voyager-*`, `kamel-image` | `:shared/commonMain` | Compose Multiplatform UI consumed by the mpp flavor |

To prove the matrix builds end-to-end:

```bash
./gradlew :androidApp:assembleMppDebug :androidApp:assembleNativeDebug
```

You should see two distinct APKs:

```
androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk
androidApp/build/outputs/apk/native/debug/androidApp-native-debug.apk
```

### Android instrumented tests / Firebase Test Lab

Build the **mpp** debug app plus its instrumentation APK (same pair `codemagic.yaml` uses for Firebase Test Lab):

```bash
./gradlew :androidApp:assembleMppDebug :androidApp:assembleMppDebugAndroidTest
```

On a device or emulator:

```bash
./gradlew :androidApp:connectedMppDebugAndroidTest
```

For `gcloud firebase test android run --type instrumentation`, the **app** APK stays under `outputs/apk/<flavor>/debug/`, but the **test** APK is emitted under `outputs/apk/androidTest/...` (not beside the app):

```
androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk
androidApp/build/outputs/apk/androidTest/mpp/debug/androidApp-mpp-debug-androidTest.apk
```

For the **native** flavor, use `assembleNativeDebug` / `assembleNativeDebugAndroidTest` (or `connectedNativeDebugAndroidTest`) and swap `mpp` for `native` in both paths.

---

## Course branches reference

This portfolio edition is built on top of the original course branches. They are kept intact for reference:

| Branch | Topic |
|--------|-------|
| `1_initial` | Project skeleton |
| `2_about_screen` | First shared screen (About / Platform info) |
| `3_articles_presentation_logic_and_UI` | Articles MVI pipeline |
| `4_articles_networking_and_business_logic` | Ktor + repository |
| `5_dependency_injection_with_koin` | Koin modules |
| `6_local_database_with_sql-delight` | SQLDelight + pull-to-refresh |
| `7_final_sources_feature` | Native UIs (Jetpack Compose + SwiftUI) |
| `8_compose_android_iOS` | **Compose Multiplatform on Android + iOS (this branch's base)** |
| `9_compose_desktop` | Desktop target (CMP) |
| `10_compose_web` | Web target (CMP / Wasm) |

---

## Author

Portfolio fork maintained by **[Cristiano Cortez](https://www.linkedin.com/in/cristianocortez/)**.

---

## License

```
Copyright (C) 2023 Petros Efthymiou Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
