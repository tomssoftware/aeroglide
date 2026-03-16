---
mode: 'agent'
description: 'Architekturziele und Qualitätsprinzipien der AeroGlide App – Referenz für alle Architekturentscheidungen'
---

# Architecture Goals – AeroGlide

This document defines the architectural goals and quality priorities for the **AeroGlide** Android app (sailplane & paragliding activity tracker).  
Use it as a reference when making design decisions, reviewing code, or planning new features.

---

## 1. Quality Attribute Priorities (ISO 25010)

The following quality attributes are prioritised **top-down**. Higher-ranked attributes must never be sacrificed for lower-ranked ones without explicit justification.

| Priority | Quality Attribute             | Rationale for AeroGlide                                                                                          |
|----------|-------------------------------|------------------------------------------------------------------------------------------------------------------|
| 🥇 1     | **Reliability**               | Pilots depend on the app during actual flights. Data loss or crashes are safety-critical. Zero tolerance for silent failures or data corruption. |
| 🥇 1     | **Functionality**             | Core use cases (tracking, auto-start detection, variometer, live tracking) must work correctly under all conditions, including poor GPS signal, background restrictions, and low battery. |
| 🥇 1     | **Security (IT-Security)**    | GPS tracks and user data are sensitive. All data storage and transmission must be encrypted and GDPR-compliant. Firebase/Crashlytics data is anonymised. |
| 🥇 1     | **Efficiency**                | The app runs for hours on a single battery charge during a flight. CPU, sensor wake-locks, and network usage must be minimised at all times. |
| 🥈 2     | **Usability**                 | The UI must be operable with gloves, in direct sunlight, and under physical stress. Minimal interactions to start/stop recording. |
| 🥉 3     | **Maintainability**           | Clean Architecture and modular structure ensure the codebase remains comprehensible and extensible for future contributors. |
| 🥉 3     | **Portability**               | The app targets Android. Wear OS support is a future option. Business logic lives in platform-agnostic `core/domain` and `core/model` modules. |
| ➖ –     | **Compatibility**             | Minimum SDK is defined in `build.gradle.kts`. No specific multi-platform compatibility target beyond modern Android versions. |

---

## 2. Design Principles (Top-Down Priority)

These principles govern every implementation decision. Apply them **in order** – resolve conflicts by preferring the higher-ranked principle.

### 2.1 Single Source of Truth (SSOT)
Every piece of data or state has exactly **one authoritative owner**. All other components observe or derive from it.

- UI state is owned by a `ViewModel` and exposed via `StateFlow`.
- Persistent data is owned by a `Repository`; the database is the ground truth.
- Settings are read from `DataStore`; no in-memory caches without a defined invalidation strategy.

```kotlin
// ✅ SSOT – ViewModel owns the state; Compose observes it
val uiState: StateFlow<TrackingUiState> = …

// ❌ Anti-pattern – two independent sources that can diverge
var isRecording: Boolean = false               // local flag
val recordingState: StateFlow<Boolean> = …    // separate flow
```

### 2.2 KISS – Keep It Simple, Stupid
Prefer the simplest solution that correctly solves the problem.  
Avoid speculative abstraction, premature generalisation, and unnecessary indirection.

- A plain `data class` beats a complex hierarchy when there is only one variant today.
- A single `when` expression beats a Strategy pattern when there are ≤ 3 cases.

### 2.3 DRY – Don't Repeat Yourself
Every piece of knowledge exists in **exactly one place**.

- Shared business logic → `core/domain` UseCase.
- Shared UI components → `core/ui` Composable.
- Shared constants / thresholds → a named constant in the relevant module; never duplicated as a magic number.

### 2.4 YAGNI – You Ain't Gonna Need It
Implement a feature only when it is **actually required**, not when it might be useful someday.

- Do not add configuration parameters that are not yet used.
- Do not create abstract base classes for a single implementation.
- Do not add API endpoints or data fields "just in case".

### 2.5 SOLID Principles

| Principle                  | Application in AeroGlide                                                                                   |
|----------------------------|------------------------------------------------------------------------------------------------------------|
| **S** – Single Responsibility | Each UseCase does exactly one thing. Each Composable renders exactly one concern.                        |
| **O** – Open/Closed        | Extend behaviour via new UseCases / Composables; do not modify stable core modules.                        |
| **L** – Liskov Substitution | Interfaces in `core/domain` must be fully substitutable (important for unit tests with fakes).            |
| **I** – Interface Segregation | Split large interfaces (e.g. `SensorRepository`) into focused ones; clients only depend on what they need. |
| **D** – Dependency Inversion | All dependencies point inward toward `core/domain`. `feature/*` and `app` depend on abstractions, not implementations. |

### 2.6 Separation of Concerns
Strict layer separation following **Android Modern Architecture**:

```
UI Layer          →  Composable + ViewModel (feature/*)
Domain Layer      →  UseCase + DomainModel (core/domain)
Data Layer        →  Repository + DataSource (core/data, core/database, core/hardware)
```

- Composables must not contain business logic.
- UseCases must not import Android UI framework classes.
- Repositories must not be called directly from ViewModels when a UseCase exists.

### 2.7 Modularity
The project is divided into small, independently buildable modules:

