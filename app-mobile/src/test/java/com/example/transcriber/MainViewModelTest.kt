package com.example.transcriber

import androidx.lifecycle.viewModelScope
import com.example.storage.TranscriptSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state`() = runTest {
        // Verify initial state
        assertEquals(false, viewModel.isRecording.first())
        assertEquals(null, viewModel.currentPartialText.first())
        assertEquals(null, viewModel.detectedLanguage.first())
        assertEquals(null, viewModel.errorMessage.first())
        assertEquals("en", viewModel.targetLanguage.first())
        assertTrue(viewModel.transcripts.first().isEmpty())
    }

    @Test
    fun `test add transcript`() = runTest {
        val transcript = TranscriptSegment(
            id = 1L,
            text = "Hello world",
            language = "en",
            confidence = 0.9f
        )

        viewModel.addTranscript(transcript)
        advanceUntilIdle()

        val transcripts = viewModel.transcripts.first()
        assertEquals(1, transcripts.size)
        assertEquals(transcript, transcripts.first())
    }

    @Test
    fun `test update partial text`() = runTest {
        val partialText = "Partial transcription"

        viewModel.updatePartialText(partialText)

        assertEquals(partialText, viewModel.currentPartialText.first())
    }

    @Test
    fun `test set detected language`() = runTest {
        val language = "es"

        viewModel.setDetectedLanguage(language)

        assertEquals(language, viewModel.detectedLanguage.first())
    }

    @Test
    fun `test set target language`() = runTest {
        val language = "fr"

        viewModel.setTargetLanguage(language)

        assertEquals(language, viewModel.targetLanguage.first())
    }

    @Test
    fun `test set recording state`() = runTest {
        viewModel.setRecordingState(true)

        assertEquals(true, viewModel.isRecording.first())
    }

    @Test
    fun `test clear error`() = runTest {
        // First set an error
        viewModel.setRecordingState(true) // This might trigger an error in real implementation
        
        // Clear the error
        viewModel.clearError()

        assertEquals(null, viewModel.errorMessage.first())
    }

    @Test
    fun `test clear transcripts`() = runTest {
        // Add some transcripts first
        val transcript1 = TranscriptSegment(text = "First transcript")
        val transcript2 = TranscriptSegment(text = "Second transcript")
        
        viewModel.addTranscript(transcript1)
        viewModel.addTranscript(transcript2)
        advanceUntilIdle()

        // Verify transcripts were added
        assertEquals(2, viewModel.transcripts.first().size)

        // Clear all transcripts
        viewModel.clearTranscripts()
        advanceUntilIdle()

        // Verify transcripts were cleared
        assertTrue(viewModel.transcripts.first().isEmpty())
    }

    @Test
    fun `test delete transcript`() = runTest {
        val transcript1 = TranscriptSegment(text = "First transcript")
        val transcript2 = TranscriptSegment(text = "Second transcript")
        
        viewModel.addTranscript(transcript1)
        viewModel.addTranscript(transcript2)
        advanceUntilIdle()

        // Verify both transcripts are present
        assertEquals(2, viewModel.transcripts.first().size)

        // Delete first transcript
        viewModel.deleteTranscript(transcript1)
        advanceUntilIdle()

        // Verify only second transcript remains
        val remainingTranscripts = viewModel.transcripts.first()
        assertEquals(1, remainingTranscripts.size)
        assertEquals(transcript2, remainingTranscripts.first())
    }

    @Test
    fun `test multiple transcript operations`() = runTest {
        val transcript1 = TranscriptSegment(text = "First")
        val transcript2 = TranscriptSegment(text = "Second")
        val transcript3 = TranscriptSegment(text = "Third")

        // Add multiple transcripts
        viewModel.addTranscript(transcript1)
        viewModel.addTranscript(transcript2)
        viewModel.addTranscript(transcript3)
        advanceUntilIdle()

        assertEquals(3, viewModel.transcripts.first().size)

        // Delete middle transcript
        viewModel.deleteTranscript(transcript2)
        advanceUntilIdle()

        val remaining = viewModel.transcripts.first()
        assertEquals(2, remaining.size)
        assertTrue(remaining.contains(transcript1))
        assertTrue(remaining.contains(transcript3))
        assertFalse(remaining.contains(transcript2))
    }

    @Test
    fun `test language state changes`() = runTest {
        // Test multiple language changes
        viewModel.setTargetLanguage("es")
        assertEquals("es", viewModel.targetLanguage.first())

        viewModel.setTargetLanguage("fr")
        assertEquals("fr", viewModel.targetLanguage.first())

        viewModel.setTargetLanguage("de")
        assertEquals("de", viewModel.targetLanguage.first())
    }

    @Test
    fun `test recording state changes`() = runTest {
        // Test recording state transitions
        viewModel.setRecordingState(true)
        assertEquals(true, viewModel.isRecording.first())

        viewModel.setRecordingState(false)
        assertEquals(false, viewModel.isRecording.first())

        viewModel.setRecordingState(true)
        assertEquals(true, viewModel.isRecording.first())
    }

    @Test
    fun `test transcript ordering`() = runTest {
        // Add transcripts with different timestamps
        val transcript1 = TranscriptSegment(
            text = "First",
            timestamp = System.currentTimeMillis() - 2000
        )
        val transcript2 = TranscriptSegment(
            text = "Second",
            timestamp = System.currentTimeMillis() - 1000
        )
        val transcript3 = TranscriptSegment(
            text = "Third",
            timestamp = System.currentTimeMillis()
        )

        viewModel.addTranscript(transcript1)
        viewModel.addTranscript(transcript2)
        viewModel.addTranscript(transcript3)
        advanceUntilIdle()

        val transcripts = viewModel.transcripts.first()
        assertEquals(3, transcripts.size)
        
        // Verify order (should be in order added)
        assertEquals(transcript1, transcripts[0])
        assertEquals(transcript2, transcripts[1])
        assertEquals(transcript3, transcripts[2])
    }

    @Test
    fun `test error handling`() = runTest {
        // Test that error states are properly managed
        viewModel.setRecordingState(true)
        
        // Simulate some operations that might cause errors
        viewModel.clearError()
        
        // Verify error is cleared
        assertEquals(null, viewModel.errorMessage.first())
    }
}
