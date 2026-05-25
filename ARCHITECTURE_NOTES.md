# MinimalFit Database Architecture

This document contains a complete overview and visual schemas of the **MinimalFit** database architecture. It includes both the normalized local SQLite schema (managed by Room) and the denormalized, synchronized Cloud Firestore schema.

---

## 1. Local Database Architecture (Room SQLite)

The local SQLite database (`AppDatabase`) is a fully relational, normalized schema consisting of **16 registered entities** (excluding preferences). Relationships such as Many-to-Many are represented using explicit Junction/Cross-Reference tables.

### 1.1 Complete ER Diagram

```mermaid
erDiagram
    %% ==========================================
    %% FOOD & DIET DOMAIN
    %% ==========================================
    ingredients {
        string id PK
        string name
        int baseCalories
        MeasurementUnit measurementUnit
        string imageUrl
        boolean isGlobal
        string creatorId
        Instant updatedAt
    }

    meals {
        string id PK
        string name
        string description
        string imageUrl
        MeasurementUnit measurementUnit
        Instant updatedAt
    }

    diets {
        string id PK
        string name
        string description
        string imageUrl
        Instant updatedAt
    }

    meal_logs {
        string id PK
        Instant createdAt
        Instant updatedAt
    }

    meal_ingredient_cross_ref {
        string mealId PK, FK
        string ingredientId PK, FK
        float amount
    }

    diet_meal_cross_ref {
        string dietId PK, FK
        string mealId PK, FK
        float amount
    }

    meal_log_meal_cross_ref {
        string mealLogId PK, FK
        string mealId PK, FK
        float amount
    }

    %% ==========================================
    %% GYM & WORKOUT DOMAIN
    %% ==========================================
    exercises {
        string id PK
        string name
        boolean isBodyweight
        string muscleGroup
        int restSeconds
        boolean isGlobal
        string creatorId
        Instant updatedAt
    }

    routines {
        string id PK
        string name
        string description
        string daysOfWeekStr
        Instant updatedAt
    }

    routine_exercises {
        string id PK
        string routineId FK
        string exerciseId FK
        Instant createdAt
    }

    routine_sets {
        string id PK
        string routineExerciseId FK
        float weight
        int reps
    }

    sessions {
        string id PK
        Instant startTime
        string title
        long durationSeconds
        string notes
        boolean isFinished
        Instant updatedAt
    }

    session_exercises {
        string id PK
        string sessionId FK
        string exerciseId FK
        Instant createdAt
    }

    sets {
        string id PK
        string sessionExerciseId FK
        string sessionId FK
        float weight
        int reps
        string notes
        boolean isCompleted
        Instant createdAt
    }

    %% ==========================================
    %% TRACKING & SYNC INFRASTRUCTURE
    %% ==========================================
    tracks {
        string id PK
        Instant startTime
        Duration duration
        string name
        double distance
        string pace
        List_TrackPoint routePoints
        Instant updatedAt
    }

    sync_queue {
        long id PK
        string entityType
        string entityId
        string action
        Instant createdAt
    }

    %% ==========================================
    %% RELATIONSHIPS
    %% ==========================================
    meals ||--|{ meal_ingredient_cross_ref : "contains (1:N)"
    ingredients ||--|{ meal_ingredient_cross_ref : "referenced_in (1:N)"

    diets ||--|{ diet_meal_cross_ref : "contains (1:N)"
    meals ||--|{ diet_meal_cross_ref : "referenced_in (1:N)"

    meal_logs ||--|{ meal_log_meal_cross_ref : "contains (1:N)"
    meals ||--|{ meal_log_meal_cross_ref : "referenced_in (1:N)"

    routines ||--|{ routine_exercises : "has (1:N)"
    exercises ||--|{ routine_exercises : "used_in (1:N)"
    routine_exercises ||--|{ routine_sets : "configures (1:N)"

    sessions ||--|{ session_exercises : "has (1:N)"
    exercises ||--|{ session_exercises : "used_in (1:N)"
    session_exercises ||--|{ sets : "performs (1:N)"
    sessions ||--|{ sets : "indexes (1:N)"
```

---

### 1.2 Isolated Domain Architecture Diagrams

#### A. Gym & Workout Domain (Relational Structure)
Focuses on how workout definitions (Routines) and actual workout executions (Sessions) link with exercises and set trackers.

