package com.example.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a single transcript segment.
 */
@Entity(tableName = "transcript_segment")
data class TranscriptSegment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String? = null,
    val translatedText: String? = null,
    val confidence: Float = 1.0f
)
