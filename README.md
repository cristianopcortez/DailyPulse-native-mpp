# DailyPulse — KMP Portfolio Edition

**CI (Codemagic):** [`kmp-workflow`](./codemagic.yaml) on `main` — [pipeline overview & screenshots](#continuous-integration-codemagic) below.

[Read this in Portuguese (PT-BR) →](./README.pt-BR.md)

> A news-reader app demonstrating **two ways of building UI on top of the same Kotlin Multiplatform business core**:
> 1. **Compose Multiplatform** — one Kotlin/Compose UI tree shipped to Android, iOS, Desktop and Web.
> 2. **Native UI** — Jetpack Compose on Android and SwiftUI on iOS, each consuming the same shared ViewModels.

The goal of this fork is not to teach KMP from scratch; it is to **showcase, in one repository, the architectural trade-offs between sharing the UI and keeping it native**, while reusing 100% of the business logic.

---

## Continuous integration (Codemagic)

Pushes to **`main`** trigger the [`kmp-workflow`](./codemagic.yaml) pipeline (**KMP Build & Test Lab**). The workflow:

- Runs **JVM unit tests** (`./gradlew :shared:testDebugUnitTest`) and publishes JUnit XML to the Codemagic **Tests** tab
- Builds the Android **mpp** debug app and its instrumentation test APK
- Runs **iOS** XCTest + XCUITest on the simulator (`xcodebuild test`), converts `.xcresult` to JUnit, and extracts XCUITest screenshots
- Runs **Firebase Test Lab** instrumentation on Android, then downloads logcat, videos, and screenshots into `build/ftl-results`

Published **Artifacts** include `shared/build/reports/**`, `androidApp/build/reports/**`, `iosApp/TestResults.xcresult`, `iosApp/TestMedia/**`, `build/ftl-results/**`, and the APKs.

Codemagic build pages require a logged-in account, so the screenshots below document the last successful run for visitors browsing the repo on GitHub.

### Last successful run (build #25 · `main` · `c349b7f`)

**Build overview** — finished in ~12 minutes on a Mac mini M2:

![Codemagic build #25 overview — finished on main](docs/ci/codemagic-build-overview.png)

**Pipeline steps:**

![Codemagic build #25 steps — Android, iOS, Firebase Test Lab](docs/ci/codemagic-build-steps.png)

**Published artifacts** (APKs and bundled CI output):

![Codemagic build #25 artifacts](docs/ci/codemagic-artifacts.png)

---

## Courses (Udemy)

The original **DailyPulse** exercise repository and its progressive branches are from **Petros Efthymiou**. The listings below are **paid courses** on Udemy:

1. [**Kotlin Multiplatform Masterclass — KMP, KMM — Android, iOS**](https://www.udemy.com/course/kotlin-multiplatform-masterclass/)
2. [**Full-stack Compose Multiplatform Masterclass — KMP**](https://www.udemy.com/course/fullstack-compose-multiplatform-masterclass-kmp/)

Upstream source code: [github.com/petros-efthymiou/DailyPulse](https://github.com/petros-efthymiou/DailyPulse). This fork adds the Android `native` / `mpp` flavor split, iOS UI switching, and portfolio-focused documentation.

---

## Table of contents

- [Continuous integration (Codemagic)](#continuous-integration-codemagic)
- [Courses (Udemy)](#courses-udemy)
- [Testing](#testing)
- [What is shared, what is per-flavor](#what-is-shared-what-is-per-flavor)
- [Build flavors at a glance](#build-flavors-at-a-glance)
- [Project layout](#project-layout)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Backend (BFF)](#backend-bff)
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

## Testing

DailyPulse showcases a **production-grade multi-layered testing strategy** that eliminates flaky tests and external dependencies through intelligent network mocking.

### 🎯 Test Architecture

| Layer | Tool | Speed | Device? | Coverage | Tests |
|-------|------|-------|---------|----------|-------|
| **Unit** | `ktor-client-mock` | ⚡⚡⚡ ~10s | No | Services, repos, use cases | 7 tests |
| **Instrumented (Android)** | `MockWebServer` | ⚡⚡ ~60s | Emulator/Device | E2E UI flows, error states | 5 tests |
| **UI (iOS XCUITest)** | `TestBffConfig` + Ktor MockEngine | ⚡⚡ | Simulator | Articles list, scroll, backend error | 4 tests |
| **Firebase Test Lab** | `MockWebServer` | ⚡ ~5min | Real devices | Device compatibility | Same as Android instrumented |

**Total: 16 automated tests** running deterministically without any external backend.

### 🚀 Quick Start

```bash
# Unit tests (fast, no device needed)
./gradlew :shared:testDebugUnitTest

# Instrumented tests (requires emulator or physical device)
./gradlew :androidApp:connectedMppDebugAndroidTest

# Build APKs for Firebase Test Lab
./gradlew :androidApp:assembleMppDebug :androidApp:assembleMppDebugAndroidTest
```

### ✨ Key Innovations

#### 1. **TestBffConfig** - Runtime URL Override
```kotlin
// Production: uses compile-time baked URL
TestBffConfig.getGraphqlUrl() // http://10.0.2.2:8080/graphql

// Tests: MockWebServer injects its URL at runtime
TestBffConfig.setOverride("http://127.0.0.1:12345")
TestBffConfig.getGraphqlUrl() // http://127.0.0.1:12345/graphql
```
No build flavors or compile-time flags needed—tests inject mock URLs seamlessly.

On **iOS XCUITest**, the test process cannot call Kotlin. The app is launched with `-ui-testing` / `UI_TESTING_SCENARIO`; `iOSApp.swift` calls `TestBffConfig.setOverride` and `setUiTestScenario` before Koin so GraphQL is served in-process.

#### 2. **MockWebServer Intelligent Dispatcher**
```kotlin
mockWebServer.dispatcher = object : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        val body = request.body.readUtf8()
        return when {
            body.contains("articles") -> MockResponse().setBody(ARTICLES_JSON)
            body.contains("sources") -> MockResponse().setBody(SOURCES_JSON)
            body.contains("aggregators") -> MockResponse().setBody(AGGREGATORS_JSON)
            else -> MockResponse().setResponseCode(404)
        }
    }
}
```
Single mock server handles all GraphQL queries automatically.

#### 3. **Custom Test Runner** - DI Injection Before App Init
```kotlin
class DailyPulseTestRunner : AndroidJUnitRunner() {
    override fun newApplication(...): Application {
        return super.newApplication(cl, TestDailyPulseApp::class.java.name, context)
    }
}
```
Injects test Koin modules before the app initializes, enabling complete DI override.

### 📊 Impact & Results

#### Before Testing Implementation
- ❌ 1 smoke test only (`MainActivity` launches)
- ❌ Required external BFF running (`http://10.0.2.2:8080`)
- ❌ Flaky CI runs due to network dependencies
- ❌ Manual setup required (~10 minutes)
- ❌ No coverage for GraphQL parsing, error states, UI flows

#### After Testing Implementation
- ✅ **16 automated tests** (7 unit + 5 Android instrumented + 4 iOS XCUITest)
- ✅ **Zero external dependencies** - all tests self-contained
- ✅ **100% deterministic** - same results every run
- ✅ **Zero configuration** - just `./gradlew test` (Android) or `xcodebuild test` (iOS)
- ✅ **Stable on physical devices** - tested on Moto G(6) Plus, multiple emulators
- ✅ **CI-ready** - JVM, iOS simulator, and Firebase Test Lab on Codemagic, with JUnit + media artifacts
- ✅ **Well-documented** - guides in [`docs/testing/`](./docs/testing/)

### 🧪 What Gets Tested

#### Unit Tests (`shared/src/commonTest/`)
- ✅ **ArticlesServiceTest** (4 tests)
  - GraphQL response parsing
  - Empty article lists
  - Variable passing (aggregator, source)
  - Error handling
- ✅ **SourcesServiceTest** (3 tests)
  - Sources parsing
  - Aggregator filter
  - Empty responses

#### Instrumented Tests (`androidApp/src/androidTest/`)
- ✅ **ArticlesScreenTest** (3 tests)
  - Articles display from MockWebServer
  - Article descriptions render
  - Loading states
- ✅ **ArticlesScreenErrorTest** (2 tests)
  - Error message display on server errors
  - Empty state handling

#### iOS XCUITests (`iosApp/iosAppUITests/`)
- ✅ **iosAppUITests** — app reaches foreground with mocked GraphQL
- ✅ **ArticlesScreenUITests** — list from mock, secondary article (scroll-tolerant), backend error

All tests use **centralized fixtures** (`GraphqlFixtures.kt`, `AndroidGraphqlFixtures.kt`, `UiTestGraphqlFixtures.kt`) for maintainable test data.

### 📁 Test Infrastructure

```
DailyPulse/
├── shared/src/
│   ├── commonMain/.../network/
│   │   └── TestBffConfig.kt              # Runtime URL override
│   └── commonTest/
│       ├── fixtures/GraphqlFixtures.kt    # Centralized test data
│       ├── articles/data/
│       │   └── ArticlesServiceTest.kt     # 4 unit tests
│       └── sources/data/
│           └── SourcesServiceTest.kt      # 3 unit tests
│
├── androidApp/src/androidTest/
│   ├── DailyPulseTestRunner.kt            # Custom AndroidJUnitRunner
│   ├── TestDailyPulseApp.kt               # Test Application with DI override
│   ├── di/TestKoinModules.kt              # Test Koin configuration
│   ├── fixtures/AndroidGraphqlFixtures.kt # UI test data
│   └── screens/
│       ├── ArticlesScreenTest.kt          # 3 UI tests
│       └── ArticlesScreenErrorTest.kt     # 2 error tests
│
└── iosApp/iosAppUITests/
    ├── UITestLaunch.swift                 # -ui-testing launch args
    ├── iosAppUITests.swift                # smoke
    └── ArticlesScreenUITests.swift        # list / scroll / error
```

### 🎓 Complete Documentation

Comprehensive testing guides available in [`docs/testing/`](./docs/testing/):

| Document | Description | Lines |
|----------|-------------|-------|
| **[README.md](./docs/testing/README.md)** | Quick start, overview, and architecture | 400+ |
| **[TESTING_STRATEGY.md](./docs/testing/TESTING_STRATEGY.md)** | Detailed strategy, rationale, migration path | 650+ |
| **[RUNNING_TESTS.md](./docs/testing/RUNNING_TESTS.md)** | Practical guide with troubleshooting | 420+ |
| **[IMPLEMENTATION_SUMMARY.md](./docs/testing/IMPLEMENTATION_SUMMARY.md)** | Complete structure reference | 280+ |
| **[QUICK_REFERENCE.md](./docs/testing/QUICK_REFERENCE.md)** | Command cheat sheet | 150+ |
| **[RESUMO_PT-BR.md](./docs/testing/RESUMO_PT-BR.md)** | Portuguese executive summary | 450+ |

**Total: 2,600+ lines of documentation** with examples, diagrams, and troubleshooting guides.

### 🔧 Physical Device Testing

Tests are **stable on physical devices** (validated on Moto G(6) Plus, API 28):

- Uses `assertExists()` instead of `assertIsDisplayed()` for viewport clipping
- `waitUntil` with 10s timeout for async data flow (Network → SQLDelight → StateFlow → UI)
- `performScrollToNode()` for LazyColumn items below the fold
- Handles TopAppBar insets and device-specific layout variations
- Explicit OkHttp 4.12.0 dependency resolution to prevent `NoClassDefFoundError`
- Database cleanup in test setup ensures isolation

### 📦 Dependencies

```kotlin
// gradle/libs.versions.toml
ktor-client-mock = "3.5.2"    // Unit test mocking
kotlinx-coroutines-test = "1.11.0" // runTest in commonTest
mockwebserver = "4.12.0"       // Instrumented test mocking
turbine = "1.2.0"              // Flow testing (future use)
```

### 🎯 Why This Matters for Portfolio Projects

This testing infrastructure demonstrates:

1. **Production-grade testing practices** - not just toy examples
2. **Understanding of test pyramids** - fast unit tests, strategic E2E tests
3. **Solving real problems** - flaky tests, external dependencies, device variations
4. **Documentation skills** - comprehensive guides for team onboarding
5. **CI/CD integration** - automated testing in real pipelines
6. **Multiplatform testing** - testing shared KMP business logic

Perfect showcase for **senior-level mobile engineering** positions requiring:
- Clean Architecture testing
- Dependency injection testing
- Network mocking strategies
- CI/CD pipeline design
- Technical documentation

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
| Build | Gradle 9.5.1, AGP 8.13.2, JDK 17 |
| Language | Kotlin 2.4.10 (K2), Swift 5 |
| Async | kotlinx.coroutines 1.11, kotlinx.datetime 0.8 (`kotlin.time`) |
| Networking | Ktor 3.5 (Android engine + Darwin engine) → GraphQL BFF (not NewsAPI) |
| Persistence | SQLDelight 2.3 |
| DI | Koin 4.2 (`koin-core`, `koin-android`, `koin-compose`, `koin-androidx-compose`) |
| UI — `mpp` flavor | Compose Multiplatform 1.11.1, Voyager 2.2, Kamel 1.0 |
| UI — `native` flavor (Android) | Jetpack Compose (Material 3 1.4.0), `androidx.navigation.compose`, Coil |
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

## Backend (BFF)

Articles and Sources are loaded from a **GraphQL BFF** (Ktor), not from NewsAPI in the client. The Android app (and iOS, via `:shared`) posts to `POST /graphql`.

BFF repository: **[github.com/cristianopcortez/daily-pulse-bff](https://github.com/cristianopcortez/daily-pulse-bff)**

Run the BFF locally (`./gradlew run`, port **8080**) before launching the app. Debug defaults: Android emulator `http://10.0.2.2:8080`, iOS Simulator `http://localhost:8080`. Optional machine override in `local.properties` (gitignored):

```properties
bff.base.url=http://192.168.x.x:8080
```

About stays fully local on the device.

---

## Running the app

### Prerequisites

- JDK 17
- Android Studio Hedgehog (or newer) with the Android SDK 34
- Xcode 15+ (for the iOS targets)
- A `local.properties` with `sdk.dir=…`
- The [Daily Pulse BFF](https://github.com/cristianopcortez/daily-pulse-bff) running locally on port 8080 (see [Backend (BFF)](#backend-bff))

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
