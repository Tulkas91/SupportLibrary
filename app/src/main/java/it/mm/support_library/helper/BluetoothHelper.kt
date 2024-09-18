package it.mm.support_library.helper

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.mm.support_library.R

class BluetoothHelper(val context: Context, val activity: Activity) {

    var bluetoothManager: BluetoothManager = activity.getSystemService<BluetoothManager>(
        BluetoothManager::class.java
    )
    var bluetoothAdapter: BluetoothAdapter? = null

    var bluetoothActivityResultLauncher: ActivityResultLauncher<Intent>? = null

    fun bluetoothIsSupported(): Boolean {
        bluetoothAdapter = bluetoothManager.adapter
        return bluetoothAdapter == null
    }

    fun bluetoothIsEnabled(): Boolean {
        return bluetoothAdapter!!.isEnabled
    }

    fun registerActionStateChange(mReceiver :BroadcastReceiver) {
        var filter: IntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        activity.registerReceiver(mReceiver, filter)
    }

    fun messageBluetoothDisableOrNotSupported() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("ATTENZIONE")
            .setMessage("Il tuo dispositivo non supporta il Bluetooth, oppure non hai acconsentito all'accensione del Bluetooth e non puoi usare questa app. L'app verrà chiusa!")
            .setCancelable(false)
            .setIcon(ContextCompat.getDrawable(activity, R.drawable.bluetooth_disabled))
            .setPositiveButton("CHIUDI") { dialogInterface, i ->
                dialogInterface.dismiss()
                activity.finish()
            }
            .create().show()
//        val materialAlertDialog = MyMaterialAlertDialog(
//            activity,
//            false,
//            View.INVISIBLE,
//            View.INVISIBLE,
//            "ESCI",
//            false
//        )
//        materialAlertDialog.title.text = ""
//        materialAlertDialog.message.text =
//            "Il tuo dispositivo non supporta il Bluetooth, oppure non hai acconsentito all'accensione del Bluetooth e non puoi usare questa app. L'app verrà chiusa!"
//        materialAlertDialog.show()
//        materialAlertDialog.setPositiveButtonListener { activity.finish() }
    }

}
