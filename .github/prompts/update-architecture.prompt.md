# Update Architecture Diagrams

Analyze the Kotlin source files that relate to the Mermaid diagrams in the `arch/` folder and update each diagram to accurately reflect the current codebase.

---

## General Principles

### Abstraction Level
- Diagrams are **high-level abstractions**, not 1:1 code mirrors. Show the **essential concepts** (states, transitions, dependencies, key interactions) and **omit implementation details** (private helpers, logging, trivial mappers, internal data classes, utility extensions).
- Every element in a diagram must earn its place: if removing it does not reduce a reader's understanding of the architecture, leave it out.
- Use **domain language** (e.g. "Recording", "TakeOff") rather than technical identifiers where possible.

### Dependency Clarity
- **Show the direction** of every dependency arrow explicitly (who calls / depends on whom).
- Group elements by **architectural layer** (UI → Domain-Coordinators → Domain-UseCases → Domain-Processors → Data/Service) so the layered architecture is immediately visible.
- Highlight **cross-module boundaries** (feature → core, core-internal) when relevant.
- Never show a dependency that does not exist in the source; never omit one that does.

### Consistency Rules
- Keep the Mermaid **diagram type** that already exists for each file (stateDiagram-v2, sequenceDiagram, graph TB, …).
- Preserve the existing **naming conventions** for participants / nodes (e.g. short aliases like `Coord`, `AppSM`).
- Use **notes** sparingly — only for information that cannot be expressed by the diagram structure itself (e.g. timer durations, guard conditions, important side-effects).
- Write notes and labels in the **same language** as the existing diagram (German or English — keep whatever is already used).

### What to Include
- All **states** and **transitions** with their trigger events and guard conditions (state machines).
- All **participants** involved in a flow and the **messages / calls** between them in order (sequence diagrams).
- All **significant classes / interfaces** and their dependency edges grouped by layer (dependency graphs).
- **Side-effects** that cross layer boundaries (e.g. a state change emitting a side-effect that triggers a use-case call).
- Key **thresholds or constants** that define behavior (e.g. speed ≥ 2.76 m/s, duration 5 s).

### What to Exclude
- Private implementation details, internal helper functions, extension utilities.
- Hilt wiring, module-level DI bindings, `@Provides` / `@Binds` details.
- Compose UI internals (recomposition, remember-blocks, modifier chains).
- Error handling / retry logic unless it is architecturally significant.
- Data classes, DTOs, mappers that only shuttle data between layers.
- Logging, analytics, or crash-reporting calls.

---

## Diagram-Specific Instructions

### AppStateMachine (`arch/domain/AppStateMachine.mermaid`)
- Source: `AppStateManager` (and related sealed classes / events).
- Diagram type: `stateDiagram-v2`.
- Show every **AppState** as a state node, every **Event** as a transition label.
- Include **side-effects** emitted on transitions (e.g. `RecordingStarted`, `RecordingStopped`).
- Annotate states with essential context (e.g. which data they carry).

### AutoStartStateMachine (`arch/domain/AutoStartStateMachine.mermaid`)
- Source: `AutoStartDetector` / `AutoStartProcessor` state logic.
- Diagram type: `stateDiagram-v2`.
- Show all detector states and automatic (sensor-driven) transitions **with guard conditions and durations**.
- Show manual overrides (`OnManualStart`, `OnManualStop`, `OnReset`) and where they apply.
- Include callback names (`onTakeOff()`, `onLanded()`) at the transitions that fire them.

### ManualStartFlow (`arch/domain/ManualStartFlow.mermaid`)
- Source: trace the call chain from UI button tap → ViewModel → Coordinator → UseCase → Processor → Service.
- Diagram type: `sequenceDiagram`.
- Show **start** and **stop** as two clearly separated `rect` blocks.
- Include UI feedback (icon change, screen-on flag) and the ForegroundService lifecycle.

### AutoStartFlow (`arch/domain/AutoStartFlow.mermaid`)
- Source: trace the flow from sensor data → Processor → Detector → UseCase → AppStateManager → Coordinator → RecordingUseCase.
- Diagram type: `sequenceDiagram`.
- Show the **take-off detection**, **flight phase transitions**, and **landing detection** as separate visual blocks.
- Show the **re-enable loop** after landing (Coordinator re-enables AutoStart if setting is still on).

### DomainLayer (`arch/domain/DomainLayer.mermaid`)
- Source: all classes in `core/domain` and their dependencies into `core/data`, `core/hardware`, services.
- Diagram type: `graph TB` with subgraphs per layer.
- Every class that is **injected into or called by** a coordinator or use-case must appear.
- Group into: UI Layer → Coordinators → Managers/UseCases → Workers/Processors → Data & Service Layer.

---

## Process

1. **Read** the relevant Kotlin source files for each diagram (state machines, use-cases, processors, coordinators, repositories).
2. **Compare** the current diagram against the source and identify additions, removals, or renames.
3. **Update** the Mermaid file — keep formatting clean and consistent with the existing style.
4. **Verify** that every node/participant in the diagram maps to a real class or concept in the source, and vice versa for all architecturally significant elements.
