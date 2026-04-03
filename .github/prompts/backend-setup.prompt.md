# Prompt: Generate a Firebase TypeScript Backend for the Aeroglide Android App

You are an expert in:

- Firebase Firestore data modeling
- Firebase Cloud Functions (TypeScript)
- Android ↔ Firebase synchronization patterns
- Offline-first mobile architectures
- Docker-based development environments
- WSL + VS Code workflows

Your task is to generate a **complete, production-ready Firebase backend** that precisely matches the existing Android app **Aeroglide** (Segelflug- und Paragliding-Aktivitätstracking).

---

## 0. Scope & Incremental Approach

> **Start small – extend step by step.**

The backend must be implemented in the following priority order:

### Phase 1 – MVP (implement first)

Only implement these two collections:

1. **`/users/{userId}`** – User profile document
2. **`/users/{userId}/activities/{activityId}`** – Activity sub-collection

Everything else (pilots, purchases, statistics, blacklist, FCM trigger function, purchase verification) is **out of scope for Phase 1**.

Phase 1 is considered complete when:
- A user document can be created/updated in Firestore.
- Activities can be uploaded, read back, and deleted via the `CloudStorage` interface.
- The `ActivityUploadWorker` can sync successfully against the emulator.
- Firestore security rules restrict access per `userId`.

### Phase 2 – Pilot profile

- Add `/pilots/{userId}` collection.
- Add `writePilot` / `readPilot` Cloud Function support.

### Phase 3 – Purchases & Subscriptions

- Add `/purchases/{purchaseToken}` collection.
- Implement `verifyPurchase` Cloud Function (Google Play API).
- Keep existing Retrofit endpoints (`registerInstanceId`, `unregisterInstanceId`, `verifyPurchase`).

### Phase 4 – FCM & Statistics

- Add `triggerSyncForAllUsers` Cloud Function (sends to FCM topic `"sync"`).
- Add `/statistics/{userId}/{date}/{docName}` collection with increment logic.
- Add `/configs/blacklist` with admin-only write access.

---

## 1. Project Context

### 1.1 Firebase Project

- Firebase project ID: **`thermalscout`**
- Region: **`us-central1`**
- Cloud Functions URL (prod): `BuildConfig.FIREBASE_FUNCTIONS_URL`
- Emulator host/port: configurable via `BuildConfig.FIREBASE_EMULATOR_HOST_ADDRESS` / `BuildConfig.FIREBASE_EMULATOR_PORT_FUNCTIONS`

### 1.2 Existing Android Architecture

The Android app already contains:

- **Room** local database
- **WorkManager**-based sync engine (`sync/work` module)
- **Hilt** dependency injection
- **`CloudStorage`** interface in `core/firebase` (Firestore CRUD)
- **`CloudFunctions`** interface in `core/firebase` (HTTP via Retrofit)
- **`SyncManager`** interface + `WorkManagerSyncManager` implementation
- **`ActivityUploadWorker`** (HiltWorker, CoroutineWorker)
- **FCM** push-triggered sync via `SyncNotificationsService`

---

## 2. Firestore Data Model

### 2.1 Collection Structure

```
/users/{userId}                          ← User profile
/users/{userId}/activities/{activityId}  ← Activity sub-collection (MAIN sync target)
/pilots/{userId}                         ← Pilot profile (glider data)
/purchases/{purchaseToken}               ← Google Play subscription purchase
/statistics/{userId}/{date}/{docName}    ← Usage counters (incr. per day)
/configs/blacklist                       ← Blacklisted app versions / users
```

### 2.2 Activity Document (`/users/{userId}/activities/{activityId}`)

This is the **primary sync entity**. All field names use **snake_case** (matching `@PropertyName` annotations in the Android DTO).

