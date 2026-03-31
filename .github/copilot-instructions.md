# .github/copilot-instructions.md

## Projektkontext
Dies ist eine Android Kotlin App (Compose) für Segelflug- und Paragliding-Aktivitätstracking.
Module: app, core/*, feature/*

Die Architektur orientiert sich am offiziellen Android-Referenzprojekt:
**Now in Android (NiA)**: https://github.com/android/nowinandroid

Alle Architekturentscheidungen sollen mit den NiA-Patterns konsistent sein,
sofern nicht explizit abweichend dokumentiert.

## Architektur-Prinzipien (aus NiA übernommen)

### Modul-Struktur
- `app/` – nur Einstiegspunkt, DI-Setup, Navigation-Graph
- `core/*` – wiederverwendbare, feature-unabhängige Bausteine (data, domain, model, ui, database, …)
- `feature/*` – genau ein fachliches Feature pro Modul, **kein** Feature importiert ein anderes Feature
- Abhängigkeitsrichtung: `feature → core`, niemals `core → feature`, niemals `feature → feature`

### ViewModel & UiState
- Jedes Feature hat sein eigenes `ViewModel` mit `@HiltViewModel`
- UiState immer als `sealed interface` mit `Loading`, `Success(data)`, `Error`
- StateFlow mit `SharingStarted.WhileSubscribed(5000)` und sinnvollem `initialValue`
- In Compose immer `collectAsStateWithLifecycle()` statt `collectAsState()`

### Repository & Domain
- Repositories liegen in `core/data`, sind Interfaces mit Impl-Klassen
- UseCases liegen in `core/domain`, sind schlanke Orchestratoren (keine Business-Logik)
- Business-Logik gehört in Processor-Klassen (z.B. `RecordingProcessor`, `AutoStartProcessor`)
- Kein direkter Repository-Zugriff aus dem ViewModel wenn ein UseCase existiert

### Navigation
- Jedes Feature-Modul exponiert eigene Extension-Funktionen auf `NavController` und `NavGraphBuilder`
- Keine hardcodierten Route-Strings außerhalb des jeweiligen `*Navigation.kt`

### Build
- Alle Gradle-Versionen über `gradle/libs.versions.toml` (Version Catalog)
- Convention Plugins nach NiA-Muster wo vorhanden

## Code-Konventionen
- Kotlin mit Coroutines & Flow
- Jetpack Compose für UI
- Hilt für Dependency Injection
- MVVM + Clean Architecture
- Keine Deprecated APIs verwenden (z.B. kein `rememberSwipeToDismissBoxState` aus alten APIs)

## Bevorzugte Patterns
- `StateFlow` statt `LiveData`
- `LaunchedEffect` für Side Effects (Navigation, Snackbar)
- Snackbar für Nutzerfeedback (kein Toast)
- Undo-Pattern: erst nach X Sekunden wirklich löschen (sofort aus UI entfernen, verzögert im Repository)
- Fake-Objekte in Tests bevorzugen vor Mocking-Frameworks wo möglich
- `runTest` + `advanceTimeBy` statt `runBlocking` + `Thread.sleep`

## Sicherheitsregeln
- GPS-Koordinaten und Positionsdaten **niemals** loggen (auch nicht mit Timber)
- Keine personenbezogenen Daten in Crash-Reports
