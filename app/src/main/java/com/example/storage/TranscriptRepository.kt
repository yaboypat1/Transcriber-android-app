package com.example.storage

import android.content.Context
import androidx.room.*

/**
 * Simple repository backed by Room for storing transcript segments.
 */
class TranscriptRepository(context: Context) {
    @Dao
    interface TranscriptDao {
        @Insert
        suspend fun insert(segment: TranscriptSegment)

        @Query("SELECT * FROM transcript_segment ORDER BY id")
        suspend fun getAll(): List<TranscriptSegment>
    }

    @Database(entities = [TranscriptSegment::class], version = 1)
    abstract class TranscriptDatabase : RoomDatabase() {
        abstract fun transcriptDao(): TranscriptDao
    }

    private val dao: TranscriptDao = Room.databaseBuilder(
        context.applicationContext,
        TranscriptDatabase::class.java,
        "transcripts.db",
    ).build().transcriptDao()

    /** Inserts a finalized transcript segment. */
    suspend fun insertSegment(segment: TranscriptSegment) = dao.insert(segment)

    /** Returns all stored transcript segments. */
    suspend fun getSegments(): List<TranscriptSegment> = dao.getAll()
}

