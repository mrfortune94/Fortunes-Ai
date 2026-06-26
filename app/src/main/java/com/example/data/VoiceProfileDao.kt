package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceProfileDao {
    @Query("SELECT * FROM voice_profiles ORDER BY name ASC")
    fun getAllProfiles(): Flow<List<VoiceProfile>>

    @Query("SELECT * FROM voice_profiles WHERE isPrimary = 1 LIMIT 1")
    fun getPrimaryProfile(): Flow<VoiceProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: VoiceProfile)

    @Update
    suspend fun updateProfile(profile: VoiceProfile)

    @Delete
    suspend fun deleteProfile(profile: VoiceProfile)

    @Query("UPDATE voice_profiles SET isPrimary = 0")
    suspend fun clearPrimaryProfiles()
}
