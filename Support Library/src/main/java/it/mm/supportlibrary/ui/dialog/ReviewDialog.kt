package it.mm.supportlibrary.ui.dialog

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.review.ReviewManagerFactory

class ReviewDialog(context: Context) : MaterialAlertDialogBuilder(context) {

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
                val flow = manager.launchReviewFlow(context as FragmentActivity, reviewInfo)
                flow.addOnCompleteListener {
                    // Review completata o chiusa
                }
            } else {
                // In caso di errore, apri direttamente la pagina del Play Store

//                        ReviewManager manager = ReviewManagerFactory.create(context);
////        ReviewManager manager = new FakeReviewManager(clientActivity);
//                        Task<ReviewInfo> request = manager.requestReviewFlow();
//                        request.addOnCompleteListener(google_task -> {
//                            if (google_task.isSuccessful()) {
//                                // We can get the ReviewInfo object
//                                ReviewInfo reviewInfo = google_task.getResult();
//                                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
//                                flow.addOnCompleteListener(task -> {
//                                    // The flow has finished. The API does not indicate whether the user
//                                    // reviewed or not, or even whether the review dialog was shown. Thus, no
//                                    // matter the result, we continue our app flow.
//                                });
//                            } else {
//                                // There was some problem, continue regardless of the result.
//                                // you can show your own rate dialog alert and redirect user to your app page
//                                // on play store.
//                            }
//                        });
                val appPackageName = context.packageName
                val playStoreUrl = "https://play.google.com/store/apps/details?id=$appPackageName"
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)))
            }
        }
    }
}