```mermaid
erDiagram
    exercises {
        string id PK
        string name
        boolean isBodyweight
        string muscleGroup
        int restSeconds
        boolean isGlobal
        string creatorId
        Instant updatedAt
    }

    routines {
        string id PK
        string name
        string description
        string daysOfWeekStr
        Instant updatedAt
    }

    routine_exercises {
        string id PK
        string routineId FK
        string exerciseId FK
        Instant createdAt
    }

    routine_sets {
        string id PK
        string routineExerciseId FK
        float weight
        int reps
    }

    sessions {
        string id PK
        Instant startTime
        string title
        long durationSeconds
        string notes
        boolean isFinished
        Instant updatedAt
    }

    session_exercises {
        string id PK
        string sessionId FK
        string exerciseId FK
        Instant createdAt
    }

    sets {
        string id PK
        string sessionExerciseId FK
        string sessionId FK
        float weight
        int reps
        string notes
        boolean isCompleted
        Instant createdAt
    }

    routines ||--|{ routine_exercises : "has"
    exercises ||--|{ routine_exercises : "used_in"
    routine_exercises ||--|{ routine_sets : "configures"

    sessions ||--|{ session_exercises : "has"
    exercises ||--|{ session_exercises : "used_in"
    session_exercises ||--|{ sets : "performs"
    sessions ||--|{ sets : "indexes"
```

#### B. Food & Nutrition Domain (Relational Structure)
Shows the normalization of ingredients, compound meals, and diet plans as well as logged nutrition logs.

```mermaid
erDiagram
    ingredients {
        string id PK
        string name
        int baseCalories
        MeasurementUnit measurementUnit
        string imageUrl
        boolean isGlobal
        string creatorId
        Instant updatedAt
    }

    meals {
        string id PK
        string name
        string description
        string imageUrl
        MeasurementUnit measurementUnit
        Instant updatedAt
    }

    diets {
        string id PK
        string name
        string description
        string imageUrl
        Instant updatedAt
    }

    meal_logs {
        string id PK
        Instant createdAt
        Instant updatedAt
    }

    meal_ingredient_cross_ref {
        string mealId PK, FK
        string ingredientId PK, FK
        float amount
    }

    diet_meal_cross_ref {
        string dietId PK, FK
        string mealId PK, FK
        float amount
    }

    meal_log_meal_cross_ref {
        string mealLogId PK, FK
        string mealId PK, FK
        float amount
    }

    meals ||--|{ meal_ingredient_cross_ref : "contains"
    ingredients ||--|{ meal_ingredient_cross_ref : "used_in"
    
    diets ||--|{ diet_meal_cross_ref : "contains"
    meals ||--|{ diet_meal_cross_ref : "used_in"

    meal_logs ||--|{ meal_log_meal_cross_ref : "contains"
    meals ||--|{ meal_log_meal_cross_ref : "used_in"
```

#### C. Tracking & Sync Infrastructure
Displays the system tables used to store geographical tracks and record changes locally before synchronization with Firestore.

```mermaid
erDiagram
    tracks {
        string id PK
        Instant startTime
        Duration duration
        string name
        double distance
        string pace
        List_TrackPoint routePoints
        Instant updatedAt
    }

    sync_queue {
        long id PK
        string entityType
        string entityId
        string action
        Instant createdAt
    }
```

---

## 2. Cloud Firestore Database Architecture

Firestore uses a **document-oriented, hierarchical schema**. To minimize roundtrips and read operations, relationships are **denormalized and embedded directly** within documents as nested arrays or maps.

### 2.1 Firestore Database Tree Folder Structure

Below is the directory tree layout showing exactly how documents and collections are hierarchically organized inside Cloud Firestore:

