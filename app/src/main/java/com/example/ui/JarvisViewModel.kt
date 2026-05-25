package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.JarvisRepository
import com.example.data.database.ConversationEntity
import com.example.data.database.JarvisDatabase
import com.example.data.database.ReminderEntity
import com.example.util.SystemCommandHelper
import com.example.util.JarvisThemeEngine
import com.example.util.ThemingMode
import com.example.util.JarvisOfflineCommandModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class JarvisState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}

class JarvisViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val database = JarvisDatabase.getDatabase(application)
    private val repository = JarvisRepository(database.jarvisDao())

    // Live Flow data from Database
    val conversations: StateFlow<List<ConversationEntity>> = repository.conversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Internal states linked to dynamic performance engine
    val isLiteMode: StateFlow<Boolean> = JarvisThemeEngine.isLiteActive

    private val _selectedTab = MutableStateFlow("hud")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _jarvisState = MutableStateFlow(JarvisState.IDLE)
    val jarvisState: StateFlow<JarvisState> = _jarvisState.asStateFlow()

    private val _speechText = MutableStateFlow("")
    val speechText: StateFlow<String> = _speechText.asStateFlow()

    private val _visionScanResult = MutableStateFlow<String?>(null)
    val visionScanResult: StateFlow<String?> = _visionScanResult.asStateFlow()

    // Speech & Text-To-Speech
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    init {
        // Initialize dynamic performance theming engine
        JarvisThemeEngine.init(application)
        
        // Initialize Speech TTS
        textToSpeech = TextToSpeech(application, this)
        initSpeechRecognizer()
    }

    private fun initSpeechRecognizer() {
        viewModelScope.launch {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication()).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {
                                _speechText.value = ""
                                _jarvisState.value = JarvisState.LISTENING
                            }

                            override fun onBeginningOfSpeech() {}
                            override fun onRmsChanged(rmsdB: Float) {
                                // Can be used for dynamic wave amplitudes
                            }

                            override fun onBufferReceived(buffer: ByteArray?) {}
                            override fun onEndOfSpeech() {
                                _jarvisState.value = JarvisState.PROCESSING
                            }

                            override fun onError(error: Int) {
                                Log.e("JarvisViewModel", "Speech error code: $error")
                                _jarvisState.value = JarvisState.IDLE
                            }

                            override fun onResults(results: Bundle?) {
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val command = matches?.firstOrNull()
                                if (!command.isNullOrEmpty()) {
                                    _speechText.value = command
                                    processTextPrompt(command)
                                } else {
                                    _jarvisState.value = JarvisState.IDLE
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle?) {}
                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                }
            } catch (e: Exception) {
                Log.e("JarvisViewModel", "Failed to setup speech recognizer: ${e.message}")
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.UK)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isTtsReady = true
                speakOut("System core active. Good morning, Sir. J.A.R.V.I.S. online.")
            }
        }
    }

    // Tab coordination
    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    // Toggle performance modes manually (AUTO, FORCE_FULL, FORCE_LITE)
    fun setThemingMode(mode: ThemingMode) {
        JarvisThemeEngine.setThemingMode(getApplication(), mode)
    }

    // Legacy quick toggle
    fun toggleLiteMode() {
        val currentActive = isLiteMode.value
        val nextMode = if (currentActive) ThemingMode.FORCE_FULL else ThemingMode.FORCE_LITE
        setThemingMode(nextMode)
    }

    // Trigger System beep or TTS speaking
    fun speakOut(message: String) {
        if (isTtsReady && message.isNotEmpty()) {
            _jarvisState.value = JarvisState.SPEAKING
            textToSpeech?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "jarvis_tts_id")
            // Schedule back to idle after speaking completes (mock completion callback)
            viewModelScope.launch {
                val delayTime = (message.length * 70L).coerceAtLeast(1500L)
                kotlinx.coroutines.delay(delayTime)
                if (_jarvisState.value == JarvisState.SPEAKING) {
                    _jarvisState.value = JarvisState.IDLE
                }
            }
        }
    }

    // Start Listening Mode
    fun startVoiceListening() {
        if (speechRecognizer != null) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } else {
            _jarvisState.value = JarvisState.LISTENING
            // Mock voice prompt or guide user
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000)
                _jarvisState.value = JarvisState.IDLE
                speakOut("Voice hardware modules are busy, Sir. Please utilize secure text inputs.")
            }
        }
    }

    fun stopVoiceListening() {
        speechRecognizer?.stopListening()
    }

    // Process all texts (either via Voice input or direct Chat prompt)
    fun processTextPrompt(prompt: String) {
        val cleanedPrompt = prompt.trim()
        if (cleanedPrompt.isEmpty()) return

        _jarvisState.value = JarvisState.PROCESSING

        viewModelScope.launch {
            // Save prompt in DB
            repository.addMessage("user", cleanedPrompt)

            // 1. Try to intercept system controls offline
            val context = getApplication<Application>().applicationContext
            val actionResult = JarvisOfflineCommandModule.processOfflineCommand(context, cleanedPrompt, this@JarvisViewModel)
            if (actionResult != null) {
                // Intercepted offline intent!
                repository.addMessage("jarvis", actionResult)
                speakOut(actionResult)
                _jarvisState.value = JarvisState.IDLE
                return@launch
            }

            // 2. Productivity note interceptor (e.g., "add reminder write emails" or "add note grocery shopping")
            if (cleanedPrompt.lowercase(Locale.ROOT).startsWith("add reminder") || cleanedPrompt.lowercase(Locale.ROOT).startsWith("add note")) {
                val remContent = cleanedPrompt.substringAfter("reminder").substringAfter("note").trim()
                if (remContent.isNotEmpty()) {
                    repository.addReminder("Sir's Priority Task", remContent, isVoice = false)
                    val resultText = "Task anchored successfully to secondary schedule grids, Sir: \"$remContent\"."
                    repository.addMessage("jarvis", resultText)
                    speakOut(resultText)
                    _jarvisState.value = JarvisState.IDLE
                    return@launch
                }
            }

            // 3. Command: Clear database history
            if (cleanedPrompt.lowercase(Locale.ROOT) == "clear logs" || cleanedPrompt.lowercase(Locale.ROOT) == "delete history") {
                repository.clearHistory()
                val clearText = "Diagnostics database purged, Sir. Operational logs reset."
                repository.addMessage("jarvis", clearText)
                speakOut(clearText)
                _jarvisState.value = JarvisState.IDLE
                return@launch
            }

            // 4. Default: Query conversational brain Gemini REST API
            val currentHistory = conversations.value
            val response = repository.askJarvis(cleanedPrompt, currentHistory)

            // Save Response in DB
            repository.addMessage("jarvis", response)
            speakOut(response)
        }
    }

    // Productivity tasks handlers
    fun addNewReminder(title: String, content: String) {
        viewModelScope.launch {
            repository.addReminder(title, content)
            speakOut("Record added to database logs.")
        }
    }

    fun toggleTask(id: Long, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, completed)
        }
    }

    fun purgeTask(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    // AI Vision Multimodal scanner
    fun runVisionScan(bitmap: Bitmap, prompt: String) {
        _jarvisState.value = JarvisState.PROCESSING
        _visionScanResult.value = "Sir, optical sensors are acquiring visual coordinates... scanning matrix..."
        
        viewModelScope.launch {
            val diagnosis = repository.analyzeImageWithJarvis(prompt, bitmap)
            _visionScanResult.value = diagnosis
            repository.addMessage("jarvis_vision", "[Optical Scan Result]\n$diagnosis")
            speakOut(diagnosis)
            _jarvisState.value = JarvisState.IDLE
        }
    }

    fun resetVisionScan() {
        _visionScanResult.value = null
    }

    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }
}
