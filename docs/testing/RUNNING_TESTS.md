# Running Tests - DailyPulse

This guide explains how to run the different types of tests in the DailyPulse project.

## Prerequisites

1. **For unit tests (commonTest):**
   - No special setup needed
   - Runs on JVM

2. **For instrumented tests (androidTest):**
   - Android emulator running OR physical device connected
   - MockWebServer handles network mocking automatically

3. **For Firebase Test Lab:**
   - `gcloud` CLI installed and authenticated
   - Firebase project configured (already done in codemagic.yaml)

## Running Tests Locally

### 1. Unit Tests (Fast - ~5-10 seconds)

Test services, repositories, and use cases without any device:

```bash
# Run all shared module unit tests
./gradlew :shared:testDebugUnitTest

# Run with coverage
./gradlew :shared:testDebugUnitTest --info

# Run specific test class
./gradlew :shared:testDebugUnitTest --tests "*.ArticlesServiceTest"
```

**What gets tested:**
- `ArticlesService` GraphQL parsing
- `SourcesService` API calls
- `ArticlesRepository` cache logic
- Error handling and edge cases

**Output:**
```
shared/build/reports/tests/testDebugUnitTest/index.html
```

### 2. Instrumented Tests (Medium - ~30-60 seconds)

UI tests with MockWebServer on emulator/device:

```bash
# First, start an emulator or connect a device
# Verify it's running:
adb devices

# Run all instrumented tests (mpp flavor)
./gradlew :androidApp:connectedMppDebugAndroidTest

# Run specific test class
./gradlew :androidApp:connectedMppDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.petros.efthymiou.dailypulse.android.screens.ArticlesScreenTest

# Run specific test method
./gradlew :androidApp:connectedMppDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.petros.efthymiou.dailypulse.android.screens.ArticlesScreenTest\#articlesScreen_displaysArticlesFromMockServer
```

**What gets tested:**
- ArticlesScreen displays articles correctly
- Loading indicators appear
- Error messages show when backend fails
- Pull-to-refresh works
- Navigation flows

**Output:**
```
androidApp/build/reports/androidTests/connected/mpp/debug/index.html
```

### 3. Firebase Test Lab (Slow - ~5-10 minutes)

Real device testing in the cloud:

```bash
# Build the APKs
./gradlew :androidApp:assembleMppDebug :androidApp:assembleMppDebugAndroidTest

# Upload to Firebase Test Lab
gcloud firebase test android run \
  --type instrumentation \
  --app androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk \
  --test androidApp/build/outputs/apk/androidTest/mpp/debug/androidApp-mpp-debug-androidTest.apk \
  --device model=MediumPhone.arm,version=34,locale=en,orientation=portrait

# Run on multiple devices
gcloud firebase test android run \
  --type instrumentation \
  --app androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk \
  --test androidApp/build/outputs/apk/androidTest/mpp/debug/androidApp-mpp-debug-androidTest.apk \
  --device model=MediumPhone.arm,version=34 \
  --device model=Pixel2,version=28 \
  --device model=MediumPhone.arm,version=30
```

**What gets tested:**
- Same as instrumented tests, but on real devices in the cloud
- MockWebServer runs on-device, no external dependencies

## Running in CI (Codemagic)

The CI pipeline is already configured in `codemagic.yaml`. It runs:

1. **Unit tests** - `./gradlew :shared:testDebugUnitTest`
2. **iOS tests** - `xcodebuild test`
3. **Firebase Test Lab** - Builds APKs and uploads to FTL

### Current Codemagic Configuration

The pipeline already includes Firebase Test Lab:

```yaml
- name: Run Tests on Firebase Test Lab (Android)
  script: |
    gcloud auth activate-service-account --key-file=./gcloud_key.json
    gcloud --quiet config set project dailypulse-kmp
    gcloud firebase test android run \
      --type instrumentation \
      --app androidApp/build/outputs/apk/mpp/debug/androidApp-mpp-debug.apk \
      --test androidApp/build/outputs/apk/androidTest/mpp/debug/androidApp-mpp-debug-androidTest.apk \
      --device model=MediumPhone.arm,version=34,locale=en,orientation=portrait
```

