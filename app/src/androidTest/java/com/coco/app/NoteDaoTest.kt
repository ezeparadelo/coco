package com.coco.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.coco.app.data.CocoDatabase
import com.coco.app.data.NoteDao
import com.coco.app.data.NoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var db: CocoDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CocoDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.noteDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun notesAreOrderedByPinnedThenRecency() = runTest {
        dao.insert(NoteEntity(content = "vieja", createdAt = 100, updatedAt = 100))
        dao.insert(NoteEntity(content = "nueva", createdAt = 200, updatedAt = 200))
        dao.insert(NoteEntity(content = "fijada", createdAt = 50, updatedAt = 50, isPinned = true))

        val contents = dao.observeNotes().first().map { it.content }

        assertEquals(listOf("fijada", "nueva", "vieja"), contents)
    }
}
