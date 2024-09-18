package it.mm.support_library.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.sikuel.k_office.R
import it.sikuel.k_office.core.BuildVars.TAG

class GPSHelper(val context: Context, val activity: Activity) {

    private var mSettingsClient: SettingsClient? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var locationManager: LocationManager? = null
    private var locationRequest: LocationRequest? = null
    var gpsActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mSettingsClient = LocationServices.getSettingsClient(context)

        locationRequest = LocationRequest.create()
        locationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest!!.setInterval(10 * 1000)
        locationRequest!!.setFastestInterval(2 * 1000)
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
            .setNeedBle(true)
        mLocationSettingsRequest = builder.build()

        //**************************
        builder.setAlwaysShow(true) //this is the key ingredient
        //**************************
    }

    // method for turn on GPS
    fun turnGPSOn(onGpsListener: onGpsListener) {
        if (locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            onGpsListener.gpsStatus(true)
        } else {
            mSettingsClient!!
                .checkLocationSettings(mLocationSettingsRequest!!)
                .addOnSuccessListener(
                    activity,
                    object : OnSuccessListener<LocationSettingsResponse?> {
                        @SuppressLint("MissingPermission")
                        override fun onSuccess(locationSettingsResponse: LocationSettingsResponse?) {
                            //  GPS is already enable, callback GPS status through listener
                            onGpsListener.gpsStatus(true)
                        }
                    })
                .addOnFailureListener(context as Activity, object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        val statusCode: Int = (e as ApiException).getStatusCode()
                        when (statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                val rae: ResolvableApiException = e as ResolvableApiException
                                rae.startResolutionForResult(
                                    context as Activity,
                                    1001
                                )
                            } catch (sie: SendIntentException) {
                                Log.i(TAG, "PendingIntent unable to execute request.")
                            }

                            LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                                val errorMessage =
                                    "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings."
                                Log.e(TAG, errorMessage)

                                Toast.makeText(context as Activity, errorMessage, Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                })
        }
    }

    fun interface onGpsListener {
        fun gpsStatus(isGPSEnable: Boolean)
    }
}
