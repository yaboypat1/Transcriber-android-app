package com.example.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a finalized chunk of transcription.
 */
@Entity(tableName = "transcript_segment")
data class TranscriptSegment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
