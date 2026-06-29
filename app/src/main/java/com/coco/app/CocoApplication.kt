package com.coco.app

import android.app.Application
import androidx.room.Room
import com.coco.app.data.CocoDatabase
import com.coco.app.data.RoomNoteRepository
import com.coco.app.data.SettingsStore
import com.coco.app.domain.NoteRepository

/**
 * DI manual: construye la base Room, el repositorio y los ajustes una sola vez por proceso.
 * Refactor a Hilt queda como opción futura.
 */
class CocoApplication : Application() {

    private val database: CocoDatabase by lazy {
        Room.databaseBuilder(this, CocoDatabase::class.java, "coco.db")
            .addMigrations(CocoDatabase.MIGRATION_1_2, CocoDatabase.MIGRATION_2_3)
            .build()
    }

    val noteRepository: NoteRepository by lazy { RoomNoteRepository(database.noteDao()) }

    val settingsStore: SettingsStore by lazy { SettingsStore(this) }
}
