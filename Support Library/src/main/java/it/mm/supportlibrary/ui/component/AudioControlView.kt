package it.mm.supportlibrary.ui.component

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import it.mm.supportlibrary.R
import java.io.IOException

class AudioControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    var fileName = ""

    private var isRecording = false
    private var isPlaying = false

    private lateinit var buttonRecord: ImageButton
    private lateinit var buttonPlay: ImageButton
    private lateinit var buttonPause: ImageButton
    private lateinit var buttonStop: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var tvCurrentTime: TextView

    init {
        // Infla il layout e collega i componenti
        LayoutInflater.from(context).inflate(R.layout.view_audio_control, this, true)
        initializeViews()
    }

    private fun initializeViews() {
        buttonRecord = findViewById(R.id.button_record)
        buttonPlay = findViewById(R.id.button_play)
        buttonPause = findViewById(R.id.button_pause)
        buttonStop = findViewById(R.id.button_stop)
        seekBar = findViewById(R.id.seek_bar)
        tvCurrentTime = findViewById(R.id.tv_current_time)

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

        buttonPause.setOnClickListener {
            pauseAudio()
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
            buttonRecord.setImageResource(R.drawable.rec)
            buttonPlay.isEnabled = true
            buttonStop.isEnabled = false
            isRecording = false
        } else {
            // Inizia la registrazione
            startRecording()
            buttonRecord.setImageResource(R.drawable.stop_circle)
            buttonPlay.isEnabled = false
            buttonStop.isEnabled = false
            isRecording = true
        }
    }

    @Throws(IOException::class)
    private fun startRecording() {
        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setOutputFile(fileName)
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
            mediaPlayer!!.setDataSource(fileName)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()

            seekBar.max = mediaPlayer!!.duration
            updateSeekBar()

            buttonPlay.setImageResource(R.drawable.pause_circle)
            buttonPause.isEnabled = true
            buttonStop.isEnabled = true
            buttonRecord.isEnabled = false
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
                buttonPlay.setImageResource(R.drawable.play_circle)
                isPlaying = false
            } else {
                mediaPlayer!!.start()
                buttonPlay.setImageResource(R.drawable.pause_circle)
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
        buttonPlay.setImageResource(R.drawable.play_circle)
        buttonPlay.isEnabled = true
        buttonPause.isEnabled = false
        buttonStop.isEnabled = false
        buttonRecord.isEnabled = true
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
