# Testing Strategy - DailyPulse

## Overview

This document outlines the testing strategy for DailyPulse, combining three layers:

1. **Unit tests (commonTest)** - Fast, JVM-based tests using `ktor-client-mock`
2. **Instrumented tests (androidTest)** - UI tests with `MockWebServer` on emulator/device
3. **Firebase Test Lab** - Real device validation with mock server on-device

## Architecture for Testing

### Current State
- BFF URL is baked at compile time (`BffBuildConfig`)
- No test hooks for runtime URL override
- Single smoke test in `androidTest`

### Testing Solution

```
┌─────────────────────────────────────────────────────────┐
│                     Application Layer                    │
│              (reads from TestBffConfig)                  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   TestBffConfig.kt                       │
│  • Production: uses BffBuildConfig.OVERRIDE_BASE_URL     │
│  • Test: uses runtime override via companion var         │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    Koin DI Layer                         │
│  • Test modules can inject MockWebServer URL             │
│  • Or inject mocked HttpClient directly                  │
└─────────────────────────────────────────────────────────┘
```

## Layer 1: commonTest + ktor-client-mock

**Goal**: Test services, repositories, use cases without network

```kotlin
// shared/src/commonTest/kotlin/...
@Test
fun `fetchArticles returns parsed articles`() = runTest {
    val mockEngine = MockEngine { request ->
        respond(
            content = articlesGraphqlResponse,
            status = HttpStatusCode.OK,
            headers = headersOf("Content-Type", "application/json")
        )
    }
    
    val client = HttpClient(mockEngine)
    val service = ArticlesService(client)
    
    val result = service.fetchArticles("tech")
    assertEquals(2, result.size)
}
```

**Advantages**:
- Runs in `./gradlew :shared:testDebugUnitTest` (fast)
- No emulator/device needed
- Tests GraphQL parsing, error handling, edge cases

**Coverage**:
- ✅ `ArticlesService.fetchArticles()`
- ✅ `SourcesService.fetchSources()`
- ✅ `AggregatorService.fetchAggregators()`
- ✅ `ArticlesRepository` cache logic
- ✅ Error scenarios (network failures, malformed JSON)

## Layer 2: androidTest + MockWebServer

**Goal**: End-to-end UI tests with controlled backend responses

```kotlin
// androidApp/src/androidTest/.../ArticlesScreenTest.kt
@RunWith(AndroidJUnit4::class)
class ArticlesScreenTest {
    
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    private lateinit var mockWebServer: MockWebServer
    
    @Before
    fun setUp() {
        mockWebServer = MockWebServer().apply {
            start(0) // random port
        }
        
        // Inject mock URL into Koin
        loadKoinModules(testNetworkModule(mockWebServer.url("/").toString()))
    }
    
    @Test
    fun articlesScreen_displaysArticles() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(articlesGraphqlFixture)
        )
        
        composeTestRule.onNodeWithText("Breaking News").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tech Article").assertIsDisplayed()
    }
}
```

**Where it runs**:
- Local emulator: `./gradlew :androidApp:connectedMppDebugAndroidTest`
- Codemagic CI: same command on Mac emulator
- Firebase Test Lab: APK upload (mock runs on-device)

**Coverage**:
- ✅ ArticlesScreen displays articles
- ✅ Loading indicator appears
- ✅ Error messages display correctly
- ✅ Pull-to-refresh works
- ✅ Navigation to AboutScreen / SourcesScreen

## Layer 3: Firebase Test Lab Integration

**Current setup** (from `codemagic.yaml`):
```yaml
- name: Run Tests on Firebase Test Lab (Android)
  script: |
    gcloud firebase test android run \
      --type instrumentation \
      --app androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk \
      --test androidApp/build/outputs/apk/androidTest/mpp/debug/androidApp-mpp-debug-androidTest.apk \
      --device model=MediumPhone.arm,version=34,locale=en,orientation=portrait
```

**With MockWebServer**:
- MockWebServer runs **inside the test APK** on the device
- No external backend needed
- Tests run deterministically on real hardware

## Implementation Checklist

