package com.example.data

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerateContentResponse
import com.example.data.api.GenerationConfig
import com.example.data.api.InlineData
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.ConversationEntity
import com.example.data.database.JarvisDao
import com.example.data.database.ReminderEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class JarvisRepository(private val jarvisDao: JarvisDao) {

    val conversations: Flow<List<ConversationEntity>> = jarvisDao.getAllConversations()
    val reminders: Flow<List<ReminderEntity>> = jarvisDao.getAllReminders()

    // Database Actions
    suspend fun addMessage(sender: String, message: String) {
        withContext(Dispatchers.IO) {
            jarvisDao.insertConversation(ConversationEntity(sender = sender, message = message))
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            jarvisDao.clearAllConversations()
        }
    }

    suspend fun addReminder(title: String, content: String, isVoice: Boolean = false) {
        withContext(Dispatchers.IO) {
            jarvisDao.insertReminder(ReminderEntity(title = title, content = content, isVoiceNote = isVoice))
        }
    }

    suspend fun toggleReminder(id: Long, isCompleted: Boolean) {
        withContext(Dispatchers.IO) {
            jarvisDao.updateReminderStatus(id, isCompleted)
        }
    }

    suspend fun deleteReminder(id: Long) {
        withContext(Dispatchers.IO) {
            jarvisDao.deleteReminder(id)
        }
    }

    // Gemini API integration with conversation history and J.A.R.V.I.S role-play
    suspend fun askJarvis(prompt: String, previousHistory: List<ConversationEntity>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Sir, my core uplink key is offline. Please configure your API Key in the AI Studio Secrets panel."
        }

        // Convert conversation entities to Gemini historical structures
        val contentsList = mutableListOf<Content>()
        
        // Add historic messages
        previousHistory.takeLast(10).forEach { msg ->
            contentsList.add(
                Content(
                    parts = listOf(
                        Part(text = msg.message)
                    )
                )
            )
        }

        // Add current user prompt
        contentsList.add(
            Content(
                parts = listOf(
                    Part(text = prompt)
                )
            )
        )

        val systemInstruction = Content(
            parts = listOf(
                Part(
                    text = "You are J.A.R.V.I.S., an incredibly advanced, elegant, loyal, and clever cinematic AI operating system inspired by Tony Stark's personal assistant. " +
                            "Your tone is refined, English, slightly witty and sarcastic, yet deeply professional and helpful. " +
                            "Address the user as 'Sir' or 'Ma'am'. State your system diagnostics or results proudly. " +
                            "You are extremely brief, smart, and fully visual. Avoid long prose unless requested. You can handle English and Malayalam."
                )
            )
        )

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = systemInstruction
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, Sir. My analysis returned null diagnostics."
        } catch (e: Exception) {
            Log.e("JarvisRepository", "Gemini API Error", e)
            "Sir, my sub-processors encountered an anomaly: ${e.localizedMessage ?: "Connection breach"}"
        }
    }

    // Multimodal prompt for AI Vision
    suspend fun analyzeImageWithJarvis(prompt: String, bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Sir, my optical uplink key is offline. Please configure your API key."
        }

        val base64Image = bitmap.toBase64()
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt.ifEmpty() { "Sir requests a visual scan. Analyze this image and describe what you see in J.A.R.V.I.S style." }),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.4f),
            systemInstruction = Content(
                parts = listOf(
                    Part(
                        text = "You are J.A.R.V.I.S.. Sir has activated your AI Vision module. Scan the image, perform OCR or object detection as required, and explain your sci-fi analytics in a cinematic, sophisticated butler style."
                    )
                )
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Optic array returned zero-result diagnostics, Sir."
        } catch (e: Exception) {
            Log.e("JarvisRepository", "Gemini API Vision Error", e)
            "Sir, visual core uplink breach: ${e.localizedMessage ?: "Unknown hardware failure"}"
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
