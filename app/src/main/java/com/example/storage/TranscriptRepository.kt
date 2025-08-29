package com.example.storage

import android.content.Context
import androidx.room.*

/**
 * Simple repository backed by Room for storing transcript segments.
 */
class TranscriptRepository(context: Context) {
    @Dao
    interface TranscriptDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(segment: TranscriptSegment)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAll(segments: List<TranscriptSegment>)

        @Query("SELECT * FROM transcript_segment ORDER BY id")
        suspend fun getAll(): List<TranscriptSegment>

        @Query("DELETE FROM transcript_segment WHERE timestamp < :cutoff")
        suspend fun deleteOlderThan(cutoff: Long): Int
    }

    @Database(entities = [TranscriptSegment::class], version = 1, exportSchema = false)
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

    /** Batch insert convenience. */
    @Transaction
    suspend fun insertSegments(segments: List<TranscriptSegment>) = dao.insertAll(segments)

    /** Delete segments older than N days. */
    suspend fun deleteOlderThanDays(days: Int): Int {
        val cutoff = System.currentTimeMillis() - days * 24L * 60L * 60L * 1000L
        return dao.deleteOlderThan(cutoff)
    }
}