### Adding Emulator Tests to CI

To run instrumented tests on the Codemagic Mac emulator (faster than FTL):

```yaml
- name: Run Instrumented Tests (Emulator)
  script: |
    # Create and start emulator
    echo "no" | avdmanager create avd \
      --force \
      --name test_emulator \
      --abi google_apis/x86_64 \
      --package 'system-images;android-34;google_apis;x86_64'
    
    $ANDROID_HOME/emulator/emulator -avd test_emulator -no-window -no-audio -no-boot-anim &
    adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done;'
    
    # Run tests
    ./gradlew :androidApp:connectedMppDebugAndroidTest
    
    # Stop emulator
    adb emu kill
```

## Test Structure

```
DailyPulse/
├── shared/
│   └── src/
│       └── commonTest/
│           └── kotlin/.../
│               ├── articles/
│               │   └── data/
│               │       └── ArticlesServiceTest.kt        # ✅ Unit tests
│               └── fixtures/
│                   └── GraphqlFixtures.kt                # 📦 Test data
│
└── androidApp/
    └── src/
        └── androidTest/
            └── java/.../android/
                ├── di/
                │   └── TestKoinModules.kt                # 🔧 DI config
                ├── fixtures/
                │   └── AndroidGraphqlFixtures.kt         # 📦 Test data
                ├── screens/
                │   ├── ArticlesScreenTest.kt             # ✅ UI tests
                │   └── ArticlesScreenErrorTest.kt        # ✅ Error tests
                ├── DailyPulseTestRunner.kt               # 🏃 Test runner
                └── TestDailyPulseApp.kt                  # 📱 Test app
```

## Troubleshooting

### Unit Tests Fail with "Could not resolve ktor-client-mock"

**Solution:** Sync Gradle and ensure dependencies are downloaded:
```bash
./gradlew --refresh-dependencies
```

### Instrumented Tests Fail with "Koin not started"

**Solution:** Ensure the custom test runner is configured in `build.gradle.kts`:
```kotlin
testInstrumentationRunner = "com.petros.efthymiou.dailypulse.android.DailyPulseTestRunner"
```

### MockWebServer Port Already in Use

**Solution:** The test uses port 0 (random port). If it still fails:
```kotlin
mockWebServer.start(0) // Random port - should always work
```

### Tests Pass Locally but Fail on Firebase Test Lab

**Possible causes:**
1. **Device-specific behavior** - Test on similar device locally
2. **Timing issues** - Increase `waitUntil` timeouts
3. **Network configuration** - Verify MockWebServer URL is `127.0.0.1`, not `10.0.2.2`

### Articles Not Displaying in Tests

**Check:**
1. MockWebServer dispatcher is handling all required queries (aggregators, articles, sources)
2. TestBffConfig override is set before Koin initializes
3. JSON fixtures match the actual GraphQL schema

## Test Coverage

After running tests, view coverage reports:

```bash
# Generate coverage report for unit tests
./gradlew :shared:testDebugUnitTestCoverage

# Open report
open shared/build/reports/coverage/testDebugUnitTest/index.html
```

## Quick Reference

| Test Type | Command | Time | Requires |
|-----------|---------|------|----------|
| Unit tests | `./gradlew :shared:testDebugUnitTest` | ~10s | Nothing |
| Instrumented | `./gradlew :androidApp:connectedMppDebugAndroidTest` | ~60s | Emulator |
| Firebase Test Lab | `gcloud firebase test android run ...` | ~5min | gcloud CLI |

## Next Steps

1. **Add more unit tests** for repositories and use cases
2. **Add UI tests** for SourcesScreen, AboutScreen
3. **Add screenshot tests** using Paparazzi or Shot
4. **Integrate with PR checks** to run tests on every commit

## Resources

- [Ktor Client Mock](https://ktor.io/docs/http-client-testing.html)
- [MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver)
- [Firebase Test Lab](https://firebase.google.com/docs/test-lab)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
