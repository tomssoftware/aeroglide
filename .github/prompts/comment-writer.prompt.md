---
mode: 'edit'
description: 'Add idiomatic Kotlin/Android comments in English to the current file'
---

# Add Kotlin Comments – AeroGlide

Review `${file}` and add or improve all comments following the rules below.
Only add a comment where it genuinely adds value.
Remove or replace any comment that is misleading, redundant, or simply restates what the code already says.

---

## 1. KDoc for Every Public or Internal Element  `/** */`

Every `public` or `internal` class, interface, object, function, and property **must** have a KDoc block.

**Structure:**
- **First line** – one concise sentence describing **what** the element does (never *how*).
- Leave a blank line before any tags.
- `@param` – for every non-obvious parameter.
- `@return` – when the return value is not self-explanatory.
- `@throws` – when the function can throw a checked or documented exception.
- `@see` – to link related classes or functions.
- Use `[ClassName]` / `[functionName]` for clickable references inside the text.

```kotlin
/**
 * Detects take-off and landing events based on velocity and climb-rate sensor data.
 *
 * Operates as a state machine:
 * [State.WaitForTakeOff] → [State.TakeOff] → [State.Flying] → [State.Landing] → [State.Landed].
 * Each transition requires sensor conditions to be satisfied for a minimum [Duration].
 *
 * @param timeProvider Returns the current time in milliseconds.
 *                     Override with a fake clock in unit tests for deterministic behaviour.
 * @see AutoStartProcessor
 */
class AutoStartDetector(
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
)
```

---

## 2. Single-Line Doc for Simple Members  `/** */`

For properties or functions whose purpose is obvious from the name alone, a single-line KDoc is enough.

```kotlin
/** Vertical speed in m/s. Positive = climbing, negative = sinking. */
val climbRate: Float

/** Resets the detector to [State.WaitForTakeOff] and clears all time accumulators. */
fun reset()
```

---

## 3. Inline Comments – Explain **Why**, Never What  `//`

Use `//` only to document a **non-obvious decision, constraint, or trade-off** in the code.
If the comment merely repeats what the code does, delete it.

**✅ Good – explains a design decision:**
```kotlin
// >= is intentional: a velocity exactly at the threshold still qualifies (inclusive boundary).
private fun isTakeOffCondition() = velocity >= velocityFlying && climbrate >= climbrateTakeOff
```

**❌ Bad – restates the obvious:**
```kotlin
// Check if velocity is greater than or equal to velocityFlying
private fun isTakeOffCondition() = velocity >= velocityFlying
```

---

## 4. Section Separators

Group logically related members with a labelled separator.
Use exactly this format — one blank line above and below:

```kotlin
// --- Public API ---

// --- State Machine ---

// --- Callbacks ---

// --- Internal Helpers ---
```

---

## 5. TODO and FIXME

Always include a short reason. Add a tracking reference (issue number, PR) where possible.
**Never** leave a bare `// TODO` or `// FIXME` without an explanation.

```kotlin
// TODO: Replace hardcoded 0L with a proper unique ID generator (see issue #142).
// FIXME: Crashes when the sensor Flow emits before the coroutine scope is ready.
```

---

## 6. Sealed UiState – Document Every Variant

Each state class and its properties must be documented.

```kotlin
/**
 * Represents all possible UI states for this screen.
 *
 * Sealed to guarantee exhaustive `when` handling in Compose.
 */
sealed interface TrackingUiState {

    /** Shown while sensor data is being collected for the first time. */
    data object Loading : TrackingUiState

    /**
     * Live data is available and the map can be rendered.
     *
     * @property location Current GPS position of the pilot.
     * @property climbRate Vertical speed in m/s. Positive = climbing.
     * @property altitude Barometric altitude in metres above sea level.
     */
    data class Ready(
        val location: Location,
        val climbRate: Float,
        val altitude: Float,
    ) : TrackingUiState

    /**
     * An unrecoverable error occurred.
     *
     * @property messageRes String resource ID of the user-facing error message.
     */
    data class Error(@StringRes val messageRes: Int) : TrackingUiState
}
```

---

## 7. Coroutines and Flow – Document Scope and Cancellation Contract

Always state which scope is used, why it was chosen, and what must be done to cancel it.

```kotlin
/**
 * Combines location and climb-rate emissions and feeds each tick into [AutoStartDetector.detect].
 *
 * Launched in [applicationScope] so detection survives Activity recreation.
 * **Must** be cancelled by calling [stop] when auto-start is disabled to release the
 * sensor wake-lock.
 */
private var collectorJob: Job? = null
```

---

## 8. Hilt Annotations – Explain Non-Obvious Scope Decisions

Place the rationale directly above the scope annotation.

```kotlin
// Singleton: the init block registers callbacks on the shared AutoStartProcessor.
// Multiple instances would silently overwrite each other's callbacks.
@Singleton
class AutoStartUseCase @Inject constructor(...)
```

---

## 9. State Machine – Every State, Event, and SideEffect Gets a KDoc

```kotlin
sealed class State {
    /** Waiting for the pilot to begin a take-off run. This is the initial state. */
    data object WaitForTakeOff : State()

    /** Take-off conditions are met; timer running before [SideEffect.TakeOff] fires. */
    data object TakeOff : State()

    /** Airborne and flying; monitoring for the start of a landing approach. */
    data object Flying : State()
}

sealed class Event {
    /**
     * Emitted on every sensor tick.
     *
     * @property elapsedTime Wall-clock time since the previous [OnUpdate] call.
     */
    data class OnUpdate(val elapsedTime: Duration) : Event()

    /** Resets the detector to [State.WaitForTakeOff] and clears all time accumulators. */
    data object OnReset : Event()
}

sealed class SideEffect {
    /** Fired exactly once when the state machine transitions to [State.TakeOff]. */
    data object TakeOff : SideEffect()

    /** Fired exactly once when the state machine transitions to [State.Landed]. */
    data object Landed : SideEffect()
}
```

---

## What NOT to Comment

| Situation | Reason |
|---|---|
| Self-explanatory names (`val isRecording: Boolean`) | The name is the documentation. |
| Auto-generated boilerplate (`copy`, `equals`, `hashCode`) | Adds no information. |
| Import statements | Never comment imports. |
| `override` with identical contract to parent | Omit the KDoc block entirely; the parent's doc is inherited. |
| Every single line in a function body | Trust readable code; only comment the *why*. |