| Firestore Field            | Type    | Notes                                    |
|----------------------------|---------|------------------------------------------|
| `user_id`                  | string  | Firebase Auth UID                        |
| `begin`                    | number  | Unix timestamp ms (flight start)         |
| `end`                      | number  | Unix timestamp ms (flight end)           |
| `distance`                 | number  | float, meters                            |
| `duration`                 | number  | long, milliseconds                       |
| `ascent`                   | number  | float, meters                            |
| `descent`                  | number  | float, meters                            |
| `min_pressure`             | number  | float, hPa                               |
| `max_pressure`             | number  | float, hPa                               |
| `min_altitude`             | number  | float, meters                            |
| `max_altitude`             | number  | float, meters                            |
| `min_speed`                | number  | float, m/s                               |
| `max_speed`                | number  | float, m/s                               |
| `avg_speed`                | number  | float, m/s                               |
| `max_climbrate`            | number  | float, m/s                               |
| `min_climbrate`            | number  | float, m/s                               |
| `positive_avg_climbrate`   | number  | float, m/s                               |
| `negative_avg_climbrate`   | number  | float, m/s                               |
| `max_grade`                | number  | float, ratio                             |
| `min_grade`                | number  | float, ratio                             |
| `max_heart_rate`           | number  | long, bpm                                |
| `min_heart_rate`           | number  | long, bpm                                |
| `updated_at`               | number  | Unix timestamp ms (last client write)    |
| `server_updated_at`        | timestamp | Firestore server timestamp (add this)  |

### 2.3 User Document (`/users/{userId}`)

| Firestore Field      | Type    | Notes                             |
|----------------------|---------|-----------------------------------|
| `uid`                | string  | Firebase Auth UID (duplicated)    |
| `email`              | string  |                                   |
| `display_name`       | string  | nullable                          |
| `photo_url`          | string  | nullable                          |
| `created_at`         | number  | Unix timestamp ms                 |
| `last_login_at`      | number  | Unix timestamp ms                 |
| `is_email_verified`  | boolean |                                   |
| `fcm_token`          | string  | nullable – used for FCM push sync |
| `version_code`       | number  | last known app version code       |
| `terms_accepted`     | boolean |                                   |

### 2.4 Pilot Document (`/pilots/{userId}`)

| Firestore Field | Type   |
|-----------------|--------|
| `email`         | string |
| `displayName`   | string |
| `gliderType`    | string |
| `gliderId`      | string |
| `licence`       | string |
| `club`          | string |

### 2.5 Purchase Document (`/purchases/{purchaseToken}`)

| Firestore Field                               | Type   |
|-----------------------------------------------|--------|
| `userId`                                      | string |
| `fcmToken`                                    | string |
| `purchaseToken`                               | string |
| `subscriptionDetails.acknowledgementState`    | string |
| `subscriptionDetails.subscriptionState`       | string |
| `subscriptionDetails.latestOrderId`           | string |

### 2.6 Blacklist Document (`/configs/blacklist`)

| Firestore Field | Type             |
|-----------------|------------------|
| `versions`      | array of numbers |
| `users`         | array of strings |

---

## 3. Existing Android Sync Architecture (must match exactly)

### 3.1 SyncState Machine

The Room entity `Activity` has a `sync_state` column with these states:

```kotlin
enum class SyncState {
    LOCAL,           // created locally, never uploaded
    PENDING_UPLOAD,  // worker has picked it up (idempotency guard)
    SYNCED,          // successfully synced; firestore_id is set
    ERROR,           // permanent failure; sync_error column holds the message
}
```

### 3.2 Upload Strategy ("upload-first")

The `ActivityUploadWorker` implements this exact flow:

1. Fetch all activities with `sync_state IN (LOCAL, PENDING_UPLOAD)` from Room.
2. For each activity:
   - Mark as `PENDING_UPLOAD` before the network call.
   - Call `CloudStorage.writeActivity(userId, activity.toFirestore())`.
   - **If `activity.id == null`** → Firestore `add()` → auto-generates document ID → store returned ID as `firestore_id`, mark `SYNCED`.
   - **If `activity.id != null`** → Firestore `set()` (full overwrite) → mark `SYNCED`.
3. Retryable errors: `IOException` or `FirebaseFirestoreException(UNAVAILABLE)` → return `Result.retry()`.
4. Permanent errors → mark `ERROR`, store error message.

