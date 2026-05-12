package eric.bitria.minimalfit.data.database

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS routines (
                id TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS routine_exercise_cross_refs (
                routineId TEXT NOT NULL,
                exerciseId TEXT NOT NULL,
                PRIMARY KEY(routineId, exerciseId)
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE sets ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "ALTER TABLE routine_exercise_cross_refs ADD COLUMN targetSets INTEGER NOT NULL DEFAULT 1"
        )
        connection.execSQL(
            "ALTER TABLE routine_exercise_cross_refs ADD COLUMN targetReps INTEGER NOT NULL DEFAULT 0"
        )
        connection.execSQL(
            "ALTER TABLE routine_exercise_cross_refs ADD COLUMN targetWeight REAL NOT NULL DEFAULT 0"
        )
        connection.execSQL(
            "ALTER TABLE routine_exercise_cross_refs ADD COLUMN position INTEGER NOT NULL DEFAULT 0"
        )
    }
}