```
app/                  – Entry point, DI wiring, navigation graph
core/model            – Pure Kotlin data models (no Android dependencies)
core/domain           – UseCases, business logic interfaces
core/data             – Repository implementations
core/database         – Room DAOs and database definition
core/hardware         – Sensor access (GPS, barometer, BLE)
core/ui               – Shared Compose components, themes
core/firebase         – Crashlytics, Analytics wrappers
core/mapbox           – Map rendering abstraction
feature/*             – Self-contained screens (ViewModel + UI)
```

**Rules:**
- `feature/*` modules must not depend on each other.
- `core/domain` and `core/model` must not depend on Android framework.
- Dependencies always flow **inward** (feature → core → model).

### 2.8 Abstraction
Expose only what callers need; hide implementation details behind interfaces.

- Hardware access (GPS, sensors) is wrapped in `core/hardware` interfaces → testable without real hardware.
- External SDKs (Mapbox, Firebase) are wrapped in `core/*` adapters → replaceable without touching features.

---

## 3. Android Modern Architecture Reference

Follow the official [Android Architecture Guidelines](https://developer.android.com/topic/architecture):

### 3.1 Layered Architecture
- **UI Layer**: Jetpack Compose + ViewModel. State exposed via `StateFlow`, collected with `collectAsStateWithLifecycle`.
- **Domain Layer**: Optional but required for complex business logic. Pure Kotlin UseCases.
- **Data Layer**: Repository pattern. Single source of truth per data type.

### 3.2 Unidirectional Data Flow (UDF)
```
Events  ──▶  ViewModel  ──▶  UiState (StateFlow)  ──▶  Composable
               │
               ▼
            UseCase  ──▶  Repository  ──▶  DataSource
```

### 3.3 Lifecycle-Aware Components
- Coroutines launched in `viewModelScope` (UI-bound) or `applicationScope` (flight-session-bound).
- `collectAsStateWithLifecycle` for all Flow→Compose bridges.
- `LaunchedEffect` for one-shot side effects tied to Compose lifecycle.
- `DisposableEffect` for cleanup (e.g. registering/unregistering sensor listeners).

### 3.4 Dependency Injection – Hilt
- `@HiltViewModel` for all ViewModels.
- `@Singleton` only for truly app-wide singletons (e.g. sensor coordinator, database).
- `@ActivityRetainedScoped` for session-scoped components.
- No `GlobalScope`; always use structured concurrency.

### 3.5 State Management
- `StateFlow` (not `LiveData`) for all observable state.
- `MutableStateFlow` is private; only immutable `StateFlow` is exposed.
- `sealed interface` for UiState with `Loading`, `Success`, and `Error` variants.

```kotlin
// ✅ Preferred state pattern
sealed interface TrackingUiState {
    data object Loading : TrackingUiState
    data class Success(val data: TrackingData) : TrackingUiState
    data class Error(@StringRes val messageRes: Int) : TrackingUiState
}

private val _uiState = MutableStateFlow<TrackingUiState>(TrackingUiState.Loading)
val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()
```

---

## 4. AeroGlide-Specific Architecture Rules

These rules are derived from the quality priorities above and are **non-negotiable**:

### 4.1 Flight Session Integrity (Reliability #1)
- A recording session must survive app process death and device restart.
- `WorkManager` or a Foreground Service with `START_STICKY` must be used for active tracking.
- All track points must be persisted atomically; partial writes are not acceptable.
- Use `Room` transactions for multi-table writes.

### 4.2 Battery & Sensor Efficiency (Efficiency #1)
- GPS sampling rate must be configurable and reduced automatically when stationary.
- Sensor wake-locks are acquired **only** during active sessions; always released in `onStop`/`DisposableEffect`.
- No network calls during active flight recording unless explicitly required (live tracking feature).
- Background processing uses `WorkManager` with appropriate constraints (charging, network).

### 4.3 Data Privacy & Security (Security #1)
- GPS coordinates are never logged to Logcat, Crashlytics, or analytics.
- User authentication tokens are stored in `EncryptedSharedPreferences` or Android Keystore.
- Cloud sync uses encrypted transport (TLS 1.2+).
- Firebase Crashlytics is configured without PII.

### 4.4 No Deprecated APIs
- Never use deprecated Android or Jetpack APIs (e.g. `rememberSwipeToDismissBoxState`).
- Check `@Deprecated` annotations and migration guides before using any Compose or AndroidX API.

### 4.5 Auto-Start Detection (Functionality #1)
- Auto-start logic lives exclusively in `core/domain` (`AutoStartDetector`, `AutoStartUseCase`).
- No direct sensor access from ViewModels or Composables.
- State machine transitions must be deterministic and unit-testable with a fake time provider.

---

## 5. When Reviewing Architecture Decisions

Ask these questions before accepting a change:

1. **Reliability**: Could this fail silently during a real flight? What happens if the process is killed?
2. **Single Source of Truth**: Is there only one owner for this state? Can two sources diverge?
3. **Layer violation**: Does this module import something it shouldn't?
4. **Battery impact**: Does this hold a wake-lock, register a sensor, or schedule polling longer than needed?
5. **Security**: Does this log, cache, or transmit location data?
6. **Complexity**: Is there a simpler solution that achieves the same result?