```text
/ (Firestore Database Root)
├── global (Collection)
│   ├── exercises (Document)
│   │   └── items (Subcollection)
│   │       └── {exerciseId} (Document) -> ExerciseDto
│   └── ingredients (Document)
│       └── items (Subcollection)
│           └── {ingredientId} (Document) -> IngredientDto
└── users (Collection)
    └── {uid} (Document) -> FCM Token, Notification settings map
        ├── exercises (Subcollection)
        │   └── {exerciseId} (Document) -> ExerciseDto (user-defined)
        ├── ingredients (Subcollection)
        │   └── {ingredientId} (Document) -> IngredientDto (user-defined)
        ├── meals (Subcollection)
        │   └── {mealId} (Document) -> MealDocument (denormalized ingredients)
        ├── diets (Subcollection)
        │   └── {dietId} (Document) -> DietDocument (denormalized meals)
        ├── meal_logs (Subcollection)
        │   └── {mealLogId} (Document) -> MealLogDocument (denormalized logged meals)
        ├── routines (Subcollection)
        │   └── {routineId} (Document) -> RoutineDocument (embedded workout configuration tree)
        ├── sessions (Subcollection)
        │   └── {sessionId} (Document) -> SessionDocument (embedded workout execution tree)
        └── tracks (Subcollection)
            └── {trackId} (Document) -> TrackDocument (embedded gps points list)
```

---

### 2.2 Global Shared Firestore Collections

The global collection contains central read-only catalog lookup datasets accessed by all users.

```mermaid
graph TD
    classDef globalStyle fill:#1e293b,stroke:#38bdf8,stroke-width:2px,color:#f8fafc;
    classDef docStyle fill:#1f2937,stroke:#10b981,stroke-width:1px,color:#f3f4f6;
    classDef schemaStyle fill:#111827,stroke:#f59e0b,stroke-width:1px,color:#d1d5db;

    GLOBAL[Collection: global]:::globalStyle
    
    GLOBAL --> G_EX[Document: exercises]:::docStyle
    G_EX --> G_EX_ITEMS[Subcollection: items]:::globalStyle
    G_EX_ITEMS --> G_EX_DOC["Document: [exerciseId]<br/>-- id: String<br/>-- name: String<br/>-- isBodyweight: Boolean<br/>-- muscleGroup: String<br/>-- restSeconds: Int<br/>-- isGlobal: Boolean (true)<br/>-- creatorId: String (null)<br/>-- updatedAt: String (ISO)"]:::schemaStyle
    
    GLOBAL --> G_ING[Document: ingredients]:::docStyle
    G_ING --> G_ING_ITEMS[Subcollection: items]:::globalStyle
    G_ING_ITEMS --> G_ING_DOC["Document: [ingredientId]<br/>-- id: String<br/>-- name: String<br/>-- baseCalories: Int<br/>-- measurementUnit: String<br/>-- imageUrl: String<br/>-- isGlobal: Boolean (true)<br/>-- creatorId: String (null)<br/>-- updatedAt: String (ISO)"]:::schemaStyle
```

---

### 2.3 Per-User Private Firestore Collections

Private user workspaces are fully partitioned under `users/{uid}` and sync Room changes securely.

