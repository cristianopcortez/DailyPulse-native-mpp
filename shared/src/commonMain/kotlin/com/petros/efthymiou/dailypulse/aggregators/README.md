# Aggregators Feature

This feature implements a news aggregator selector for the DailyPulse KMP app.

## Overview

The aggregators feature allows users to select their preferred news provider. Currently, this is preparation for step 2 where the selected aggregator will actually be used to fetch articles and sources.

## Architecture

The feature follows Clean Architecture principles with SOLID design:

```
aggregators/
├── application/           # Domain layer
│   ├── Aggregator.kt     # Domain model
│   └── AggregatorUseCase.kt  # Business logic
├── data/                 # Data layer
│   ├── AggregatorRaw.kt  # GraphQL DTO
│   ├── AggregatorsGraphqlData.kt  # GraphQL response wrapper
│   ├── AggregatorService.kt  # Network service
│   ├── AggregatorSettings.kt  # Persistence
│   └── AggregatorRepository.kt  # Repository pattern
├── di/                   # Dependency injection
│   └── AggregatorModule.kt  # Koin module
└── presentation/         # Presentation layer
    ├── AggregatorState.kt  # UI state
    └── AggregatorViewModel.kt  # ViewModel
```

## Features

### 1. GraphQL Query

Added `Aggregators` query to fetch available news providers:

```graphql
query Aggregators { 
  aggregators { 
    id 
    name 
  } 
}
```

### 2. Persistence

Selected aggregator ID is persisted in SQLite using a singleton table pattern:
- Default: "newsapi"
- Survives app restarts
- No network dependency for reading saved preference

### 3. UI

Clean dropdown selector accessible from the Articles screen toolbar:
- Shows available aggregators
- Displays currently selected provider
- Visual feedback for selection
- Loading and error states

### 4. Error Handling

- Graceful fallback to empty list if query fails offline
- Default "newsapi" used if no selection saved
- Consistent error handling with Articles and Sources features

## Current Limitations (Step 1)

1. The selected aggregator is **NOT** yet passed to Articles or Sources queries
2. Articles and Sources continue to use NewsAPI exclusively
3. The selector only saves the preference for future use

## Next Steps (Step 2)

When the BFF supports the `aggregator` argument:

1. Update `ArticlesService.fetchArticles()` to accept and pass `aggregator` parameter
2. Update `SourcesService.fetchSources()` to accept and pass `aggregator` parameter
3. Inject `AggregatorUseCase` into Articles and Sources use cases
4. Read selected aggregator ID and pass to service calls
5. Clear local caches when aggregator changes

## API Contract

### Request
```kotlin
// In AggregatorService
suspend fun fetchAggregators(): List<AggregatorRaw>
```

### Response (v1)
```json
{
  "data": {
    "aggregators": [
      { "id": "newsapi", "name": "NewsAPI" }
    ]
  }
}
```

### Future Response
When GNews and NewsData are added:
```json
{
  "data": {
    "aggregators": [
      { "id": "newsapi", "name": "NewsAPI" },
      { "id": "gnews", "name": "GNews" },
      { "id": "newsdata", "name": "NewsData" }
    ]
  }
}
```

## Usage

```kotlin
// Get aggregators
val aggregators = aggregatorUseCase.getAggregators()

// Get selected aggregator ID
val selectedId = aggregatorUseCase.getSelectedAggregatorId()  // "newsapi" by default

// Save selection
aggregatorUseCase.selectAggregator("newsapi")
```

## Testing

Since the project doesn't have existing tests, manual testing should verify:

1. Aggregator screen loads successfully
2. Default selection is shown when app first opens
3. Selection persists across app restarts
4. Offline behavior (empty list or cached selection)
5. Error states display correctly
6. Navigation works from Articles screen

## Database Schema

```sql
CREATE TABLE AggregatorSetting (
    id INTEGER NOT NULL PRIMARY KEY DEFAULT 1,
    selectedAggregatorId TEXT NOT NULL,
    CHECK (id = 1)
);
```

This ensures only one row exists (singleton pattern).