### Phase 1: Prepare Infrastructure
- [x] Document testing strategy
- [ ] Add dependencies to `libs.versions.toml`
- [ ] Create `TestBffConfig` with runtime override
- [ ] Create `testNetworkModule` for Koin DI override

### Phase 2: Unit Tests (commonTest)
- [ ] Add `ktor-client-mock` dependency
- [ ] Create test fixtures (GraphQL JSON responses)
- [ ] Write `ArticlesServiceTest`
- [ ] Write `SourcesServiceTest`
- [ ] Write `ArticlesRepositoryTest`

### Phase 3: Instrumented Tests (androidTest)
- [ ] Add `mockwebserver` dependency
- [ ] Create `TestDailyPulseApp` or injection mechanism
- [ ] Write `ArticlesScreenTest`
- [ ] Write `SourcesScreenTest`
- [x] Codemagic: JVM unit tests, iOS `xcodebuild test`, FTL (not local `connectedAndroidTest` on Mac M2)

### Phase 4: Firebase Test Lab
- [ ] Verify MockWebServer works on FTL devices
- [ ] Update documentation with FTL results

## Dependencies Required

```toml
# gradle/libs.versions.toml
[versions]
mockwebserver = "4.12.0"  # or latest
turbine = "1.2.0"          # for Flow testing (optional)

[libraries]
ktor-client-mock = { module = "io.ktor:ktor-client-mock", version.ref = "ktorClient" }
mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "mockwebserver" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
```

## Test Fixtures Location

```
shared/src/commonTest/kotlin/
└── com/petros/efthymiou/dailypulse/
    └── fixtures/
        ├── ArticlesFixtures.kt      # GraphQL responses
        ├── SourcesFixtures.kt
        └── AggregatorsFixtures.kt

androidApp/src/androidTest/java/
└── com/petros/efthymiou/dailypulse/android/
    ├── di/
    │   └── TestKoinModule.kt        # Override networkModule
    ├── screens/
    │   ├── ArticlesScreenTest.kt
    │   └── SourcesScreenTest.kt
    └── fixtures/
        └── GraphqlFixtures.kt       # Shared with commonTest
```

## Running Tests

```bash
# Unit tests (fast)
./gradlew :shared:testDebugUnitTest

# Instrumented tests (local emulator)
./gradlew :androidApp:connectedMppDebugAndroidTest

# Build APKs for Firebase Test Lab
./gradlew :androidApp:assembleMppDebug :androidApp:assembleMppDebugAndroidTest

# Firebase Test Lab (via gcloud)
gcloud firebase test android run \
  --type instrumentation \
  --app androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk \
  --test androidApp/build/outputs/apk/androidTest/mpp/debug/androidApp-mpp-debug-androidTest.apk \
  --device model=MediumPhone.arm,version=34
```

## Benefits

| Layer | Speed | Cost | Coverage | Determinism |
|-------|-------|------|----------|-------------|
| commonTest | ⚡⚡⚡ | Free | Business logic | 100% |
| androidTest (emulator) | ⚡⚡ | Free | UI + integration | 100% |
| Firebase Test Lab | ⚡ | Paid | Real devices | 100% with mock |

## Migration Path

### Current (now)
- 1 smoke test (`MainActivity` launches)
- No network mocking
- FTL runs smoke test only

### After Phase 2
- ~10-15 unit tests in `commonTest`
- Services fully tested
- No device/emulator needed for most coverage

### After Phase 3
- 3-5 UI tests with MockWebServer
- Runs on local emulator + CI emulator
- Deterministic, no external dependencies

### After Phase 4
- Same tests run on FTL real devices
- Validates real-world scenarios
- No backend dependency in CI

## Notes

- **Why not just use real backend?** Flaky tests, slow, requires deployment, non-deterministic
- **Why not just unit tests?** UI regressions won't be caught
- **Why MockWebServer AND ktor-client-mock?** Different layers — client-mock for unit, MockWebServer for E2E
- **BFF_BASE_URL in CI?** Still useful for manual testing or staging environment tests; MockWebServer is for automation