> **Known race condition**: If the worker is killed after `add()` returns but before the local DB update, the next retry will create a duplicate document. The backend should handle idempotency via client-side pre-generated document IDs (improvement task).

### 3.3 WorkManager Trigger

```kotlin
// Expedited, one-time, requires network
OneTimeWorkRequestBuilder<DelegatingWorker>()
    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
    .setConstraints(SyncConstraints)   // NetworkType.CONNECTED
    .build()

// ExistingWorkPolicy.KEEP – no parallel runs
workManager.enqueueUniqueWork(SYNC_WORK_NAME, ExistingWorkPolicy.KEEP, work)
```

### 3.4 FCM Push-Triggered Sync

- The Android app subscribes to the FCM topic **`"sync"`** via `FirebaseSyncSubscriber`.
- `SyncNotificationsService` (extends `FirebaseMessagingService`) listens for messages from `/topics/sync` and calls `syncManager.requestSync()`.
- **The backend must send an FCM message to topic `"sync"` whenever data relevant to clients changes** (e.g., after a server-side data migration or admin action).

### 3.5 CloudStorage Interface (Android-side Firestore API)

```kotlin
// /users/{userId}/activities/{activityId}
suspend fun writeActivity(userId: String, activity: ActivityDto): String
suspend fun readActivitiesForUser(userId: String): List<ActivityDto>
suspend fun deleteActivity(userId: String, firestoreId: String)
```

### 3.6 Existing Cloud Functions (HTTP via Retrofit PUT)

These already exist and must be kept:

| Endpoint             | Method | Description                        |
|----------------------|--------|------------------------------------|
| `registerInstanceId` | PUT    | Registers FCM instance ID          |
| `unregisterInstanceId` | PUT  | Unregisters FCM instance ID        |
| `verifyPurchase`     | PUT    | Verifies Google Play purchase token |

---

## 4. Backend Requirements

### 4.1 Firestore Triggered Functions

Implement the following **Firestore-triggered Cloud Functions**:

- `onActivityCreated` – triggered on `users/{userId}/activities/{activityId}` create
  - Adds `server_updated_at` server timestamp
  - Validates required fields
- `onActivityUpdated` – triggered on update
  - Updates `server_updated_at`
- `onActivityDeleted` – triggered on delete (for audit/cleanup)

### 4.2 HTTP Callable / REST Functions

Keep existing HTTP functions and add:

- `createOrUpdateActivity(userId, activityData)` – callable function
- `deleteActivity(userId, activityId)` – callable function
- `getActivitiesForUser(userId)` – callable function
- Conflict resolution: **last-write-wins** based on `updated_at` field
- Validation: all required numeric fields must be present and finite

### 4.3 FCM Sync Trigger Function

- `triggerSyncForAllUsers` – HTTP/Admin function
  - Sends FCM message to topic `"sync"`
  - Used to push-trigger client sync after server-side changes

### 4.4 Purchase Verification Function

Extend existing `verifyPurchase` to:
- Call Google Play Developer API
- Write result back to `/purchases/{purchaseToken}`
- Update subscription state fields

### 4.5 Firebase Emulator Setup

Generate:

- `firebase.json` (functions, firestore, emulators config)
- `firestore.rules`
- `firestore.indexes.json` (index on `user_id` + `begin` for activity queries)
- `.firebaserc` with project alias `thermalscout`
- `docker-compose.yml` for WSL
- VS Code debug configuration (`launch.json`)

### 4.6 Docker + WSL Integration

- `Dockerfile` with Node 18 + Firebase Tools
- Volume mounts for live reload of functions source
- Commands:
  - `firebase emulators:start --only firestore,functions`
  - `npm run build` (TypeScript compile)
  - `npm run serve` (emulator + functions)

### 4.7 Environment Separation

- `.env.development` → emulator
- `.env.production` → production Firebase
- `BuildConfig`-compatible: `FIREBASE_EMULATOR_HOST_ADDRESS`, `FIREBASE_EMULATOR_PORT_FUNCTIONS`

---

## 5. TypeScript Types (must match Android DTOs exactly)

