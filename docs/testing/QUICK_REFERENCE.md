# Testing Quick Reference

## 🚀 Commands

```bash
# Unit tests (10s, no device)
./gradlew :shared:testDebugUnitTest

# Instrumented tests (60s, needs emulator)
./gradlew :androidApp:connectedMppDebugAndroidTest

# Build for Firebase Test Lab
./gradlew :androidApp:assembleMppDebug :androidApp:assembleMppDebugAndroidTest
```

## 📁 File Locations

```
Test Code:
├── shared/src/commonTest/         → Unit tests
└── androidApp/src/androidTest/    → Instrumented tests

Test Data:
├── shared/src/commonTest/fixtures/GraphqlFixtures.kt
└── androidApp/src/androidTest/fixtures/AndroidGraphqlFixtures.kt

Infrastructure:
├── shared/src/commonMain/.../network/TestBffConfig.kt
├── androidApp/src/androidTest/.../DailyPulseTestRunner.kt
└── androidApp/src/androidTest/.../TestDailyPulseApp.kt
```

## 🧪 Test Types

| Type | Tool | Speed | Device? | What's tested |
|------|------|-------|---------|---------------|
| Unit | ktor-client-mock | ⚡⚡⚡ | No | Services, repos |
| Instrumented | MockWebServer | ⚡⚡ | Yes | UI flows |
| Firebase Test Lab | MockWebServer | ⚡ | Cloud | Real devices |

## 📊 Current Coverage

```
Unit Tests:
✅ ArticlesServiceTest (4)
✅ SourcesServiceTest (3)
🔲 AggregatorServiceTest (0)
Total: 7 tests

Instrumented Tests:
✅ ArticlesScreenTest (3)
✅ ArticlesScreenErrorTest (2)
🔲 SourcesScreenTest (0)
Total: 5 tests
```

## 🔧 How It Works

### Unit Tests
```kotlin
val mockEngine = MockEngine { request ->
    respond(content = GraphqlFixtures.ARTICLES_RESPONSE)
}
val client = HttpClient(mockEngine)
val service = ArticlesService(client)
val result = service.fetchArticles("tech") // ✅ Works without network
```

### Instrumented Tests
```kotlin
@Before
fun setUp() {
    mockWebServer.start(0)
    TestBffConfig.setOverride(mockWebServer.url("/").toString())
}

@Test
fun test() {
    mockWebServer.enqueue(MockResponse().setBody(ARTICLES_JSON))
    // Launch app, verify UI
}
```

## 🐛 Troubleshooting

| Problem | Solution |
|---------|----------|
| "Could not resolve ktor-client-mock" | `./gradlew --refresh-dependencies` |
| "Koin not started" | Check testInstrumentationRunner in build.gradle.kts |
| Tests pass locally, fail on FTL | Increase timeouts, use 127.0.0.1 not 10.0.2.2 |
| MockWebServer port conflict | Using port 0 (random) should fix it |

## 📚 Documentation

- **[README.md](./README.md)** - Start here
- **[TESTING_STRATEGY.md](./TESTING_STRATEGY.md)** - Architecture
- **[RUNNING_TESTS.md](./RUNNING_TESTS.md)** - Detailed guide
- **[RESUMO_PT-BR.md](./RESUMO_PT-BR.md)** - Portuguese summary

## 🎯 Key Files

### To Write New Unit Test
1. Create test file in `shared/src/commonTest/`
2. Use `MockEngine` from ktor-client-mock
3. Use fixtures from `GraphqlFixtures.kt`

### To Write New Instrumented Test
1. Create test file in `androidApp/src/androidTest/.../screens/`
2. Use `MockWebServer`
3. Use fixtures from `AndroidGraphqlFixtures.kt`
4. Set up with `TestBffConfig.setOverride()`

## ✅ Checklist for New Developer

- [ ] Read [`docs/testing/README.md`](./README.md)
- [ ] Run `./gradlew :shared:testDebugUnitTest`
- [ ] Start emulator
- [ ] Run `./gradlew :androidApp:connectedMppDebugAndroidTest`
- [ ] Review test examples:
  - [ ] `ArticlesServiceTest.kt`
  - [ ] `ArticlesScreenTest.kt`
- [ ] Try writing a new test

## 🎓 Examples to Study

```kotlin
// Unit test example
@Test
fun `fetchArticles returns parsed articles`() = runTest {
    val mockEngine = MockEngine { 
        respond(content = GraphqlFixtures.ARTICLES_RESPONSE)
    }
    val client = HttpClient(mockEngine) { /* config */ }
    val service = ArticlesService(client)
    
    val result = service.fetchArticles("tech")
    
    assertEquals(2, result.size)
}

// Instrumented test example
@Test
fun articlesScreen_displaysArticles() {
    mockWebServer.enqueue(
        MockResponse().setBody(AndroidGraphqlFixtures.ARTICLES_SUCCESS)
    )
    
    composeTestRule.setContent { App() }
    
    composeTestRule.onNodeWithText("Breaking News").assertIsDisplayed()
}
```

## 🔥 Pro Tips

1. **Unit tests are fast** - Write them first
2. **Use fixtures** - Don't hardcode JSON in tests
3. **Test errors** - Don't just test happy path
4. **Keep tests simple** - One assertion per test is OK
5. **Use descriptive names** - `test1()` ❌ → `fetchArticles_returnsEmptyList_whenNoData()` ✅

## 📈 Next Steps

1. Add more unit tests for remaining services
2. Add repository tests (cache logic)
3. Add ViewModel tests with Turbine
4. Add UI tests for remaining screens
5. Consider screenshot tests with Paparazzi

---

**Quick Links:**
- [View full strategy](./TESTING_STRATEGY.md)
- [Run tests guide](./RUNNING_TESTS.md)
- [Implementation summary](./IMPLEMENTATION_SUMMARY.md)
- [Portuguese summary](./RESUMO_PT-BR.md)
