---
mode: 'agent'
description: 'Führt ein gründliches Code-Review mit Fokus auf Qualität und Fehlerfreiheit durch'
---

# Code Review – AeroGlide

Führe ein gründliches Code-Review der Datei `${file}` durch. Analysiere den Code systematisch anhand der folgenden Kategorien und gib für jeden gefundenen Punkt eine **konkrete Verbesserung mit Codebeispiel** an.

---

## 1. 🐛 Korrektheit & Fehlerfreiheit

- Gibt es **logische Fehler** oder falsche Annahmen?
- Können **NullPointerExceptions**, `IndexOutOfBoundsException` oder ähnliche Runtime-Fehler auftreten?
- Werden **Edge Cases** (leere Listen, null-Werte, leere Strings) korrekt behandelt?
- Gibt es **Race Conditions** oder **Concurrency-Probleme** (z.B. unsicherer Zugriff auf SharedState in Coroutines)?
- Werden **Coroutine-Scopes** korrekt verwendet (`viewModelScope`, kein `GlobalScope`)?
- Werden **Flows** korrekt gesammelt (kein `collect` ohne `lifecycleScope` oder `repeatOnLifecycle`)?

## 2. 🏗️ Architektur & Clean Architecture

- Hält der Code die **MVVM + Clean Architecture** Schichtentrennung ein?
- Greift die UI-Schicht direkt auf Daten zu, **ohne** das ViewModel zu nutzen?
- Befinden sich **Business-Logik-Teile** fälschlicherweise in Composables oder ViewModels statt in UseCases?
- Werden **Repository-Abstraktionen** korrekt verwendet?
- Sind **Hilt-Dependencies** korrekt injiziert (`@HiltViewModel`, `@Inject`, `@Singleton` etc.)?

## 3. 🎨 Jetpack Compose – Best Practices

- Werden **veraltete APIs** verwendet (z.B. `rememberSwipeToDismissBoxState`, `BackHandler` deprecated-Varianten)?
- Sind **State-Hoisting**-Prinzipien eingehalten (State so weit oben wie nötig)?
- Gibt es unnötige **Recompositions** durch instabile Parameter oder fehlende `remember`/`derivedStateOf`?
- Werden **`LaunchedEffect`-Keys** korrekt gesetzt, um unerwünschte Neuausführungen zu vermeiden?
- Sind **Side Effects** (`LaunchedEffect`, `SideEffect`, `DisposableEffect`) korrekt und sparsam eingesetzt?
- Werden **`collectAsStateWithLifecycle`** statt `collectAsState` verwendet?

## 4. ⚡ Performance

- Gibt es **teure Berechnungen** direkt in Composables ohne `remember` oder `derivedStateOf`?
- Werden **Lambdas in Composables** unnötig neu erstellt (fehlende `remember { {} }` Wrapping)?
- Gibt es **unnötige State-Updates**, die zu exzessiven Recompositions führen?
- Werden **`LazyColumn`/`LazyRow`**-Items korrekt mit stabilen `key`-Parametern versehen?

## 5. 🔒 Sicherheit & Datenschutz

- Werden **sensible Daten** (GPS-Koordinaten, Nutzerdaten) sicher gespeichert und übertragen?
- Gibt es **Logging von sensiblen Informationen** (z.B. `Log.d` mit Nutzerdaten)?
- Werden **Firebase/Crashlytics**-Daten DSGVO-konform behandelt?

## 6. 🧹 Code-Qualität & Lesbarkeit

- Gibt es **doppelten Code** (DRY-Prinzip verletzt)?
- Sind **Funktionen und Klassen** zu groß (Single Responsibility verletzt)?
- Sind **Namen** (Variablen, Funktionen, Klassen) aussagekräftig und selbstdokumentierend?
- Gibt es **unnötige Kommentare**, die offensichtlichen Code erklären?
- Fehlen **wichtige Kommentare** bei komplexer Logik?
- Werden **Magic Numbers/Strings** verwendet statt benannter Konstanten?

## 7. 🧪 Testbarkeit

- Ist der Code **unit-testbar** (Abhängigkeiten injizierbar, keine statischen Aufrufe)?
- Gibt es **fehlende Tests** für kritische Business-Logik?
- Sind **ViewModels** ohne Android-Framework-Abhängigkeiten testbar?

---

## Ausgabeformat

Strukturiere deine Antwort so:

### ✅ Gut gemacht
- Liste der positiven Aspekte

### 🔴 Kritisch (muss behoben werden)
Für jeden Punkt:
> **Problem:** Kurze Beschreibung  
> **Ursache:** Warum ist das ein Problem?  
> **Fix:**
> ```kotlin
> // Konkretes Codebeispiel
> ```

### 🟡 Verbesserungsvorschläge (sollte behoben werden)
Gleiche Struktur wie kritisch.

### 🔵 Hinweise (nice to have)
- Kleinere Optimierungen, Stil-Hinweise

### 📊 Gesamtbewertung
- **Qualität:** X/10
- **Fehlerrisiko:** Niedrig / Mittel / Hoch
- **Priorität für Refactoring:** Niedrig / Mittel / Hoch

