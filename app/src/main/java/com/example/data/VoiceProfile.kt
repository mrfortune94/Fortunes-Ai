package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_profiles")
data class VoiceProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val voiceId: String,
    val isPrimary: Boolean = false
)
