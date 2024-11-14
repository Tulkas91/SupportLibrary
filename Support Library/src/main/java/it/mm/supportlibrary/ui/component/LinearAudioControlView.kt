package it.mm.supportlibrary.ui.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isEmpty
import androidx.lifecycle.LifecycleOwner
import it.mm.supportlibrary.R
import it.mm.supportlibrary.core.Utilities
import it.mm.supportlibrary.databinding.LinearAudioControlBinding
import java.util.ArrayList

class LinearAudioControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var audioListPath = ArrayList<String>()
    var parentFilePath = ""
    var count = 0

    // Infla il layout e collega i componenti
    var binding: LinearAudioControlBinding =
        LinearAudioControlBinding.inflate(LayoutInflater.from(context), this, true)

    // Imposta una variabile LifecycleOwner
    private var lifecycleOwner: LifecycleOwner? = null

    // Funzione per impostare il LifecycleOwner
    fun setLifecycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
    }

    init {
        initializeViews()
    }

    private fun initializeViews() {
        count = 0
        audioListPath = ArrayList<String>()
        binding.newAudio.setIconResource(R.drawable.add)
        binding.newAudio.setOnClickListener {
            binding.tvMessage.visibility = View.GONE
            count += 1
            val audioControlView = AudioControlView(context).apply {
                filePath = "$parentFilePath/audio_$count"
                setLifecycleOwner(lifecycleOwner!!)
                buttonDelete.setOnClickListener {
                    binding.audioList.removeView(this)
                    if (isAudioSaved.value!!) Utilities.deleteFile(filePath)
                    if (binding.audioList.isEmpty()) binding.tvMessage.visibility = View.VISIBLE
                }
                isAudioSaved.observe(lifecycleOwner!!) {
                    if (it) {
                        audioListPath.add(filePath)
                    }
                }
            }
            audioControlView.fileName.value = "Audio $count"
            binding.audioList.addView(audioControlView)
        }
    }
}