```mermaid
graph TD
    classDef userStyle fill:#1e1b4b,stroke:#818cf8,stroke-width:2px,color:#f8fafc;
    classDef docStyle fill:#1f2937,stroke:#10b981,stroke-width:1px,color:#f3f4f6;
    classDef schemaStyle fill:#111827,stroke:#f59e0b,stroke-width:1px,color:#d1d5db;

    USERS[Collection: users]:::userStyle
    USERS --> USER_DOC["Document: [uid]<br/>-- fcmToken: String<br/>-- notifications: Map { dailyRun: Boolean, weightMilestone: Boolean }"]:::docStyle

    %% Custom lookup catalog
    USER_DOC --> U_EX[Subcollection: exercises]:::userStyle
    U_EX --> U_EX_DOC["Document: [exerciseId]<br/><i>ExerciseDto (user-custom)</i>"]:::schemaStyle

    USER_DOC --> U_ING[Subcollection: ingredients]:::userStyle
    U_ING --> U_ING_DOC["Document: [ingredientId]<br/><i>IngredientDto (user-custom)</i>"]:::schemaStyle

    %% Food Domain (Flattened / Denormalized)
    USER_DOC --> U_MEALS[Subcollection: meals]:::userStyle
    U_MEALS --> U_MEAL_DOC["Document: [mealId]<br/><b>MealDocument</b><br/>-- id: String<br/>-- name: String<br/>-- description: String<br/>-- imageUrl: String<br/>-- measurementUnit: String<br/>-- ingredients: List { ingredient: IngredientDto, amount: Float }<br/>-- updatedAt: String (ISO)"]:::schemaStyle

    USER_DOC --> U_DIETS[Subcollection: diets]:::userStyle
    U_DIETS --> U_DIET_DOC["Document: [dietId]<br/><b>DietDocument</b><br/>-- id: String<br/>-- name: String<br/>-- description: String<br/>-- imageUrl: String<br/>-- meals: List { meal: MealDocument, amount: Float }<br/>-- updatedAt: String (ISO)"]:::schemaStyle

    USER_DOC --> U_LOGS[Subcollection: meal_logs]:::userStyle
    U_LOGS --> U_LOG_DOC["Document: [mealLogId]<br/><b>MealLogDocument</b><br/>-- id: String<br/>-- createdAt: String<br/>-- meals: List { meal: MealDocument, amount: Float }<br/>-- updatedAt: String (ISO)"]:::schemaStyle

    %% Gym Domain (Nested trees)
    USER_DOC --> U_ROUTINES[Subcollection: routines]:::userStyle
    U_ROUTINES --> U_ROUTINE_DOC["Document: [routineId]<br/><b>RoutineDocument</b><br/>-- id: String<br/>-- name: String<br/>-- description: String<br/>-- daysOfWeekStr: String<br/>-- exercises: List { routineExerciseId, exercise: ExerciseDto, sets: List{id, weight, reps}, createdAt }<br/>-- updatedAt: String (ISO)"]:::schemaStyle

    USER_DOC --> U_SESSIONS[Subcollection: sessions]:::userStyle
    U_SESSIONS --> U_SESSION_DOC["Document: [sessionId]<br/><b>SessionDocument</b><br/>-- id: String<br/>-- startTime: String<br/>-- title: String<br/>-- durationSeconds: Long<br/>-- notes: String<br/>-- isFinished: Boolean<br/>-- exercises: List { sessionExerciseId, exercise: ExerciseDto, sets: List{id, weight, reps, notes, isCompleted, createdAt}, createdAt }<br/>-- updatedAt: String (ISO)"]:::schemaStyle

    %% Tracks
    USER_DOC --> U_TRACKS[Subcollection: tracks]:::userStyle
    U_TRACKS --> U_TRACK_DOC["Document: [trackId]<br/><b>TrackDocument</b><br/>-- id: String<br/>-- name: String<br/>-- startTime: String<br/>-- durationMillis: Long<br/>-- distance: Double<br/>-- pace: String<br/>-- routePoints: List { latitude: Double, longitude: Double, timestamp: String }<br/>-- updatedAt: String (ISO)"]:::schemaStyle
```

---

## 3. Comparison of Local (Room) vs Remote (Firestore) Models

### Relational Normalized vs Nested Denormalized

The synchronization layer (via `SyncScheduler` and repositories) translates Room's normalized tables into nested Firestore documents:

| Component | Local Table Model (Room) | Firestore Document Model (Denormalized) |
| :--- | :--- | :--- |
| **Track** | `tracks` + serialized `List<TrackPoint>` field. | `TrackDocument` containing an array of `TrackPointDto` (lat, lng, ISO timestamp). |
| **Diet** | `diets` $\leftrightarrow$ `diet_meal_cross_ref` $\leftrightarrow$ `meals`. | `DietDocument` inlining an array of `DietMealDto` (which fully inlines each `MealDocument`). |
| **Meal** | `meals` $\leftrightarrow$ `meal_ingredient_cross_ref` $\leftrightarrow$ `ingredients`. | `MealDocument` inlining an array of `MealIngredientDto` (which fully inlines each `IngredientDto`). |
| **Meal Log** | `meal_logs` $\leftrightarrow$ `meal_log_meal_cross_ref` $\leftrightarrow$ `meals`. | `MealLogDocument` inlining an array of `MealLogMealDto` (which fully inlines each `MealDocument`). |
| **Routine** | `routines` $\leftrightarrow$ `routine_exercises` $\leftrightarrow$ `routine_sets`. | `RoutineDocument` containing an array of `RoutineExerciseDto` (each inlines `ExerciseDto` and a nested array of `RoutineSetDto`). |
| **Session** | `sessions` $\leftrightarrow$ `session_exercises` $\leftrightarrow$ `sets`. | `SessionDocument` containing an array of `SessionExerciseDto` (each inlines `ExerciseDto` and a nested array of performance `SetDto`s). |
