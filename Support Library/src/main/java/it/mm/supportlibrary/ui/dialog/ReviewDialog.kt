package it.mm.supportlibrary.ui.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import it.mm.supportlibrary.R

class ReviewDialog(
    private val activity: Activity,
    private val useFakeManager: Boolean
) : MaterialAlertDialogBuilder(activity) {

    init {
        setTitle("Ti piace la nostra app?")
        setMessage("Se ti piace, ti preghiamo di lasciarci una recensione sul Play Store!")

        setPositiveButton("Valuta ora") { dialog, _ ->
            requestReview()
            dialog.dismiss()
        }

        setNegativeButton("Forse più tardi") { dialog, _ ->
            dialog.dismiss()
        }
    }

    private fun requestReview() {
        val manager: ReviewManager = if (useFakeManager) {
            FakeReviewManager(activity)
        } else {
            ReviewManagerFactory.create(activity)
        }

        val request = manager.requestReviewFlow()

        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    // Flusso di recensione completato o chiuso
                    showThankYouDialog()
                }
            } else {
                // In caso di errore, apri la pagina del Play Store
                val appPackageName = context.packageName
                val playStoreUrl = "https://play.google.com/store/apps/details?id=$appPackageName"
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)))
            }
        }
    }

    private fun showThankYouDialog() {
        val thankYouDialog = MaterialAlertDialogBuilder(context)
            .setTitle("Grazie mille!")
            .setMessage("Il tuo feedback è prezioso per noi. Continua a goderti l'app!")
            .setIcon(R.drawable.sentiment_very_satisfied) // Aggiungi un'icona personalizzata
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        thankYouDialog.show()
    }
}
