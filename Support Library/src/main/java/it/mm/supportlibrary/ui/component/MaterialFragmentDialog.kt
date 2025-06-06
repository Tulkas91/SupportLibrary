package it.mm.supportlibrary.ui.component

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import it.mm.supportlibrary.R
import it.mm.supportlibrary.databinding.DialogFragmentLayoutBinding

class MaterialDialogFragment : DialogFragment() {

    private lateinit var binding: DialogFragmentLayoutBinding
    var currentFragment: Fragment? = null
    lateinit var title: String
    lateinit var message: String
    var buttonNeutralVisibility = View.VISIBLE
    var buttonNegativeVisibility = View.GONE
    var buttonPositiveTitle = "CONFERMA"
    var buttonNegativeTitle = "RIPROVA"
    var buttonPositiveClickListener: View.OnClickListener? = null
    var buttonNegativeClickListener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Imposta la dialog come non cancellabile
        isCancelable = false
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.MaterialAlertDialog_Material3)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogFragmentLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Carica il Fragment nel dialog_container
        if (currentFragment != null) {
            childFragmentManager.beginTransaction()
                .replace(binding.dialogContainer.id, currentFragment!!)
                .commit()
        } else {
            binding.line3.visibility = View.GONE
        }

        binding.title.text = title
        binding.message.text = message
        binding.buttonNegative.visibility = buttonNegativeVisibility
        binding.buttonNeutral.visibility = buttonNeutralVisibility

        binding.buttonNeutral.setOnClickListener {
            dismiss()
        }

        binding.buttonNegative.text = buttonNegativeTitle
        if (buttonNegativeClickListener != null)
            binding.buttonNegative.setOnClickListener(buttonNegativeClickListener)
        else {
            binding.buttonNegative.setOnClickListener {
                dismiss()
            }
        }

        binding.buttonPositive.text = buttonPositiveTitle
        if (buttonPositiveClickListener != null)
            binding.buttonPositive.setOnClickListener(buttonPositiveClickListener)
        else {
            binding.buttonPositive.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            // Imposta la larghezza al 100% dello schermo
            val params = window.attributes
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params

            // Imposta uno stile senza margini, opzionale
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
}