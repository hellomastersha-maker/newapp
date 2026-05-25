package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val isVoiceNote: Boolean = false
)
