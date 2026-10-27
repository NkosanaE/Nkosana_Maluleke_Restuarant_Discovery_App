# SOLUTION

## What was built

A production-minded vertical slice of a restaurant discovery feature:

1. **Browse screen** with a restaurant list, name search, and an "Open now" filter.
2. **Details screen** with restaurant info, image, and a favorite action.
3. Distinct **loading**, **empty**, and **problem** UI states.
4. **REST-style JSON integration** through an asynchronous mock remote data source.
5. Mandatory **offline baseline**: the last successful list is cached and available after a restart or while offline.
6. Measurable improvement: **favorites** that can be toggled from both screens and persist across app restarts.

---

## Architecture

The app is a single module with packages split by layer and feature, so each package maps cleanly onto a future Gradle module.

- `di/`: Hilt modules
  - Binds `RestaurantRemoteDataSource` to `MockRestaurantRemoteDataSource`
  - Provides the shared `Json` configuration and an `@IoDispatcher`-qualified dispatcher
- `data/remote/`: data source interface, mock implementation, and DTOs
- `data/RestaurantRepository`: single source of truth; fetches, maps, caches, and manages favorites
- `model/`: the `Restaurant` domain model. The UI never sees DTOs.
- `ui/browse/`, `ui/details/`: each feature owns its ViewModel, UI state, and screens
- `ui/common/`: shared `RestaurantCard` and formatting helpers
- `ui/RestaurantApp.kt`: navigation graph

### Patterns

- **Unidirectional data flow**: each ViewModel exposes a single `StateFlow` of UI state. Screens render that state and send user actions back as callbacks.
- **Route / Screen split**: the Route composable connects to the ViewModel; the Screen composable is stateless, which makes it easy to preview and test.
- **Lifecycle-aware collection** with `collectAsStateWithLifecycle`, so nothing is collected while the app is in the background.
- **Dependency injection with Hilt**: ViewModels use `@HiltViewModel` and are obtained with `hiltViewModel()`, scoped to their navigation back stack entry. The details screen reads its restaurant ID from `SavedStateHandle`, which Navigation fills from the route, so no custom ViewModel factories are needed.

---

## Key design decisions and trade-offs

| Decision | Why | Trade-off / next step |
|---|---|---|
| File-based JSON cache | Quick to build, easy to reason about, and clearly demonstrates the offline baseline | Room with indexed queries would be the production choice for large datasets |
| In-memory search and filtering | Fast and simple for a few hundred items | Does not scale to thousands of items; move filtering into the database or server with Paging 3 |
| Favorites stored separately from the feed | A refresh can never wipe the user's favorites, and favorites work offline | Favorites are device-local; a real app would sync them to the user's account |
| Mock data source behind an interface | The parsing and mapping code is exactly what a real API would use; only the transport is fake | Swapping in Retrofit means changing one Hilt binding |
| Hilt instead of a hand-written container | Matches how a multi-squad app manages dependencies: each feature module contributes its own bindings | Adds annotation processing (KSP) and some build-time cost |
| Bundled placeholder image for all restaurants | The demo works fully offline with no third-party image service | Less realistic; with a real backend, Coil would load real images with caching |
| Single module | Fits the timebox | Package boundaries already match the planned module split |

---

## Offline and resilience strategy

- On startup, the repository loads the last successful list from disk (`restaurants_cache.json`), so content appears immediately, even offline.
- A refresh fetches the JSON feed and maps it defensively:
  - Every DTO field is nullable.
  - Rows without an ID or name are dropped.
  - Missing cuisines, rating, fee, or ETA fall back to friendly text such as "ETA unavailable".
- Prices are kept as integer cents and only formatted to rands at display time.
- A successful refresh replaces the cache. A failed refresh leaves the cached list in place.
- Error presentation depends on whether data exists:
  - With no cached data, the user sees a full-screen error with **Try again**.
  - With cached data, the list stays visible and a snackbar offers **Retry**.
- Favorites are persisted separately (`favorite_restaurants.json`) and merged into the feed on every load.

---

## Keeping the UI responsive

- `LazyColumn` with stable keys (`restaurant.id`) so items are reused correctly when filters change the list.
- Search text and the "Open now" toggle are combined and passed through `flatMapLatest` into the repository. Each new input cancels the previous filter work, so typing stays responsive.
- UI state uses `stateIn(WhileSubscribed(5_000))`. State survives configuration changes such as rotation without refetching, and collection stops when the app is backgrounded.
- Network, disk, and parsing work run on an injected IO dispatcher, off the main thread.
- The dataset has 240 restaurants to give a realistic scrolling experience.

---

## Testing

Current unit tests (`app/src/test`):

- `BrowseViewModelTest`: search and "Open now" state changes, the error state after a failed refresh, and favorite toggles delegated to the repository
- `RestaurantDetailsViewModelTest`: loading a restaurant via `SavedStateHandle` and delegating favorite toggles
- `FormattersTest`: delivery fee, ETA, and rating formatting, including null fallbacks
- `MockRestaurantRemoteDataSourceTest`: dataset size, required fields, and unique restaurant names

ViewModel tests use MockK for the repository and `kotlinx-coroutines-test` with a main-dispatcher rule.

Next steps:

- Repository tests for mapping, cache read/write, and favorite merging, using a temporary directory
- Compose UI tests for the loading, empty, and error states and for the browse-to-details flow
- Hilt instrumentation tests with a fake data source bound in place of the mock

---

## How this would evolve in a large multi-squad app

### Module split

- `core:designsystem`: theme, tokens, and shared components such as `RestaurantCard`
- `core:navigation`: typed route and deep link contracts
- `core:data`: networking, serialization, persistence, and dispatcher setup
- `core:model`: shared domain models
- `feature:browse`, `feature:details`, `feature:favorites`

Each module contributes its own Hilt bindings, so squads can work independently without editing a central container.

### Navigation, state, and data boundaries

- Features expose navigation entry points through `core:navigation` contracts, never by depending on each other directly.
- Each feature owns its ViewModels and UI state.
- Repository interfaces live in a domain or API module. Implementations live in data modules and are bound with Hilt.

### Testing at scale

- Unit tests for mapping, repositories, and ViewModel state transitions
- Compose UI tests for critical screens, using stateless Screen composables
- Integration tests for navigation and the cache-first startup path
- Screenshot tests for design-system components

### Keeping the UI smooth as the list grows

- Paging 3 backed by Room, with database-side search and filtering
- Debounced search input
- Image requests sized to their target views
- Baseline Profiles, measured with Macrobenchmark

### AI-assisted development

- Use AI for boilerplate, mapping code, test scaffolding, and documentation drafts.
- Keep humans responsible for architecture and design decisions.
- Require generated code to pass the same gates as any other change: build, lint, tests, and code review.
- Write clear acceptance criteria first, so generated output can be checked against them.

---

## Possible next steps

- Persist the search query and filter through process death using `SavedStateHandle`
- Debounce search input
- Add deep link support for opening a restaurant directly
- Replace the file cache with Room and the mock source with a Retrofit client
