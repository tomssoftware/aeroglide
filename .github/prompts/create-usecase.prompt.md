---
mode: 'agent'
description: 'Erstellt einen neuen UseCase in core/domain – NiA-Stil oder Service-Coordinator'
---

# Create UseCase – AeroGlide

Erstelle einen neuen UseCase im Modul `core/domain`.

> ⚠️ **Zuerst Typ bestimmen!** Es gibt zwei grundlegend verschiedene Typen.
> Wähle anhand der Frage: *"Liefert dieser UseCase Daten, oder steuert er Hardware/Services?"*

---

## Eingabe

- **Name des UseCases**: $USECASE_NAME
- **Typ**: `[A] NiA-Stil` oder `[B] Service-Coordinator`
- **Kurzbeschreibung**: Was soll der UseCase tun?
- **Benötigte Dependencies**: $DEPENDENCIES

---

## Typ A – NiA-Stil UseCase ✅ (Standardfall)

**Wann:** Daten lesen, transformieren, kombinieren oder eine einmalige Aktion auslösen.
**Vorbild im Projekt:** `DownloadElevationUseCase.kt`, `CalibrationUseCase.kt`
**NiA-Referenz:** https://github.com/android/nowinandroid/tree/main/core/domain

### Klassenstruktur

```kotlin
package com.alpsfly.aeroglide.core.domain.usecase

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * [Kurzbeschreibung was der UseCase tut]
 */
class $USECASE_NAME @Inject constructor(
    private val someRepository: SomeRepository
) {
    // Für Daten-Queries: Flow zurückgeben
    operator fun invoke(param: ParamType): Flow<ResultType> =
        someRepository.getSomeData(param)

    // Für einmalige Aktionen: suspend fun oder Unit
    // operator fun invoke(param: ParamType): Unit { ... }
}
```

### Regeln für Typ A
- ✅ `operator fun invoke(...)` als einzige öffentliche Methode
- ✅ Stateless – **kein** `@Singleton`, kein eigener Zustand
- ✅ Gibt `Flow<T>` zurück (Query) oder `Unit`/`suspend` (Command)
- ✅ Kein `@Provides` in `DomainModule.kt` nötig – Hilt injiziert direkt via `@Inject constructor`
- ✅ Kein Timber-Logging erforderlich (kein eigener Zustand)
- ✅ Keine Business-Logik – nur Daten durchreichen oder transformieren

---

## Typ B – Service-Coordinator UseCase ⚠️ (bewusste Abweichung von NiA)

**Wann:** Langlebige Hardware-Ressourcen oder Background-Services koordinieren.
**Vorbild im Projekt:** `RecordingUseCase.kt`, `AutoStartUseCase.kt`, `VarioToneUseCase.kt`
**Begründung der Abweichung:** NiA hat keine Sensor-Hardware oder ForegroundServices.
Der Coordinator-Typ ist eine projektspezifische Erweiterung des NiA-Musters.

### Klassenstruktur

```kotlin
package com.alpsfly.aeroglide.core.domain.usecase

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Koordiniert [was koordiniert wird].
 * Singleton: registriert Callbacks einmalig und hält Hardware-Zustand.
 */
@Singleton
class $USECASE_NAME @Inject constructor(
    private val processor: SomeProcessor,           // Business-Logik-Worker
    private val locationServiceStarter: LocationServiceStarter  // Hardware (optional)
) {
    fun enable() {
        Timber.i("$USECASE_NAME: Commanding START.")
        processor.start()
        locationServiceStarter.startForegroundService()
    }

    fun disable() {
        Timber.i("$USECASE_NAME: Commanding STOP.")
        // Reihenfolge umkehren: zuerst Worker stoppen, dann Hardware freigeben
        processor.stop()
        locationServiceStarter.stopForegroundService()
    }
}
```

### Zustand exponieren (nur wenn nötig)

```kotlin
private val _isActive = MutableStateFlow(false)
val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
// MutableStateFlow ist immer private – nur das immutable StateFlow exponieren
```

### Hilt-DI in `DomainModule.kt` (nur für Typ B erforderlich)

```kotlin
@Provides
@Singleton
fun provide$USECASE_NAME(
    processor: SomeProcessor,
    locationServiceStarter: LocationServiceStarter
): $USECASE_NAME = $USECASE_NAME(processor, locationServiceStarter)
```

### Regeln für Typ B
- ✅ `@Singleton` – genau eine Instanz, Callbacks nur einmal registriert
- ✅ Kein `@HiltViewModel` – UseCases sind keine ViewModels
- ✅ Kein `GlobalScope` – nur `@ApplicationScope`-CoroutineScope wenn nötig
- ✅ Kein direkter Sensor-Zugriff – nur über `SensorRepository` / `LocationServiceStarter`
- ✅ Timber-Logging in jeder öffentlichen Methode
- ✅ Eintrag in `DomainModule.kt` erforderlich

---

## Gemeinsame Regeln (beide Typen)

- ✅ Liegt ausschließlich in `core/domain`
- ✅ Keine Android-UI-Imports (`Context`, `Activity`) – nur über Interfaces
- ✅ GPS-Koordinaten **niemals** loggen
- ✅ Keine deprecated APIs
- ✅ Business-Logik gehört in Processor-Klassen, **nicht** in den UseCase

---

## Checkliste vor Abgabe

- [ ] Typ A oder B bewusst gewählt und begründet
- [ ] **Typ A**: `operator fun invoke`, stateless, kein `@Singleton`, kein `DomainModule`-Eintrag
- [ ] **Typ B**: `@Singleton`, `enable()`/`disable()`, Timber-Logging, `DomainModule`-Eintrag
- [ ] Alle Dependencies sind Interfaces (testbar mit Fakes)
- [ ] Kein GPS-Logging
- [ ] Keine deprecated APIs

