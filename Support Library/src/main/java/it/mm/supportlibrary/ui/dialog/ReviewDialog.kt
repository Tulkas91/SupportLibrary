package it.mm.supportlibrary.ui.dialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewDialog(val context: Context) : MaterialAlertDialogBuilder(context) {

    init {
        setTitle("Ti piace la nostra app?")
        setMessage("Se ti piace, ti preghiamo di lasciarci una recensione sul Play Store!")

        // Imposta il bottone per avviare il flusso di recensione
        setPositiveButton("Valuta ora") { dialog, _ ->
            requestReview(context)
            dialog.dismiss()
        }

        // Bottone per chiudere il dialogo senza fare nulla
        setNegativeButton("Forse più tardi") { dialog, _ ->
            dialog.dismiss()
        }
    }

    // Funzione per richiedere la recensione
    private fun requestReview(context: Context) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(context as androidx.fragment.app.FragmentActivity, reviewInfo)
                flow.addOnCompleteListener {
                    // Review completata o chiusa
                }
            } else {
                // In caso di errore, apri direttamente la pagina del Play Store
                val playStoreUrl = "https://play.google.com/store/apps/details?id=TUA_APP_ID"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)))
            }
        }
    }
}
