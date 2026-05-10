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
