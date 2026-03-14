# .github/copilot-instructions.md

## Projektkontext
Dies ist eine Android Kotlin App (Compose) für Segelflug- und Paragliding-Aktivitätstracking.
Module: app, core/*, feature/*

## Code-Konventionen
- Kotlin mit Coroutines & Flow
- Jetpack Compose für UI
- Hilt für Dependency Injection
- MVVM + Clean Architecture
- Keine Deprecated APIs verwenden (z.B. rememberSwipeToDismissBoxState)

## Bevorzugte Patterns
- StateFlow statt LiveData
- LaunchedEffect für Side Effects
- SnackBar/Toast für Nutzerfeedback
- Undo-Pattern: erst nach X Sekunden wirklich löschen
