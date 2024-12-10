package it.mm.supportlibrary.ui.component.photo

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import it.mm.supportlibrary.core.FileLog
import it.mm.supportlibrary.core.Utilities
import it.mm.supportlibrary.databinding.ViewPhotoControlBinding
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.let

class PhotoControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var filePath = ""
    var fileName = MutableLiveData<String>()
    var isPhotoSaved = MutableLiveData<Boolean>()
    var provider = ""

    private var internalFragment: InternalPhotoFragment? = null

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
                binding.tvFileName.text = updateText
            })
        }
    }

    var binding: ViewPhotoControlBinding =
        ViewPhotoControlBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initializeViews()
        setupInternalFragment()
    }

    private fun setupInternalFragment() {
        val fragmentActivity = findFragmentActivity(context)
        if (fragmentActivity != null) {
            val fragmentManager = fragmentActivity.supportFragmentManager
            var fragment =
                fragmentManager.findFragmentByTag("InternalPhotoFragment") as? InternalPhotoFragment
            if (fragment == null) {
                fragment = InternalPhotoFragment()
                fragment.providerString = provider
                fragmentManager.beginTransaction()
                    .add(fragment, "InternalPhotoFragment")
                    .commitNow()
            }
            internalFragment = fragment
        } else {
            throw IllegalStateException("Context non è un'istanza di FragmentActivity")
        }
    }

    private fun findFragmentActivity(context: Context): FragmentActivity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is FragmentActivity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    private fun initializeViews() {
        isPhotoSaved.value = false
        binding.ivPhoto.setOnClickListener {
            internalFragment?.takePicture { bitmap ->
                if (bitmap != null) {
                    addPhoto(bitmap)
                } else {
                    showToast("Errore durante lo scatto della foto")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).apply {
            setGravity(Gravity.CENTER, 0, 0)
            show()
        }
    }

    private fun addPhoto(bitmap: Bitmap) {
        binding.ivPhoto.setImageBitmap(bitmap)
        binding.ivPhoto.scaleType = ImageView.ScaleType.CENTER_CROP

        if (savePhoto(bitmap)) {
            isPhotoSaved.value = true
        }
    }

    fun savePhoto(bitmap: Bitmap): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveBitmap(bitmap)
        } else {
            writePhotoReading(bitmap)
        }
    }

    fun saveBitmap(bitmap: Bitmap): Boolean {
        var values = ContentValues()
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName.toString())
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/")

        val resolver = context.contentResolver

        var uri: Uri?
        try {
            var contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values)

            if (uri == null)
                throw IOException("Failed to create new MediaStore record.");

            var stream = resolver.openOutputStream(uri)
            if (stream == null)
                throw IOException("Failed to open output stream.");

//                Bitmap bitmap = bitmaps.get(num - 1);
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream))
                throw IOException("Failed to save bitmap.");


            Utilities.execTerminalCommand("cp /storage/emulated/0/Pictures/$fileName $filePath\n")
            Utilities.execTerminalCommand("rm /storage/emulated/0/Pictures/$fileName\n")

            return true
        } catch (e : IOException) {
            // Don't leave an orphan entry in the MediaStore
//            resolver.delete(uri, null, null)
            return false
        }
    }

    fun writePhotoReading(bitmap: Bitmap) : Boolean {
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val inputStream = ByteArrayInputStream(stream.toByteArray())
        lateinit var photoFile: File
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
            photoFile = File(filePath)
            try {
                val outStream = FileOutputStream(photoFile);
                inputStream.use { input ->
                    outStream.use { output ->
                        input.copyTo(output, bufferSize = 1024)
                    }
                }

                outStream.flush();
                outStream.close();
            } catch (e: FileNotFoundException ) {
                FileLog.e(e)
                return false
            } catch (e: IOException) {
                FileLog.e(e)
                return false
            }

        }
        return true;
    }

    interface PhotoControlListener {
        fun onRequestTakePhoto()
    }

}
