package com.example.transcriber

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.storage.TranscriptRepository
import com.example.storage.TranscriptRepository.TranscriptDatabase
import com.example.storage.TranscriptRepository.TranscriptDao
import com.example.storage.TranscriptSegment
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.annotation.Config

@Config(sdk = [34])
class TranscriptDaoTest {
    private lateinit var context: Context
    private lateinit var db: TranscriptDatabase
    private lateinit var dao: TranscriptDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, TranscriptDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.transcriptDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndBatchInsertAndQuery() {
        dao.apply {
            kotlinx.coroutines.runBlocking {
                insert(TranscriptSegment(text = "hello"))
                insertAll(listOf(
                    TranscriptSegment(text = "world"),
                    TranscriptSegment(text = "!"),
                ))
                val all = getAll()
                assertEquals(3, all.size)
                assertEquals("hello", all[0].text)
            }
        }
    }

    @Test
    fun deleteOlderThanDays() = kotlinx.coroutines.runBlocking {
        val old = TranscriptSegment(text = "old", timestamp = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L)
        val recent = TranscriptSegment(text = "recent", timestamp = System.currentTimeMillis())
        dao.insertAll(listOf(old, recent))
        val cutoffDays = 2
        val deleted = dao.deleteOlderThan(System.currentTimeMillis() - cutoffDays * 24L * 60L * 60L * 1000L)
        assertEquals(1, deleted)
        val all = dao.getAll()
        assertEquals(1, all.size)
        assertEquals("recent", all.first().text)
    }
}

