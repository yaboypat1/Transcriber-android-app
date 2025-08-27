package com.example.transcriber

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storage.TranscriptRepository
import com.example.storage.TranscriptSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    
    private val _transcripts = MutableStateFlow<List<TranscriptSegment>>(emptyList())
    val transcripts: StateFlow<List<TranscriptSegment>> = _transcripts.asStateFlow()
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _currentPartialText = MutableStateFlow<String?>(null)
    val currentPartialText: StateFlow<String?> = _currentPartialText.asStateFlow()
    
    private val _detectedLanguage = MutableStateFlow<String?>(null)
    val detectedLanguage: StateFlow<String?> = _detectedLanguage.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _targetLanguage = MutableStateFlow("en")
    val targetLanguage: StateFlow<String> = _targetLanguage.asStateFlow()

    init {
        loadTranscripts()
    }

    private fun loadTranscripts() {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual repository call
                // val allTranscripts = repository.getAllTranscripts()
                // _transcripts.value = allTranscripts
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load transcripts: ${e.message}"
            }
        }
    }

    fun addTranscript(transcript: TranscriptSegment) {
        viewModelScope.launch {
            try {
                val currentList = _transcripts.value.toMutableList()
                currentList.add(transcript)
                _transcripts.value = currentList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add transcript: ${e.message}"
            }
        }
    }

    fun updatePartialText(text: String?) {
        _currentPartialText.value = text
    }

    fun setDetectedLanguage(language: String?) {
        _detectedLanguage.value = language
    }

    fun setTargetLanguage(language: String) {
        _targetLanguage.value = language
    }

    fun setRecordingState(recording: Boolean) {
        _isRecording.value = recording
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearTranscripts() {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual repository call
                // repository.deleteAllTranscripts()
                _transcripts.value = emptyList()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear transcripts: ${e.message}"
            }
        }
    }

    fun deleteTranscript(transcript: TranscriptSegment) {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual repository call
                // repository.deleteTranscript(transcript)
                val currentList = _transcripts.value.toMutableList()
                currentList.remove(transcript)
                _transcripts.value = currentList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete transcript: ${e.message}"
            }
        }
    }
}
