package it.sikuel.k_office.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import it.sikuel.k_office.core.BuildVars.TAG
import it.sikuel.k_office.ui.activity.MainActivity

class PermissionsHelper(val context: Context, val activity: MainActivity) {
    // Manifest.permission.ACCESS_BACKGROUND_LOCATION
    // Manifest.permission.ACCESS_FINE_LOCATION
    // Manifest.permission.BLUETOOTH_CONNECT
    // Manifest.permission.BLUETOOTH_SCAN
    val permissions = ArrayList<String>()

    private fun isPermissionGranted(permissionString: String): Boolean {
        return (ContextCompat.checkSelfPermission(
            context,
            permissionString
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun setFirstTimeAskingPermission(permissionString: String, isFirstTime: Boolean) {
        val sharedPreference = context.getSharedPreferences(
            "org.altbeacon.permisisons",
            AppCompatActivity.MODE_PRIVATE
        )
        sharedPreference.edit().putBoolean(
            permissionString,
            isFirstTime
        ).apply()
    }

    fun isFirstTimeAskingPermission(permissionString: String): Boolean {
        val sharedPreference = context.getSharedPreferences(
            "org.altbeacon.permisisons",
            AppCompatActivity.MODE_PRIVATE
        )
        return sharedPreference.getBoolean(
            permissionString,
            true
        )
    }

    fun beaconScanPermissionGroupsNeeded(backgroundAccessRequested: Boolean = false): List<String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // As of version M (6) we need FINE_LOCATION (or COARSE_LOCATION, but we ask for FINE)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.CAMERA)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // As of version S (12) we need FINE_LOCATION, BLUETOOTH_SCAN and BACKGROUND_LOCATION
            // Manifest.permission.BLUETOOTH_CONNECT is not absolutely required to do just scanning,
            // but it is required if you want to access some info from the scans like the device name
            // and the aditional cost of requsting this access is minimal, so we just request it
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // As of version T (13) we POST_NOTIFICATIONS permissions if using a foreground service
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // As of version Q (10) we need FINE_LOCATION and BACKGROUND_LOCATION
            if (backgroundAccessRequested) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
        return permissions
    }

    private fun allPermissionsGranted(permissionsGroup: List<String>): Boolean {
        for (permission in permissionsGroup) {
            if (!isPermissionGranted(permission)) {
                return false
            }
        }
        return true
    }

    fun promptForPermissions() {
        if (!allPermissionsGranted(permissions)) {
//            val firstPermission = permissionsGroup.first()

//            var showRationale = true
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                showRationale = activity.shouldShowRequestPermissionRationale(firstPermission)
//            }
//            if (showRationale || isFirstTimeAskingPermission(firstPermission)) {
//                setFirstTimeAskingPermission(firstPermission, false)
                requestPermissionsLauncher.launch(permissions.toTypedArray())
//            } else {
//                val builder = AlertDialog.Builder(this)
//                builder.setTitle("Can't request permission")
//                builder.setMessage("This permission has been previously denied to this app.  In order to grant it now, you must go to Android Settings to enable this permission.")
//                builder.setPositiveButton("OK", null)
//                builder.show()
//            }
        }
    }

    val requestPermissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle Permission granted/rejected
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if (isGranted) {
                    Log.d(TAG, "$permissionName permission granted: $isGranted")
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    Log.d(TAG, "$permissionName permission granted: $isGranted")
                    requestPermission(it.key)
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
        }
    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),
            1
        )
    }
}
