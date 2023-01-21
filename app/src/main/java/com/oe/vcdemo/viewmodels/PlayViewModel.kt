package com.oe.vcdemo.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oe.vcdemo.repository.RecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val repository: RecordRepository
) : ViewModel() {

    private val _whichPlaying = MutableStateFlow(-1)
    val whichPlaying = _whichPlaying.asStateFlow()

    private var recordPath : String? = null

    var job : Job? = null

    private fun changePlaying(index: Int): Int{
        _whichPlaying.value = index
        repository.playSound(recordPath!!)
        return repository.getDuration()
    }

    fun stopPlaying(){
        _whichPlaying.value = -1
        repository.stopSound()
    }

    fun onButtonClick(index: Int){
        if(_whichPlaying.value != index) {
            job?.cancel()
            job = viewModelScope.launch() {
                stopPlaying()
                delay(changePlaying(index).toLong())
                _whichPlaying.value = -1
            }
        }else
            stopPlaying()
    }

    init {
        Log.d("init","PlayViewModel")
    }

    fun checkAndGetWav(context: Context) {

        val filesDir = context.filesDir
        val myFolderStr = filesDir.absolutePath + "/.tmp/"
        val myFolder = File(myFolderStr)
        val pathRec = "$myFolder/record.wav"
        val pathRecFile = File(pathRec)
        if (pathRecFile.exists()) {
            recordPath = pathRec
        }
        return
    }

    fun onDestroy() {
        Log.d("onDestroy","PlayViewModel")
    }

}