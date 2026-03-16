# Plan: Vario-Ton (VarioTone)

Implementiere ein akustisches Variometer-Signal nach dem Vorbild des [LX Navigation Navia Vario-Indicators](https://lxnavigation.com/product/navia-vario-indicator/).
Der Ton wird in 3 Schritten umgesetzt. Halte dabei alle Architekturprinzipien aus `@app-arch-goals.prompt.md` ein (SSOT, Separation of Concerns, KISS, kein Deprecated API).

---

## Datenbasis & Bestandscode

| Artefakt | Pfad | Rolle |
|---|---|---|
| `VarioTone` Interface + `VarioToneImpl` | `core/common/src/.../audio/VarioTone.kt` | Generator (Step 1) |
| `climbrateFlowUi: Flow<Climbrate>` | `core/data/SensorRepository.kt` | 1-sek. gemittelte Steigrate als Eingangssignal |
| `AeroGlideTopAppBar` | `core/ui/.../AeroGlideTopBar.kt` | `CenterAlignedTopAppBar` mit `actions`-Slot |
| `volume_off_24px.xml` / `volume_up_24px.xml` | `core/ui/src/main/res/drawable/` | Icons bereits vorhanden |
| Volume-`IconButton` | `app/.../AeroGlideActivity.kt` | Button existiert bereits, `onClick = {}` ist noch leer |
| `AeroGlideViewModel` | `app/.../AeroGlideViewModel.kt` | App-weiter ViewModel – Toggle hier verankern |
| `SettingsViewModel` | `feature/settings/.../SettingsViewModel.kt` | Prefs-Keys `spk_vario_tone_threshold_climb` / `spk_vario_tone_threshold_sink` bereits vorhanden |
| `SettingsScreen.kt` | `feature/settings/.../SettingsScreen.kt` | Threshold-Slider bereits vorhanden, müssen nach oben verschoben werden |
| `DomainModule` | `core/domain/.../di/DomainModule.kt` | UseCase-Registrierung hier ergänzen |
| `CommonModule` | `core/common/.../di/CommonModule.kt` | `AudioModule` hier oder als neue Datei daneben ergänzen |

---

## Ton-Algorithmus (LX Navia-Stil)

### Steigton (unterbrochener Beep-Ton)
Aktiv wenn `climbrate > climbThreshold` (Prefs-Default: `0.2 m/s`).

```
frequencyHz = (700f + climbrate * 150f).coerceIn(700f, 2200f)
beepMs      = (500L - (climbrate * 45f).toLong()).coerceIn(60L, 500L)
pauseMs     = beepMs   // 50 % Duty-Cycle – gleicher Rhythmus wie Beep
```

Beispiel: 1 m/s → 850 Hz, 455 ms Beep / 455 ms Pause · 5 m/s → 1450 Hz, 275 ms / 275 ms · 10 m/s → 2200 Hz, 60 ms / 60 ms.

### Sinkton (kontinuierlicher Dauerton)
Aktiv wenn `climbrate < sinkThreshold` (Prefs-Default: `-0.3 m/s`).

```
frequencyHz = (500f + climbrate * 30f).coerceIn(220f, 500f)   // climbrate negativ → Frequenz sinkt
```

Kein Unterbruch – kontinuierlich bis Zustandswechsel.

### Totstelle (Stille)
`sinkThreshold ≤ climbrate ≤ climbThreshold` → kein Ton abspielen.

---

## Schritt 1 – VarioTone Generator (`core/common`)

**Ziel:** Robuste, coroutine-basierte Audioausgabe mit sauberem Lifecycle.

### 1a. `VarioTone.kt` – Interface erweitern

Behalte die bestehenden Methoden (`setFrequency`, `setDuration`, `playBeep`, `stopBeep`) für Rückwärtskompatibilität. Ergänze:

```kotlin
fun startClimbTone(frequencyHz: Float, beepMs: Long, pauseMs: Long)
fun startSinkTone(frequencyHz: Float)
fun stop()
```

### 1b. `VarioToneImpl` – Neuimplementierung

Konstruktor erhält `@ApplicationScope scope: CoroutineScope` als Parameter (für Hilt-Injection).

- **Steigton** (`startClimbTone`): Starte eine Coroutine (`scope.launch`). Schleife: PCM-Sinuswellenpuffer mit `AudioTrack.Builder` (`MODE_STREAM`, `ENCODING_PCM_16BIT`, `CHANNEL_OUT_MONO`) abspielen → `delay(beepMs)` → Track stoppen → `delay(pauseMs)` → wiederholen.
- **Sinkton** (`startSinkTone`): Coroutine mit dauerhaft laufendem `AudioTrack`-Schreibloop bis Abbruch.
- **Stop** (`stop()`): `job?.cancelAndJoin()` → `audioTrack?.stop()` → `audioTrack?.release()` → `audioTrack = null`.
- **Threadsicherheit**: Nur ein aktiver `job: Job?`. Jedes `start*` ruft intern zuerst `stop()` auf.
- **`AudioAttributes`**: `USAGE_ASSISTANCE_SONIFICATION` + `CONTENT_TYPE_SONIFICATION` (kein Medieninhalt, kein Ducking durch Musik-Apps).

### 1c. Hilt-Binding – neues `AudioModule` in `core/common/di/`

```kotlin
@Module @InstallIn(SingletonComponent::class)
object AudioModule {
    @Provides @Singleton
    fun provideVarioTone(@ApplicationScope scope: CoroutineScope): VarioTone =
        VarioToneImpl(scope)
}
```

---

## Schritt 2 – VarioTone UseCase (`core/domain`)

**Ziel:** Steigrate in Tonzustand übersetzen und `VarioTone` steuern. Analog zu `AutoStartUseCase`.

### 2a. `VarioToneState` – Sealed Interface

Neue Datei `VarioToneState.kt` im Package `com.alpsfly.aeroglide.core.domain.usecase`:

```kotlin
sealed interface VarioToneState {
    data object Silence : VarioToneState
    data class Climb(val frequencyHz: Float, val beepMs: Long, val pauseMs: Long) : VarioToneState
    data class Sink(val frequencyHz: Float) : VarioToneState
}
```

### 2b. `VarioToneUseCase.kt` – `core/domain/usecase/`

```kotlin
@Singleton
class VarioToneUseCase @Inject constructor(
    private val sensorRepository: SensorRepository,
    private val varioTone: VarioTone,
    private val prefs: SharedPreferences,          // selbe @Singleton-Instanz wie SettingsModule
    @ApplicationScope private val scope: CoroutineScope
)
```

**State:**
```kotlin
private val _isToneEnabled = MutableStateFlow(false)
val isToneEnabled: StateFlow<Boolean> = _isToneEnabled.asStateFlow()
private var collectJob: Job? = null
```

**`enable()` / `disable()`:**
- `enable()`: `_isToneEnabled.value = true`, `collectJob = scope.launch { collectAndPlay() }`
- `disable()`: `collectJob?.cancel()`, `varioTone.stop()`, `_isToneEnabled.value = false`

**`collectAndPlay()` (private suspend):**
1. Lese Thresholds aus `prefs`:
   - `climbThreshold = prefs.getFloat("spk_vario_tone_threshold_climb", 0.2f)`
   - `sinkThreshold  = prefs.getFloat("spk_vario_tone_threshold_sink", -0.3f)`
2. Sammle `sensorRepository.climbrateFlowUi` mit `collect`.
3. Wende den LX-Navia-Algorithmus auf jeden `Climbrate`-Wert an → ergibt `VarioToneState`.
4. Vergleiche mit dem zuletzt gesendeten State (`var lastState: VarioToneState = Silence`). **Nur bei Zustandswechsel** neu starten (vermeidet Neustart-Klackern).
5. Dispatch: `Climb` → `varioTone.startClimbTone(...)`, `Sink` → `varioTone.startSinkTone(...)`, `Silence` → `varioTone.stop()`.

### 2c. `DomainModule.kt` – `VarioToneUseCase` registrieren

Ergänze analog zu `provideAutoStartUseCase` eine neue `@Provides @Singleton`-Methode für `VarioToneUseCase`.

### 2d. `AeroGlideViewModel.kt` – Delegation

```kotlin
// Inject VarioToneUseCase
val isToneEnabled: StateFlow<Boolean> = varioToneUseCase.isToneEnabled
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

fun onToggleTone() {
    if (isToneEnabled.value) varioToneUseCase.disable()
    else varioToneUseCase.enable()
}
```

---

## Schritt 3 – UI-Anbindung

### 3a. `AeroGlideActivity.kt` – Volume-Button verdrahten

Der `IconButton` mit `volume_off_24px` existiert bereits in der `actions`-Lambda der `AeroGlideTopAppBar`. Drei Änderungen:

1. State sammeln: `val isToneEnabled by aeroGlideViewModel.isToneEnabled.collectAsStateWithLifecycle()`
2. Click verdrahten: `onClick = { aeroGlideViewModel.onToggleTone() }`
3. Icon umschalten:

```kotlin
Icon(
    painter = painterResource(
        if (isToneEnabled) R.drawable.volume_up_24px else R.drawable.volume_off_24px
    ),
    contentDescription = if (isToneEnabled) "Vario-Ton aus" else "Vario-Ton ein"
)
```

### 3b. `SettingsScreen.kt` – Threshold-Slider nach oben

Die bestehenden `PreferenceSlider`-Items für `Vario Climb Tone Threshold` und `Vario Sink Tone Threshold` an den **Anfang der `LazyColumn`** verschieben. Den `HorizontalDivider` zwischen Vario- und Auto-Start-Block positionieren.

**Neue Reihenfolge:**
1. `PreferenceSlider` – Vario Climb Tone Threshold (`0.1f..2.0f`)
2. `PreferenceSlider` – Vario Sink Tone Threshold (`-5f..-0.1f`)
3. `HorizontalDivider`
4. `PreferenceSwitch` – Auto-Start
5. `PreferenceSlider` – Auto-Start Speed
6. `PreferenceSlider` – Auto-Start Climb Rate

---

## Architektur-Checkliste

- **SSOT**: `_isToneEnabled` lebt ausschließlich in `VarioToneUseCase`. `AeroGlideViewModel` gibt es nur weiter – kein zweiter lokaler State.
- **Battery (Prio 1)**: `collectJob` wird nur gestartet wenn aktiviert. Kein permanenter Sensor-Listener im Hintergrund. Bei `disable()` sofort stoppen und freigeben.
- **Kein Deprecated API**: `AudioTrack.Builder` statt deprecated Konstruktor. `collectAsStateWithLifecycle` statt `collectAsState`.
- **`feature/variometer`-Modul**: Das Modul existiert, enthält noch keinen Kotlin-Code. Tone-Logik gehört in `core/domain` (Clean Architecture – kein Feature-zu-Feature-Dependency). Das Modul ist für eine spätere eigenständige Variometer-UI reserviert.
- **SharedPreferences in `VarioToneUseCase`**: Direkt via dieselbe `@Singleton`-Instanz aus `SettingsModule` – kein extra Provider-Interface (KISS/YAGNI).
- **Keine extra Android-Permission nötig**: `USAGE_ASSISTANCE_SONIFICATION` erfordert keine zusätzliche `uses-permission` in `AndroidManifest.xml`.

