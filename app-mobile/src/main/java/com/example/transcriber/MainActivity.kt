package com.example.transcriber

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.AudioProcessor
import com.example.captions.EnhancedCaptionManager
import com.example.language.LanguageIdentifier
import com.example.session.AsrSession
import com.example.storage.TranscriptRepository
import com.example.storage.TranscriptSegment
import com.example.transcriber.translation.MlKitTranslatorPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_CODE = 123
    }

    private lateinit var asrSession: AsrSession
    private lateinit var audioProcessor: AudioProcessor
    private lateinit var languageIdentifier: LanguageIdentifier
    private lateinit var translator: MlKitTranslatorPool
    private lateinit var repository: TranscriptRepository
    private lateinit var captionManager: EnhancedCaptionManager
    private val scope = CoroutineScope(Dispatchers.Main)
    private val mainViewModel: MainViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startTranscription()
        } else {
            Toast.makeText(this, "Microphone permission required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "MainActivity created")
        
        // Initialize components
        initializeComponents()
        
        // Check permissions
        checkPermissions()
        
        setContent {
            TranscriberApp(
                onStartRecording = { startTranscription() },
                onStopRecording = { stopTranscription() },
                onLanguageChanged = { languageCode ->
                    scope.launch {
                        translator.setTargetLanguage(languageCode)
                    }
                }
            )
        }
    }

    private fun initializeComponents() {
        try {
            asrSession = AsrSession(this)
            audioProcessor = AudioProcessor(this)
            languageIdentifier = LanguageIdentifier(this)
            translator = MlKitTranslatorPool
            repository = TranscriptRepository(this)
            captionManager = EnhancedCaptionManager(this, scope)

            // Set up ASR callbacks
            asrSession.setOnPartialResult { text ->
                captionManager.onPartial(text)
            }
            asrSession.setOnFinalResult { text ->
                scope.launch {
                    processFinalResult(text)
                }
            }

            // Set up audio processor callbacks
            audioProcessor.setOnVoiceActivity { hasVoice ->
                if (!hasVoice && audioProcessor.hasVoiceActivityEnded()) {
                    asrSession.stopListening()
                }
            }
            audioProcessor.setOnError { error ->
                captionManager.onError(error)
            }

            Log.d(TAG, "Components initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize components", e)
            Toast.makeText(this, "Initialization failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Microphone permission already granted")
            }
            shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) -> {
                Toast.makeText(this, "Microphone permission needed for transcription", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startTranscription() {
        try {
            audioProcessor.startRecording()
            asrSession.startListening()
            mainViewModel.setRecordingState(true)
            Log.d(TAG, "Transcription started")
            Toast.makeText(this, "Transcription started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start transcription", e)
            Toast.makeText(this, "Failed to start: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopTranscription() {
        try {
            audioProcessor.stopRecording()
            asrSession.stopListening()
            mainViewModel.setRecordingState(false)
            Log.d(TAG, "Transcription stopped")
            Toast.makeText(this, "Transcription stopped", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop transcription", e)
            Toast.makeText(this, "Failed to stop: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun processFinalResult(text: String) {
        try {
            // Identify language
            val detectedLanguage = languageIdentifier.identifyLanguage(text)
            detectedLanguage?.let { lang ->
                captionManager.onLanguageDetected(lang)
                Log.d(TAG, "Language detected: $lang")
            }

            // Store transcript
            val segment = TranscriptSegment(
                text = text,
                language = detectedLanguage,
                confidence = 0.9f
            )
            repository.insertSegment(segment)

            // Show final result
            captionManager.onFinal(text)

            Log.d(TAG, "Final result processed: $text")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing final result", e)
            captionManager.onError("Result processing failed: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            asrSession.close()
            audioProcessor.release()
            languageIdentifier.release()
            captionManager.setOnError { }
            Log.d(TAG, "MainActivity destroyed, resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranscriberApp(
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onLanguageChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()
    
    var isRecording by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("en") }
    var transcripts by remember { mutableStateOf(listOf<TranscriptSegment>()) }
    
    val languages = listOf(
        "en" to "English",
        "es" to "Spanish", 
        "fr" to "French",
        "de" to "German",
        "it" to "Italian",
        "pt" to "Portuguese",
        "ru" to "Russian",
        "ja" to "Japanese",
        "ko" to "Korean",
        "zh" to "Chinese"
    )

    LaunchedEffect(Unit) {
        viewModel.transcripts.collect { transcriptList ->
            transcripts = transcriptList
        }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Transcriber", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Language Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Target Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        languages.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { (code, name) ->
                                    FilterChip(
                                        onClick = {
                                            selectedLanguage = code
                                            onLanguageChanged(code)
                                        },
                                        label = { Text(name) },
                                        selected = selectedLanguage == code,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Recording Controls
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isRecording) "Recording..." else "Ready to Record",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    isRecording = true
                                    onStartRecording()
                                },
                                enabled = !isRecording,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Start Recording")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start")
                            }
                            
                            Button(
                                onClick = {
                                    isRecording = false
                                    onStopRecording()
                                },
                                enabled = isRecording,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop Recording")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Stop")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Transcripts List
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Transcripts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (transcripts.isEmpty()) {
                            Text(
                                text = "No transcripts yet. Start recording to see results here.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(transcripts.reversed()) { transcript ->
                                    TranscriptItem(transcript = transcript)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TranscriptItem(transcript: TranscriptSegment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = transcript.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                transcript.language?.let { lang ->
<<<<<<< HEAD
                    AssistChip(
                        onClick = { },
                        label = { Text(lang.uppercase(), fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
=======
                    SuggestionChip(
                        onClick = {},
                        label = { Text(lang.uppercase(), fontSize = 12.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
>>>>>>> fb9df69ed72c58ee2bc168e83960bedd7bd752db
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                }
                
                Text(
                    text = formatTimestamp(transcript.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            transcript.translatedText?.let { translated ->
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = translated,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return format.format(date)
}
