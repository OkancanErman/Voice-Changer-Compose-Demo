package com.oe.vcdemo.helper

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.AudioRecord.OnRecordPositionUpdateListener
import kotlin.Throws
import android.os.Environment
import android.util.Log
import java.io.*
import java.lang.Exception
import java.lang.IllegalStateException

class ExtAudioRecorder(
    uncompressed: Boolean,
    audioSource: Int,
    sampleRate: Int,
    channelConfig: Int,
    audioFormat: Int
) {
    /**
     * INITIALIZING : recorder is initializing;
     * READY: 		recorder has been initialized, recorder not yet started
     * RECORDING:	recording
     * ERROR: 		reconstruction needed
     * STOPPED: 		reset needed
     */
    enum class State {
        INITIALIZING, READY, RECORDING, ERROR, STOPPED
    }

    // Toggles uncompressed recording on/off; RECORDING_UNCOMPRESSED / RECORDING_COMPRESSED
    private var rUncompressed = false

    // Recorder used for uncompressed recording
    private var audioRecorder: AudioRecord? = null

    // Recorder used for compressed recording
    private var mediaRecorder: MediaRecorder? = null

    // Stores current amplitude (only in uncompressed mode)
    private var cAmplitude = 0

    // Output file path
    private var filePath: String? = null

    /**
     *
     * Returns the state of the recorder in a RehearsalAudioRecord.State typed object.
     * Useful, as no exceptions are thrown.
     *
     * @return recorder state
     */
    // Recorder state; see State
    var state: State? = null
        private set

    // File writer (only in uncompressed mode)
    private var randomAccessWriter: RandomAccessFile? = null

    // Number of channels, sample rate, sample size(size in bits), buffer size, audio source, sample size(see AudioFormat)
    private var nChannels: Short = 0
    private var sRate = 0
    private var bSamples: Short = 0
    private var bufferSize = 0
    private var aSource = 0
    private var aFormat = 0

    // Number of frames written to file on each output(only in uncompressed mode)
    private var framePeriod = 0

    // Buffer for output(only in uncompressed mode)
    private lateinit var buffer: ByteArray

    // Number of bytes written to file after header(only in uncompressed mode)
    // after stop() is called, this size is written to the header/data chunk in the wave file
    private var payloadSize = 0
    private var iBufSizeWav = 0
    var byPattern: Byte = 0
    var typeConverter: TypeConverter? = null
    private var iReadSampleRate = 0
    private val byMusic = ByteArray(iBufSize) // Read the file into the "music" array
    private val iSigQual = 0

    /*
    *
    * Method used for recording.
    *
    */
    private val updateListener: OnRecordPositionUpdateListener =
        object : OnRecordPositionUpdateListener {
            override fun onPeriodicNotification(recorder: AudioRecord) {
                audioRecorder!!.read(buffer, 0, buffer.size) // Fill buffer
                try {
                    randomAccessWriter!!.write(buffer) // Write buffer to file
                    payloadSize += buffer.size
                    if (bSamples.toInt() == 16) {
                        for (i in 0 until buffer.size / 2) { // 16bit sample size
                            val curSample =
                                typeConverter!!.getShort(buffer[i * 2], buffer[i * 2 + 1])
                            if (curSample > cAmplitude) { // Check amplitude
                                cAmplitude = curSample.toInt()
                            }
                        }
                    } else { // 8bit sample size
                        for (i in buffer.indices) {
                            if (buffer[i] > cAmplitude) { // Check amplitude
                                cAmplitude = buffer[i].toInt()
                            }
                        }
                    }
                } catch (e: IOException) {
                    Log.e(
                        ExtAudioRecorder::class.java.name,
                        "Error occured in updateListener, recording is aborted"
                    )
                    //stop();
                }
            }

            override fun onMarkerReached(recorder: AudioRecord) {
                // NOT USED
            }
        }

    /**
     * Sets output file path, call directly after construction/reset.
     *
     * @param output file path
     */
    fun setOutputFile(argPath: String?) {
        try {
            if (state == State.INITIALIZING) {
                filePath = argPath
                if (!rUncompressed) {
                    mediaRecorder!!.setOutputFile(filePath)
                }
            }
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e(ExtAudioRecorder::class.java.name, e.message!!)
            } else {
                Log.e(
                    ExtAudioRecorder::class.java.name,
                    "Unknown error occured while setting output path"
                )
            }
            state = State.ERROR
        }
    }

    /**
     *
     * Returns the largest amplitude sampled since the last call to this method.
     *
     * @return returns the largest amplitude since the last call, or 0 when not in recording state.
     */
    val maxAmplitude: Int
        get() = if (state == State.RECORDING) {
            if (rUncompressed) {
                val result = cAmplitude
                cAmplitude = 0
                result
            } else {
                try {
                    mediaRecorder!!.maxAmplitude
                } catch (e: IllegalStateException) {
                    0
                }
            }
        } else {
            0
        }

    /**
     *
     * Prepares the recorder for recording, in case the recorder is not in the INITIALIZING state and the file path was not set
     * the recorder is set to the ERROR state, which makes a reconstruction necessary.
     * In case uncompressed recording is toggled, the header of the wave file is written.
     * In case of an exception, the state is changed to ERROR
     *
     */
    fun prepare() {
        try {
            if (state == State.INITIALIZING) {
                if (rUncompressed) {
                    if ((audioRecorder!!.state == AudioRecord.STATE_INITIALIZED) and (filePath != null)) {
                        // write file header
                        randomAccessWriter = RandomAccessFile(filePath, "rw")
                        randomAccessWriter!!.setLength(0) // Set file length to 0, to prevent unexpected behavior in case the file already existed
                        randomAccessWriter!!.writeBytes("RIFF")
                        randomAccessWriter!!.writeInt(0) // Final file size not known yet, write 0
                        randomAccessWriter!!.writeBytes("WAVE")
                        randomAccessWriter!!.writeBytes("fmt ")
                        randomAccessWriter!!.writeInt(Integer.reverseBytes(16)) // Sub-chunk size, 16 for PCM
                        randomAccessWriter!!.writeShort(
                            java.lang.Short.reverseBytes(1.toShort()).toInt()
                        ) // AudioFormat, 1 for PCM
                        randomAccessWriter!!.writeShort(
                            java.lang.Short.reverseBytes(nChannels).toInt()
                        ) // Number of channels, 1 for mono, 2 for stereo
                        randomAccessWriter!!.writeInt(Integer.reverseBytes(sRate)) // Sample rate
                        randomAccessWriter!!.writeInt(Integer.reverseBytes(sRate * bSamples * nChannels / 8)) // Byte rate, SampleRate*NumberOfChannels*BitsPerSample/8
                        randomAccessWriter!!.writeShort(
                            java.lang.Short.reverseBytes((nChannels * bSamples / 8).toShort())
                                .toInt()
                        ) // Block align, NumberOfChannels*BitsPerSample/8
                        randomAccessWriter!!.writeShort(
                            java.lang.Short.reverseBytes(bSamples).toInt()
                        ) // Bits per sample
                        randomAccessWriter!!.writeBytes("data")
                        randomAccessWriter!!.writeInt(0) // Data chunk size not known yet, write 0
                        buffer = ByteArray(framePeriod * bSamples / 8 * nChannels)
                        state = State.READY
                    } else {
                        Log.e(
                            ExtAudioRecorder::class.java.name,
                            "prepare() method called on uninitialized recorder"
                        )
                        state = State.ERROR
                    }
                } else {
                    mediaRecorder!!.prepare()
                    state = State.READY
                }
            } else {
                Log.e(ExtAudioRecorder::class.java.name, "prepare() method called on illegal state")
                release()
                state = State.ERROR
            }
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e(ExtAudioRecorder::class.java.name, e.message!!)
            } else {
                Log.e(ExtAudioRecorder::class.java.name, "Unknown error occured in prepare()")
            }
            state = State.ERROR
        }
    }

    /**
     *
     *
     * Releases the resources associated with this class, and removes the unnecessary files, when necessary
     * @throws Exception
     */
    @Throws(Exception::class)
    fun release() {
        if (state == State.RECORDING) {
            stop()
        } else {
            if ((state == State.READY) and rUncompressed) {
                try {
                    randomAccessWriter!!.close() // Remove prepared file
                } catch (e: IOException) {
                    Log.e(
                        ExtAudioRecorder::class.java.name,
                        "I/O exception occured while closing output file"
                    )
                }
                File(filePath).delete()
            }
        }
        if (rUncompressed) {
            if (audioRecorder != null) {
                audioRecorder!!.release()
            }
        } else {
            if (mediaRecorder != null) {
                mediaRecorder!!.release()
            }
        }
    }

    /**
     *
     *
     * Resets the recorder to the INITIALIZING state, as if it was just created.
     * In case the class was in RECORDING state, the recording is stopped.
     * In case of exceptions the class is set to the ERROR state.
     *
     */
    fun reset() {
        try {
            if (state != State.ERROR) {
                release()
                filePath = null // Reset file path
                cAmplitude = 0 // Reset amplitude
                if (rUncompressed) {
                    audioRecorder = AudioRecord(aSource, sRate, nChannels + 1, aFormat, bufferSize)
                } else {
                    mediaRecorder = MediaRecorder()
                    mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                    mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                    mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                }
                state = State.INITIALIZING
            }
        } catch (e: Exception) {
            Log.e(ExtAudioRecorder::class.java.name, e.message!!)
            state = State.ERROR
        }
    }

    /**
     *
     *
     * Starts the recording, and sets the state to RECORDING.
     * Call after prepare().
     *
     */
    fun start() {
        if (state == State.READY) {
            if (rUncompressed) {
                payloadSize = 0
                audioRecorder!!.startRecording()
                audioRecorder!!.read(buffer, 0, buffer.size)
            } else {
                mediaRecorder!!.start()
            }
            state = State.RECORDING
        } else {
            Log.e(ExtAudioRecorder::class.java.name, "start() called on illegal state")
            state = State.ERROR
        }
    }

    /**
     *
     *
     * Stops the recording, and sets the state to STOPPED.
     * In case of further usage, a reset is needed.
     * Also finalizes the wave file in case of uncompressed recording.
     * @throws Exception
     */
    @Throws(Exception::class)
    fun stop() {
        if (state == State.RECORDING) {
            if (rUncompressed) {
                audioRecorder!!.stop()
                try {
                    randomAccessWriter!!.seek(4) // Write size to RIFF header
                    randomAccessWriter!!.writeInt(Integer.reverseBytes(36 + payloadSize))
                    randomAccessWriter!!.seek(40) // Write size to Subchunk2Size field
                    randomAccessWriter!!.writeInt(Integer.reverseBytes(payloadSize))
                    randomAccessWriter!!.close()
                } catch (e: IOException) {
                    Log.e(
                        ExtAudioRecorder::class.java.name,
                        "I/O exception occured while closing output file"
                    )
                    state = State.ERROR
                }
            } else {
                mediaRecorder!!.stop()
            }
            state = State.STOPPED
        } else {
            Log.e(ExtAudioRecorder::class.java.name, "stop() called on illegal state")
            state = State.ERROR
        }
    }

    // Read a existing *.wav file from Application directories
    @Throws(IOException::class)
    fun readWav(): Boolean {
        var i = 0
        var bSuccess = true

        val file = File(Environment.getExternalStorageDirectory().toString() + "/.tmp/record.wav")
        val `is`: InputStream = FileInputStream(file)
        val bis = BufferedInputStream(`is`, iBufSize)
        val dis = DataInputStream(bis) // Read audio data from saved file

        // Read the header data from *.wav file
        readWavHeader(dis)
        while (dis.available() > 0 && bSuccess != false) {
            byMusic[i] = dis.readByte()
            i++
            if (i >= iBufSize) {
                // The recorded file is bigger than the default buffer
                bSuccess = false
            }
        }
        dis.close()

        // Set the size of the wav file
        iBufSizeWav = i
        return bSuccess
    }

    @Throws(IOException::class)
    fun readWavHeader(dis: DataInputStream) {
        val bySize: Byte = 44
        val byTemp = ByteArray(bySize.toInt())
        val typeConverter = TypeConverter()
        for (i in 0 until bySize) {
            byTemp[i] = dis.readByte()
        }
        iReadSampleRate = typeConverter.getInt(byTemp[24], byTemp[25], byTemp[26], byTemp[27])
    }

    fun GetPattern(): Byte {
        return byPattern
    }

    fun GetSignalQuality(): Int {
        return iSigQual
    }

    companion object {
        fun getInstance(recordingCompressed: Boolean): ExtAudioRecorder? {
            var result: ExtAudioRecorder? = null
            if (recordingCompressed) // 8000 i alıyor #compressed
            {
                result = ExtAudioRecorder(
                    false,
                    MediaRecorder.AudioSource.MIC,
                    sampleRates[3],
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
            } else {
                var i = 0 // Sample rate 44100
                do {
                    result = ExtAudioRecorder(
                        true,
                        MediaRecorder.AudioSource.MIC,
                        sampleRates[i],
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                } while ((++i < sampleRates.size) and (result!!.state != State.INITIALIZING)) //44100 den 8000 e deniyor ilk initalize olanı alıyor
            }
            return result
        }

        const val RECORDING_UNCOMPRESSED = true
        const val RECORDING_COMPRESSED = false

        // The interval in which the recorded samples are output to the file
        // Used only in uncompressed mode
        private const val TIMER_INTERVAL = 120
        private val sampleRates = intArrayOf(44100, 22050, 11025, 8000)
        private val recordingThread: Thread? = null

        // find out the size of the file
        private const val iBufSize = 1200000
    }

    /**
     *
     *
     * Default constructor
     *
     * Instantiates a new recorder, in case of compressed recording the parameters can be left as 0.
     * In case of errors, no exception is thrown, but the state is set to ERROR
     *
     */
    init {
        try {
            rUncompressed = uncompressed
            if (rUncompressed) { // RECORDING_UNCOMPRESSED
                bSamples = if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
                    16
                } else {
                    8
                }
                nChannels = if (channelConfig != AudioFormat.CHANNEL_IN_STEREO) {
                    1
                } else {
                    2
                }
                aSource = audioSource
                sRate = sampleRate
                aFormat = audioFormat
                framePeriod = sampleRate * TIMER_INTERVAL / 1000
                bufferSize = framePeriod * 2 * bSamples * nChannels / 8
                if (bufferSize < AudioRecord.getMinBufferSize(
                        sampleRate,
                        channelConfig,
                        audioFormat
                    )
                ) { // Check to make sure buffer size is not smaller than the smallest allowed one
                    bufferSize =
                        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                    // Set frame period and timer interval accordingly
                    framePeriod = bufferSize / (2 * bSamples * nChannels / 8)
                    Log.w(
                        ExtAudioRecorder::class.java.name,
                        "Increasing buffer size to " + Integer.toString(bufferSize)
                    )
                }
                audioRecorder =
                    AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSize)
                if (audioRecorder!!.state != AudioRecord.STATE_INITIALIZED) {
                    throw Exception("AudioRecord initialization failed")
                }
                audioRecorder!!.setRecordPositionUpdateListener(updateListener)
                audioRecorder!!.positionNotificationPeriod = framePeriod
                typeConverter = TypeConverter()
            } else { // RECORDING_COMPRESSED
                mediaRecorder = MediaRecorder()
                mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
                mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            }
            cAmplitude = 0
            filePath = null
            state = State.INITIALIZING
        } catch (e: Exception) {
            if (e.message != null) {
                Log.e(ExtAudioRecorder::class.java.name, e.message!!)
            } else {
                Log.e(
                    ExtAudioRecorder::class.java.name,
                    "Unknown error occured while initializing recording"
                )
            }
            state = State.ERROR
        }
    }
}