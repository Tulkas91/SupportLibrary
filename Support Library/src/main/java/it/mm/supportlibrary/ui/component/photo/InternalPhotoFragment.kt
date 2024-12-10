package it.mm.supportlibrary.ui.component.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import it.mm.supportlibrary.core.FileLog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.also
import kotlin.apply
import kotlin.jvm.Throws

class InternalPhotoFragment : Fragment() {

    lateinit var providerString: String
    private var takePictureLauncher: ActivityResultLauncher<Uri>? = null
    private var mCurrentPhotoFile: File? = null
    private var callback: ((Bitmap?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerTakePictureLauncher()
    }

    private fun registerTakePictureLauncher() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && mCurrentPhotoFile != null && mCurrentPhotoFile!!.exists()) {
                val bitmap = BitmapFactory.decodeFile(mCurrentPhotoFile!!.absolutePath)
                callback?.invoke(bitmap)
            } else {
                callback?.invoke(null)
            }
            mCurrentPhotoFile = null
        }
    }

    fun takePicture(callback: (Bitmap?) -> Unit) {
        this.callback = callback
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            FileLog.e("Errore durante la creazione del file", ex)
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                providerString,
                it
            )
            takePictureLauncher?.launch(photoURI)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            mCurrentPhotoFile = this
        }
    }
}
