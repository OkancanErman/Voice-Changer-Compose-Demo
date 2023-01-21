package com.oe.vcdemo.repository

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecordRepository @Inject constructor(
    private val app: Application,
) {
    private var mediaPlayer: MediaPlayer? = null

    fun playSound(uri:String) {
        mediaPlayer = MediaPlayer.create(app, Uri.parse(uri))
        mediaPlayer?.start()
    }

    fun stopSound() {
        mediaPlayer?.pause()
    }

    fun getDuration() : Int {
        return mediaPlayer?.duration!!
    }

}