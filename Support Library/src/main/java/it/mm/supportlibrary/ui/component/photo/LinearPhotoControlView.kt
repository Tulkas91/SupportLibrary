package it.mm.supportlibrary.ui.component.photo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isEmpty
import androidx.lifecycle.LifecycleOwner
import it.mm.supportlibrary.core.Utilities
import it.mm.supportlibrary.databinding.LinearPhotoControlBinding
import it.mm.supportlibrary.R
import java.util.ArrayList
import kotlin.apply

class LinearPhotoControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var photoListPath = ArrayList<String>()
    var parentFilePath = ""
    var count = 0
    var providerString = ""

    // Infla il layout e collega i componenti
    var linearBinding: LinearPhotoControlBinding =
        LinearPhotoControlBinding.inflate(LayoutInflater.from(context), this, true)

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
        photoListPath = ArrayList<String>()
        linearBinding.newPhoto.setIconResource(R.drawable.add_a_photo)
        linearBinding.newPhoto.setIconTintResource(R.color.colorPrimaryStrong)
        linearBinding.newPhoto.setOnClickListener {
            linearBinding.tvMessage.visibility = GONE
            linearBinding.photoList.visibility = VISIBLE
            count += 1
            val photoControlView = PhotoControlView(context).apply {
                this.provider = providerString
                filePath = "$parentFilePath/photo_$count.jpeg"
                setLifecycleOwner(lifecycleOwner!!)
                binding.buttonDelete.setOnClickListener {
                    linearBinding.photoList.removeView(this)
                    if (isPhotoSaved.value!!) {
                        Utilities.deleteFile(filePath)
                    }
                    if (linearBinding.photoList.isEmpty()) linearBinding.tvMessage.visibility = VISIBLE
                }
                isPhotoSaved.observe(lifecycleOwner!!) {
                    if (it) {
                        photoListPath.add(filePath)
                    }
                }
            }
            photoControlView.fileName.value = "Foto$count"
            linearBinding.photoList.addView(photoControlView)
            linearBinding.horizontalScrollView.post {
                linearBinding.horizontalScrollView.fullScroll(FOCUS_RIGHT)
            }
        }
    }
}
