package com.thelazybattley.hiddenridge.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DatabaseMigrations {
    /** Adds check-in and check-out flags to bookings; existing bookings start with neither. */
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE bookings ADD COLUMN checked_in INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE bookings ADD COLUMN checked_out INTEGER NOT NULL DEFAULT 0")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
