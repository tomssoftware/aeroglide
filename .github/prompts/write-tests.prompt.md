---
mode: 'agent'
description: 'Erstellt Unit-Tests für UseCases, Processors und ViewModels nach AeroGlide-Pattern'
---

# Write Tests – AeroGlide

Erstelle Unit-Tests für die angegebene Klasse nach dem AeroGlide-Testmuster.

## Eingabe

- **Zu testende Klasse**: $CLASS_NAME (z.B. `AutoStartDetector`, `VarioToneUseCase`, `ActivityViewModel`)
- **Modulpfad**: z.B. `core/domain`, `feature/activityhistory`
- **Testtyp**: Pure Unit-Test (JVM) oder ViewModel-Test (mit Hilt/Coroutines)?

---

## Pflichtmuster je Testtyp

### A) Pure UseCase / Processor – ohne Android-Abhängigkeit

Orientiere dich an `AutoStartDetectorUnitTest.kt`:

```kotlin
package de.tomssoftware.aeroglide.core.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class $CLASS_NAMETest {

    private lateinit var sut: $CLASS_NAME  // System Under Test

    // Fake-Zeitgeber statt System.currentTimeMillis()
    private var testTimeMillis: Long = 0L

    // Callback-Flags statt Mock-Frameworks wenn möglich
    private var callbackFired = false

    @Before
    fun setUp() {
        testTimeMillis = 0L
        callbackFired = false
        sut = $CLASS_NAME(timeProvider = { testTimeMillis })
        sut.onSomeEvent = { callbackFired = true }
    }

    @Test
    fun `initial state is correct`() = runTest {
        // GIVEN / WHEN: nur setUp
        // THEN:
        assertFalse(callbackFired)
    }

    @Test
    fun `event fires after condition is met for required duration`() = runTest {
        // GIVEN: Bedingung erfüllt
        sut.someValue = THRESHOLD + 1f

        // WHEN: Zeit simulieren
        repeat(10) {
            testTimeMillis += 500L
            sut.detect()
        }
        advanceTimeBy(REQUIRED_DURATION + 100.milliseconds)

        // THEN:
        assertTrue("Callback should fire", callbackFired)
    }
}
```

**Regeln für Pure Tests:**
- Kein Mockk wenn ein einfaches Callback-Flag genügt
- Fake-Zeitgeber als Lambda-Parameter injizieren (`timeProvider = { testTimeMillis }`)
- `runTest` für alle Coroutine-Tests
- `advanceTimeBy` / `advanceUntilIdle` für zeitbasierte Logik
- Given/When/Then-Kommentare in jedem Test

---

### B) UseCase mit Flows und Mockk

Orientiere dich an `VarioToneUseCaseUnitTest.kt`:

```kotlin
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class $CLASS_NAMETest {

    private lateinit var repository: SomeRepository
    private lateinit var dataFlow: MutableSharedFlow<SomeModel>
    private lateinit var sut: $CLASS_NAME

    @Before
    fun setUp() {
        clearAllMocks()
    }

    private fun createSut(
        testScope: TestScope = TestScope(UnconfinedTestDispatcher())
    ) {
        dataFlow = MutableSharedFlow()
        repository = mockk {
            every { someFlow } returns dataFlow
        }
        sut = $CLASS_NAME(
            repository = repository,
            applicationScope = testScope
        )
    }

    @Test
    fun `enable() activates processing`() = runTest {
        createSut(testScope = this)
        assertFalse(sut.isActive.value)

        sut.enable()
        advanceUntilIdle()

        assertTrue(sut.isActive.value)
        verify(exactly = 1) { repository.someFlow }
    }

    @Test
    fun `flow emission triggers correct state change`() = runTest {
        createSut(testScope = this)
        sut.enable()

        dataFlow.emit(SomeModel(value = 42f))
        advanceUntilIdle()

        assertEquals(42f, sut.someState.value, 0.001f)
    }
}
```

---

### C) ViewModel-Tests mit Turbine (StateFlow)

```kotlin
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class $CLASS_NAMEViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var useCase: SomeUseCase
    private lateinit var sut: $CLASS_NAMEViewModel

    @Before
    fun setUp() {
        useCase = mockk(relaxed = true)
        sut = $CLASS_NAMEViewModel(useCase)
    }

    @Test
    fun `uiState starts as Loading`() = runTest {
        sut.uiState.test {
            assertEquals($CLASS_NAMEUiState.Loading, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits Success when data arrives`() = runTest {
        // Arrange: Flow in useCase mit Testdaten befüllen
        // ...

        sut.uiState.test {
            skipItems(1) // Loading überspringen
            val state = awaitItem()
            assertTrue(state is $CLASS_NAMEUiState.Success)
        }
    }
}
```

---

## Allgemeine Regeln

### Namenskonventionen
```
`methodName() does X when Y`  – Backtick-Stil für Testmethoden
```

### Teststruktur
- Immer `@Before fun setUp()` mit `clearAllMocks()`
- Factory-Methode `createSut(...)` wenn Konfigurationsvarianten nötig
- Keine Magic Numbers – benannte Konstanten verwenden

### Verbotene Patterns
- ❌ Kein `runBlocking` in Tests – immer `runTest`
- ❌ Kein `Thread.sleep` – immer `advanceTimeBy` / `advanceUntilIdle`
- ❌ Keine GPS-Koordinaten in Testdaten loggen
- ❌ Kein `GlobalScope` in Produktionscode der getestet wird

### Testabdeckungs-Pflichtfälle
Für jeden UseCase/Processor mindestens:
1. **Happy Path**: Normaler Ablauf funktioniert
2. **Edge Case – zu früh**: Bedingung noch nicht erfüllt
3. **Edge Case – Abbruch**: Prozess wird unterbrochen/neugestartet
4. **Callback-Verifikation**: Alle Callbacks werden korrekt aufgerufen

---

## Checkliste vor Abgabe

- [ ] `runTest` für alle Coroutine-Tests
- [ ] Given/When/Then-Struktur erkennbar
- [ ] Keine `Thread.sleep` oder `runBlocking`
- [ ] Fake-Zeitgeber statt `System.currentTimeMillis()` wenn zeitabhängig
- [ ] `clearAllMocks()` in `@Before`
- [ ] Mindestens: Happy Path + Edge Case + Callback-Verifikation
- [ ] Kein GPS-Logging in Testdaten

