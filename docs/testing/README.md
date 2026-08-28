# Testing Infrastructure for DailyPulse

## Overview

This directory contains the testing strategy and implementation for DailyPulse: JVM unit tests (`ktor-client-mock`), Android instrumented UI tests (`MockWebServer`), iOS XCUITests (`TestBffConfig` + in-process MockEngine), and Firebase Test Lab.

Codemagic (`codemagic.yaml` **kmp-workflow**) runs the JVM and iOS suites, publishes JUnit to the Tests tab, and downloads FTL media into `build/ftl-results`. See [RUNNING_TESTS.md](./RUNNING_TESTS.md#running-in-ci-codemagic).

## Files in this Directory

- **`TESTING_STRATEGY.md`** - Complete testing strategy, architecture, and rationale
- **`RUNNING_TESTS.md`** - Practical guide for running tests locally and in CI

## Quick Start

### 1. Run Unit Tests (Fast)
```bash
./gradlew :shared:testDebugUnitTest
```

### 2. Run Instrumented Tests (Requires Emulator)
```bash
./gradlew :androidApp:connectedMppDebugAndroidTest
```

### 3. Run on Firebase Test Lab
```bash
./gradlew :androidApp:assembleMppDebug :androidApp:assembleMppDebugAndroidTest
gcloud firebase test android run \
  --type instrumentation \
  --app androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk \
  --test androidApp/build/outputs/apk/androidTest/mpp/debug/androidApp-mpp-debug-androidTest.apk \
  --device model=MediumPhone.arm,version=34
```

## Architecture

### Three Test Layers

1. **Unit Tests (`commonTest`)** - Fast JVM tests using `ktor-client-mock`
   - Location: `shared/src/commonTest/`
   - Tests: Services, repositories, use cases
   - Speed: ~5-10 seconds
   - Coverage: Business logic, GraphQL parsing, error handling

2. **Instrumented Tests (`androidTest`)** - UI tests with MockWebServer
   - Location: `androidApp/src/androidTest/`
   - Tests: Complete UI flows, loading states, error handling
   - Speed: ~30-60 seconds (local emulator)
   - Coverage: End-to-end user journeys

3. **iOS XCUITests (`iosAppUITests`)** - Simulator UI tests with in-process GraphQL mocks
   - Location: `iosApp/iosAppUITests/`
   - Launch args: `-ui-testing`, `-ui-testing-scenario success|error`
   - Speed: ~1–2 minutes on Codemagic simulator
   - Coverage: Articles list, secondary article (scroll), backend error

4. **Firebase Test Lab** - Real device validation
   - Same Android instrumented tests, on cloud devices
   - Speed: ~5-10 minutes
   - Coverage: Device compatibility; CI copies screenshots/logcat/video to `build/ftl-results`

### Key Components

#### For Unit Tests
- `ktor-client-mock` - Mock Ktor HttpClient
- `GraphqlFixtures.kt` - Test data
- `ArticlesServiceTest.kt` - Example test

#### For Instrumented Tests
- `MockWebServer` - OkHttp mock server
- `TestBffConfig` - Runtime URL override
- `TestKoinModules` - DI configuration for tests
- `DailyPulseTestRunner` - Custom test runner
- `TestDailyPulseApp` - Test application
- `AndroidGraphqlFixtures.kt` - Test data
- `ArticlesScreenTest.kt` / `ArticlesScreenErrorTest.kt`

#### For iOS XCUITests
- `TestBffConfig.setUiTestScenario` - in-process MockEngine (XCUITest is a separate process)
- `iosApp/iOSApp.swift` - reads launch arguments before `initKoin()`
- `ArticlesScreenUITests.swift` / `UITestLaunch.swift`
- Accessibility: `articles_screen`, `articles_list`, `article_item`, `articles_error`

## Benefits

| Aspect | Before | After |
|--------|--------|-------|
| Test Coverage | 1 smoke test | 10+ tests across 3 layers |
| External Dependencies | Requires BFF running | Self-contained with mocks |
| CI Stability | Flaky (network issues) | Deterministic |
| Test Speed | Slow (waits for backend) | Fast (instant mock responses) |
| Developer Experience | Manual backend setup | Zero configuration |

## Implementation Details

### Runtime URL Override

The key innovation is `TestBffConfig`, which allows runtime override of the BFF URL:

```kotlin
// Production: uses compile-time baked URL
val url = TestBffConfig.getGraphqlUrl() // http://10.0.2.2:8080/graphql

// Tests: uses MockWebServer URL
TestBffConfig.setOverride("http://127.0.0.1:12345")
val url = TestBffConfig.getGraphqlUrl() // http://127.0.0.1:12345/graphql
```

### MockWebServer Dispatcher

Tests configure a dispatcher to handle different GraphQL queries:

```kotlin
mockWebServer.dispatcher = object : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        val body = request.body.readUtf8()
        return when {
            body.contains("aggregators") -> MockResponse().setBody(AGGREGATORS_SUCCESS)
            body.contains("articles") -> MockResponse().setBody(ARTICLES_SUCCESS)
            body.contains("sources") -> MockResponse().setBody(SOURCES_SUCCESS)
            else -> MockResponse().setResponseCode(404)
        }
    }
}
```

### Custom Test Runner

`DailyPulseTestRunner` ensures the test application (`TestDailyPulseApp`) is used instead of the production app, allowing test modules to be injected before Koin initializes.

## Migration Status

- ✅ Dependencies added (`ktor-client-mock`, `kotlinx-coroutines-test`, `mockwebserver`, `turbine`)
- ✅ `TestBffConfig` infrastructure created
- ✅ Services updated to use `TestBffConfig`
- ✅ Unit test fixtures created
- ✅ Example unit test (`ArticlesServiceTest`)
- ✅ Instrumented test infrastructure (runner, test app, modules)
- ✅ Example UI tests (`ArticlesScreenTest`, `ArticlesScreenErrorTest`)
- ✅ iOS XCUITests (`ArticlesScreenUITests`) with launch-arg mock injection
- ✅ Documentation complete
- ✅ CI: JVM unit tests, iOS `xcodebuild test`, FTL + JUnit/media artifacts on Codemagic
- 🔲 Additional unit tests for remaining services
- 🔲 Additional UI tests for SourcesScreen / AboutScreen

## Next Steps

1. **Verify the implementation:**
   ```bash
   # Sync Gradle dependencies
   ./gradlew --refresh-dependencies
   
   # Run unit tests
   ./gradlew :shared:testDebugUnitTest
   
   # Run instrumented tests (with emulator running)
   ./gradlew :androidApp:connectedMppDebugAndroidTest
   ```

2. **Expand test coverage:**
   - Add tests for `SourcesService`, `AggregatorService`
   - Add tests for repositories
   - Add UI tests for SourcesScreen, AboutScreen

3. **Integrate with CI:** already done in `codemagic.yaml` (JVM JUnit, iOS `.xcresult` → JUnit, FTL download). Optional: add a Codemagic Android emulator step for `connectedMppDebugAndroidTest` if you want instrumented tests without FTL.

## Troubleshooting

See `RUNNING_TESTS.md` for common issues and solutions.

## References

- [Ktor Client Testing](https://ktor.io/docs/http-client-testing.html)
- [MockWebServer GitHub](https://github.com/square/okhttp/tree/master/mockwebserver)
- [Koin Testing](https://insert-koin.io/docs/reference/koin-test/testing/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
