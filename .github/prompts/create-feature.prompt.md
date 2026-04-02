---
mode: 'agent'
description: 'Erstellt ein neues Feature-Modul (feature/*) nach AeroGlide Clean Architecture'
---

# Create Feature Module – AeroGlide

Erstelle ein neues, vollständiges Feature-Modul unter `feature/$FEATURE_NAME` nach dem AeroGlide-Architekturmuster.

## Eingabe

- **Feature-Name** (lowercase, kebab-case): `$FEATURE_NAME` (z.B. `windanalysis`, `thermalmap`)
- **Kurzbeschreibung**: Was macht dieses Feature?
- **Benötigte Daten-Quellen**: Welche Repositories/UseCases werden benötigt?
- **Screens**: Welche Composable-Screens soll das Modul enthalten?

---

## Zu erstellende Dateien

### 1. `feature/$FEATURE_NAME/build.gradle.kts`
Orientiere dich exakt an `feature/activityhistory/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialzation)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.alpsfly.aeroglide.feature.$FEATURE_NAME_LOWER"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":core:ui"))
    api(project(":core:data"))
    // Weitere core-Module nach Bedarf
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // ...
}
```

### 2. `.../$FEATURE_CLASS_NAME/ViewModel.kt`
Pflichtmuster – `@HiltViewModel` + `StateFlow`:

```kotlin
package com.alpsfly.aeroglide.feature.$FEATURE_NAME_LOWER

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class $FEATURE_CLASS_NAMEViewModel @Inject constructor(
    private val useCase: /* relevanter UseCase */
) : ViewModel() {

    val uiState: StateFlow<$FEATURE_CLASS_NAMEUiState> =
        useCase.someFlow
            .map { data -> $FEATURE_CLASS_NAMEUiState.Success(data) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = $FEATURE_CLASS_NAMEUiState.Loading
            )
}
```

### 3. `.../$FEATURE_CLASS_NAME/UiState.kt`
Sealed interface nach AeroGlide-Konvention:

```kotlin
sealed interface $FEATURE_CLASS_NAMEUiState {
    data object Loading : $FEATURE_CLASS_NAMEUiState
    data class Success(val data: /* DomainModel */) : $FEATURE_CLASS_NAMEUiState
    data class Error(@StringRes val messageRes: Int) : $FEATURE_CLASS_NAMEUiState
}
```

### 4. `.../$FEATURE_CLASS_NAME/Screen.kt`
Compose-Screen-Pflichtmuster:

```kotlin
@Composable
fun $FEATURE_CLASS_NAMEScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: $FEATURE_CLASS_NAMEViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is $FEATURE_CLASS_NAMEUiState.Loading -> { /* Loading-Indikator */ }
        is $FEATURE_CLASS_NAMEUiState.Success -> { /* Hauptinhalt */ }
        is $FEATURE_CLASS_NAMEUiState.Error -> { /* Fehlermeldung via Snackbar */ }
    }
}
```

### 5. `.../$FEATURE_CLASS_NAME/Navigation.kt`
Navigation-Extension nach dem `ActivityNavigation.kt`-Muster:

```kotlin
const val $FEATURE_UPPER_ROUTE = "$FEATURE_NAME_LOWER_route"

fun NavController.navigateTo$FEATURE_CLASS_NAMEScreen() {
    navigate($FEATURE_UPPER_ROUTE)
}

fun NavGraphBuilder.$FEATURE_NAME_LOWERScreen(navController: NavController) {
    composable(route = $FEATURE_UPPER_ROUTE) {
        $FEATURE_CLASS_NAMEScreen(navController = navController)
    }
}
```

### 6. `settings.gradle.kts` ergänzen
Füge das neue Modul hinzu:
```kotlin
include(":feature:$FEATURE_NAME")
```

---

## Architekturregeln (non-negotiable)

- ✅ `feature/*` darf **nicht** von anderen `feature/*`-Modulen abhängen
- ✅ Composables enthalten **keine** Business-Logik – nur State observieren und Events delegieren
- ✅ ViewModels rufen **keine** Repositories direkt auf wenn ein UseCase existiert
- ✅ `collectAsStateWithLifecycle` statt `collectAsState`
- ✅ `LaunchedEffect` für einmalige Side Effects (Navigation, Snackbar)
- ✅ `SharingStarted.WhileSubscribed(5000)` für alle StateFlows im ViewModel
- ✅ Kein `GlobalScope`, kein `runBlocking`
- ✅ Keine deprecated APIs (z.B. kein `rememberSwipeToDismissBoxState` aus alten APIs)

---

## Checkliste vor Abgabe

- [ ] `build.gradle.kts` angelegt und in `settings.gradle.kts` eingetragen
- [ ] ViewModel mit `@HiltViewModel` und `StateFlow`
- [ ] UiState als `sealed interface` mit Loading/Success/Error
- [ ] Screen mit `collectAsStateWithLifecycle`
- [ ] Navigation-Funktion vorhanden
- [ ] Keine Abhängigkeit auf andere `feature/*`-Module
- [ ] Kein direkter Repository-Zugriff ohne UseCase

