package it.mm.supportlibrary.ui.component

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButton
import it.mm.supportlibrary.R
import java.io.IOException

class AudioControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    var filePath = ""
    var fileName = MutableLiveData<String>()

    private var isRecording = false
    private var isPlaying = false

    private lateinit var tvFileName: TextView
    lateinit var buttonDelete: MaterialButton
    private lateinit var buttonRecord: MaterialButton
    private lateinit var buttonPlay: MaterialButton
    private lateinit var buttonStop: MaterialButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView

    // Imposta una variabile LifecycleOwner
    private var lifecycleOwner: LifecycleOwner? = null

    // Funzione per impostare il LifecycleOwner
    fun setLifecycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
        observeFileName()
    }

    private fun observeFileName() {
        lifecycleOwner?.let { owner ->
            fileName.observe(owner, Observer { updateText ->
                tvFileName.text = updateText
            })
        }
    }

    init {
        // Infla il layout e collega i componenti
        LayoutInflater.from(context).inflate(R.layout.view_audio_control, this, true)
        initializeViews()
    }

    private fun initializeViews() {
        tvFileName = findViewById(R.id.file_name)
        buttonDelete = findViewById(R.id.button_delete)
        buttonRecord = findViewById(R.id.button_record)
        buttonPlay = findViewById(R.id.button_play)
        buttonStop = findViewById(R.id.button_stop)
        seekBar = findViewById(R.id.seek_bar)
        tvCurrentTime = findViewById(R.id.tv_current_time)
        tvTotalTime = findViewById(R.id.tv_total_time)

        buttonPlay.isVisible = false
        buttonStop.isVisible = false

        // Aggiungi i listener per i pulsanti
        buttonRecord.setOnClickListener {
            try {
                recordAudio()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        buttonPlay.setOnClickListener {
            try {
                playAudio()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        buttonStop.setOnClickListener {
            stopAudio()
        }

        // Configura la SeekBar
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer!!.seekTo(progress)
                    tvCurrentTime.text = formatTime(mediaPlayer!!.currentPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Non necessario
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Non necessario
            }
        })
    }

    // Metodo per aggiornare la SeekBar
    fun updateSeekBar(progress: Int) {
        seekBar.progress = progress
    }

    @Throws(IOException::class)
    private fun recordAudio() {
        if (isRecording) {
            // Ferma la registrazione
            stopRecording()
            buttonRecord.icon = ContextCompat.getDrawable(context, R.drawable.rec)
            buttonPlay.isVisible = true
            buttonStop.isVisible = true
            isRecording = false
        } else {
            // Inizia la registrazione
            startRecording()
            buttonRecord.icon = ContextCompat.getDrawable(context, R.drawable.stop_circle)
            buttonPlay.isVisible = false
            buttonStop.isVisible = false
            isRecording = true
        }
    }

    @Throws(IOException::class)
    private fun startRecording() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setOutputFile(filePath)
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
    }

    private fun stopRecording() {
        mediaRecorder!!.stop()
        mediaRecorder!!.release()
        mediaRecorder = null
    }

    @Throws(IOException::class)
    private fun playAudio() {
        if (!isPlaying) {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setDataSource(filePath)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()

            seekBar.max = mediaPlayer!!.duration
            updateSeekBar()

            buttonPlay.icon = ContextCompat.getDrawable(context, R.drawable.pause_circle)
            isPlaying = true

            mediaPlayer!!.setOnCompletionListener { resetPlayer() }
        } else {
            pauseAudio()
        }
    }

    private fun pauseAudio() {
        if (mediaPlayer != null) {
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.pause()
                buttonPlay.icon = ContextCompat.getDrawable(context, R.drawable.play_circle)
                isPlaying = false
            } else {
                mediaPlayer!!.start()
                buttonPlay.icon = ContextCompat.getDrawable(context, R.drawable.pause_circle)
                isPlaying = true
            }
        }
    }

    private fun stopAudio() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            resetPlayer()
        }
    }

    private fun resetPlayer() {
        buttonPlay.icon = ContextCompat.getDrawable(context, R.drawable.play_circle)
        isPlaying = false

        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }

        handler.removeCallbacks(updateSeekBarRunnable)
        seekBar.progress = 0
        tvCurrentTime.text = "00:00"
    }

    private fun updateSeekBar() {
        handler.postDelayed(updateSeekBarRunnable, 0)
    }

    private val updateSeekBarRunnable: Runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer != null) {
                val currentPosition = mediaPlayer!!.currentPosition
                seekBar.progress = currentPosition
                tvCurrentTime.text = formatTime(currentPosition)
                tvTotalTime.text = formatTime(seekBar.max)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60

        val minutesStr = String.format("%02d", minutes)
        val secondsStr = String.format("%02d", seconds)

        return "$minutesStr:$secondsStr"
    }
}
