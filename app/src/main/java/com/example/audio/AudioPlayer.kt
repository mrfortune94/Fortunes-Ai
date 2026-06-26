package com.example.audio

import android.content.Context
import android.media.MediaPlayer
import java.io.File
import java.io.FileOutputStream
import okhttp3.ResponseBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    suspend fun playAudio(responseBody: ResponseBody) {
        withContext(Dispatchers.IO) {
            try {
                val tempFile = File.createTempFile("voice_", ".mp3", context.cacheDir)
                val inputStream = responseBody.byteStream()
                val outputStream = FileOutputStream(tempFile)
                
                inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                
                withContext(Dispatchers.Main) {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        prepare()
                        start()
                        setOnCompletionListener {
                            tempFile.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