```typescript
// Matches de.tomssoftware.aeroglide.core.model.firebase.Activity
interface ActivityDocument {
  user_id: string;
  begin: number;
  end: number;
  distance: number;
  duration: number;
  ascent: number;
  descent: number;
  min_pressure: number;
  max_pressure: number;
  min_altitude: number;
  max_altitude: number;
  min_speed: number;
  max_speed: number;
  avg_speed: number;
  max_climbrate: number;
  min_climbrate: number;
  positive_avg_climbrate: number;
  negative_avg_climbrate: number;
  max_grade: number;
  min_grade: number;
  max_heart_rate: number;
  min_heart_rate: number;
  updated_at: number;
  server_updated_at?: FirebaseFirestore.Timestamp; // server-side only
}

// Matches de.tomssoftware.aeroglide.core.model.firebase.User
interface UserDocument {
  uid: string;
  email: string;
  display_name: string | null;
  photo_url: string | null;
  created_at: number;
  last_login_at: number;
  is_email_verified: boolean;
  fcm_token: string | null;
  version_code: number;
  terms_accepted: boolean;
}

// Matches de.tomssoftware.aeroglide.core.model.common.Pilot
interface PilotDocument {
  email: string;
  displayName: string;
  gliderType: string;
  gliderId: string;
  licence: string;
  club: string;
}
```

---

## 6. Security Rules

Generate Firestore rules that:

- Restrict all access to **authenticated users** only
- `/users/{userId}/**`: read/write only if `request.auth.uid == userId`
- `/pilots/{userId}`: read/write only if `request.auth.uid == userId`
- `/purchases/{purchaseToken}`: write only from Cloud Functions (no direct client write)
- `/configs/blacklist`: read for all authenticated users, write only admin
- `/statistics/{userId}/**`: write only from Cloud Functions
- Validate `Activity` fields: `user_id` must equal `request.auth.uid`, numeric fields must be numbers

---

## 7. Output Format

### 7.1 Project Structure

```
/firebase
  /functions
    /src
      index.ts            ← function exports
      activities.ts       ← activity CRUD + Firestore triggers
      users.ts            ← user management
      purchases.ts        ← purchase verification (existing)
      fcm.ts              ← FCM sync trigger
      types.ts            ← TypeScript interfaces (matching Android DTOs)
      validation.ts       ← field validation helpers
    package.json
    tsconfig.json
    .eslintrc.js
  firebase.json
  firestore.rules
  firestore.indexes.json
  .firebaserc
  docker-compose.yml
  Dockerfile
  .env.development
  .env.production
  .gitignore
```

### 7.2 All Source Code

Provide complete TypeScript implementations for:

- Firestore CRUD matching `CloudStorageFirebase.kt` behaviour
- Firestore-triggered functions with `server_updated_at`
- HTTP callable functions
- FCM topic message sender
- Purchase verification via Google Play API
- Input validation matching Android DTO constraints
- Error handling with structured logging

### 7.3 Android Integration Guide

Explain precisely:

- How `ActivityUploadWorker` pushes data (already implemented – describe the matching server-side contract)
- How to implement a **download/pull** function in Android to call `readActivitiesForUser`
- How to handle the **duplicate-document race condition** using client-side document ID pre-generation
- FCM topic `"sync"` subscription flow
- Emulator connection: `FirebaseFirestore.getInstance().useEmulator(host, port)`

### 7.4 Setup Guide

Step-by-step:

1. Firebase project initialization (`thermalscout`)
2. Docker / WSL setup
3. Emulator start
4. Android emulator connection (`adb reverse tcp:8080 tcp:8080`)
5. Production deployment

---

## 8. Additional Requirements

- **No GPS coordinates or location data** must ever be logged (matches Android security policy).
- All functions must be **idempotent** (safe to retry).
- Backend must support both **Firebase Emulator** (local dev) and **production** deployment.
- Use **Node 18 + Firebase Functions v2** (2nd gen).
- Follow Firebase best practices (batched writes, transaction for counters, etc.).
- The output must be ready to copy into a real project at path `/firebase` in the Aeroglide repository root.
