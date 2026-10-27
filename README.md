# Mr D Android — Restaurant Discovery Mini-App

A focused native Android app built with **Kotlin + Jetpack Compose** for browsing restaurants and viewing their details. It includes search and filtering, an offline-first cache of the last successful list, and favorites that persist across restarts.

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- Hilt (dependency injection, with KSP)
- Coroutines + Flow
- Kotlinx Serialization (JSON)
- Coil (image loading)
- File-based local cache for the offline baseline and favorites
- JUnit, MockK and kotlinx-coroutines-test for unit tests

## Features

- **Browse screen** with a scrollable list of 240 restaurants showing name, cuisines, rating, ETA, open/closed status and delivery fee
- **Search** by restaurant name
- **"Open now"** filter toggle
- **Details screen** with the restaurant's info, image and favorite action
- **Distinct UI states**: loading, empty results, and an error state with retry
- **Offline baseline**: the last successful list is cached on disk and shown immediately on startup, including when offline
- **Favorites** (measurable improvement): toggle from either screen; persisted across app restarts
- **Mock REST-style JSON source** with simulated network latency

## Requirements

- A recent Android Studio version that supports AGP 9.2 and compileSdk 37
- An emulator or device running Android 7.0 (API 24) or higher

## Run Locally

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Select the `app` run configuration and run it on an emulator or device.

Or from the command line:

```bash
./gradlew installDebug   # build and install on a connected device/emulator
./gradlew test           # run unit tests
```

## Project Structure

```
app/src/main/java/.../
├── di/                 Hilt modules (bindings, JSON, IO dispatcher)
├── data/
│   ├── remote/         Data source interface, mock implementation, DTOs
│   └── RestaurantRepository.kt   Fetch, map, cache and favorites
├── model/              Domain model used by the UI
└── ui/
    ├── browse/         Browse screen + ViewModel
    ├── details/        Details screen + ViewModel
    ├── common/         Shared card component and formatters
    └── RestaurantApp.kt   Navigation graph
```

## Notes

- Restaurant data comes from an in-app mock source that returns REST-style JSON. It sits behind the `RestaurantRemoteDataSource` interface, so a real API client can replace it by changing a single Hilt binding.
- All restaurants use an image bundled with the app, so the demo works fully offline without depending on an external image service.
- See [SOLUTION.md](SOLUTION.md) for design decisions, trade-offs, and how this would evolve in a large multi-squad codebase.
