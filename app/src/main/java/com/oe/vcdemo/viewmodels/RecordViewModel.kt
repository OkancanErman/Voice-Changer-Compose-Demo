package com.oe.vcdemo.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.oe.vcdemo.helper.ExtAudioRecorder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(

) : ViewModel() {
    var recorder: ExtAudioRecorder? = null

    private val _recordingState = MutableStateFlow(false)
    val recordingState = _recordingState.asStateFlow()

    var pathRec: String = ""
    lateinit var myFolder: File

    fun createFolders(context: Context) {
        val filesDir = context.filesDir
        val myFolderStr = filesDir.absolutePath + "/.tmp/"

        myFolder = File(myFolderStr)
        pathRec = "$myFolder/record.wav"
    }

    fun checkWavExist(): Boolean {
        val pathRecFile = File(pathRec)
        return pathRecFile.exists()
    }

    fun startRecording() {
        val noMedia = File(myFolder, ".nomedia")

        if (!myFolder.exists()) {
            myFolder.mkdirs()
        }
        try {
            noMedia.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        recorder = ExtAudioRecorder.getInstance(false) // Uncompressed recording (WAV)

        try {
            recorder?.apply {
                setOutputFile(pathRec)
                prepare()
                start()
            }
            _recordingState.value = true
        } catch (e: IOException) {
            _recordingState.value = false
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (_: Exception) {
        }
        _recordingState.value = false
    }
}