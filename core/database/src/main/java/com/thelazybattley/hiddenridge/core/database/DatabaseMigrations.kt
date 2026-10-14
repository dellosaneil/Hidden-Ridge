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

    /**
     * Moves house types from code into their own table, so staff can add types, and gives each
     * house its own sleeps count, opening date and note. Existing houses sleep what their type did.
     */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `house_types` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                    "`plural_name` TEXT NOT NULL, `icon` TEXT NOT NULL, `sleeps` INTEGER NOT NULL, " +
                    "`sort_order` INTEGER NOT NULL, PRIMARY KEY(`id`))",
            )
            db.execSQL(
                "INSERT OR IGNORE INTO house_types (id, name, plural_name, icon, sleeps, sort_order) VALUES " +
                    "('CABIN', 'Cabin', 'Cabins', 'CABIN', 2, 0), " +
                    "('CASITA', 'Casita', 'Casitas', 'COTTAGE', 4, 1), " +
                    "('FAMILY_HOUSE', 'Family house', 'Family houses', 'VILLAGE', 8, 2)",
            )
            db.execSQL("ALTER TABLE houses ADD COLUMN sleeps INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE houses ADD COLUMN bookable_from INTEGER")
            db.execSQL("ALTER TABLE houses ADD COLUMN note TEXT NOT NULL DEFAULT ''")
            db.execSQL("UPDATE houses SET sleeps = (SELECT sleeps FROM house_types WHERE house_types.id = houses.type)")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
