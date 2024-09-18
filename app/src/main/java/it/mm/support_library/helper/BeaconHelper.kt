package it.mm.support_library.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.Observer
import it.mm.support_library.Application
import it.mm.support_library.R
import it.mm.support_library.core.NotificationCenter
import it.mm.support_library.core.time.FastDateFormat
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.MonitorNotifier
import org.altbeacon.beacon.Region
import java.util.Date
import java.util.Locale

class BeaconHelper(val context: Context) {

    private var beaconManager: BeaconManager? = null
    var region = Region("all-beacons", null, null, null)

    fun beaconStart() {
        val beaconManager = BeaconManager.getInstanceForApplication(context)
        BeaconManager.setDebug(true)

        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        //beaconManager.getBeaconParsers().clear();
        //beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:0-1=4c00,i:2-24v,p:24-24"));


        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon like Eddystone or iBeacon, you must specify the byte layout
        // for that beacon's advertisement with a line like below.
        //
        // If you don't care about AltBeacon, you can clear it from the defaults:
        //beaconManager.getBeaconParsers().clear()

        // Uncomment if you want to block the library from updating its distance model database
        //BeaconManager.setDistanceModelUpdateUrl("")

        // The example shows how to find iBeacon.
        val parser = BeaconParser().
        setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24")
        parser.setHardwareAssistManufacturerCodes(arrayOf(0x004c).toIntArray())
        beaconManager.getBeaconParsers().add(parser)

        // enabling debugging will send lots of verbose debug information from the library to Logcat
        // this is useful for troubleshooting problmes
        // BeaconManager.setDebug(true)


        // The BluetoothMedic code here, if included, will watch for problems with the bluetooth
        // stack and optionally:
        // - power cycle bluetooth to recover on bluetooth problems
        // - periodically do a proactive scan or transmission to verify the bluetooth stack is OK
        // BluetoothMedic.getInstance().legacyEnablePowerCycleOnFailures(this) // Android 4-12 only
        // BluetoothMedic.getInstance().enablePeriodicTests(this, BluetoothMedic.SCAN_TEST + BluetoothMedic.TRANSMIT_TEST)
        setUpBeaconRegion()
        setUpBeaconScanning()
    }

    fun beaconEnd() {
        beaconManager!!.stopMonitoring(region)
        beaconManager!!.stopRangingBeacons(region)
    }

    private fun setUpBeaconRegion() {
        val identifiers: MutableList<Identifier> = ArrayList()
        for (i in getInstance()!!.beaconDevices!!.indices) {
            identifiers.add(Identifier.parse(getInstance()!!.beaconDevices!![i].code))
        }
        region = Region("all-beacons", identifiers)
    }

    private fun setUpBeaconScanning() {
        beaconManager = BeaconManager.getInstanceForApplication(context)

        // By default, the library will scan in the background every 5 minutes on Android 4-7,
        // which will be limited to scan jobs scheduled every ~15 minutes on Android 8+
        // If you want more frequent scanning (requires a foreground service on Android 8+),
        // configure that here.
        // If you want to continuously range beacons in the background more often than every 15 mintues,
        // you can use the library's built-in foreground service to unlock this behavior on Android
        // 8+.   the method below shows how you set that up.
        try {
//            setUpForegroundService()
        }
        catch (e: SecurityException) {
            // On Android TIRAMUSU + this security exception will happen
            // if location permission has not been granted when we start
            // a foreground service.  In this case, wait to set this up
            // until after that permission is granted
            Log.d(Application.TAG, "Not setting up foreground service scanning until location permission granted by user")
            return
        }
        //beaconManager.setEnableScheduledScanJobs(false);
        beaconManager!!.setBackgroundBetweenScanPeriod(20000);
        beaconManager!!.setBackgroundScanPeriod(3000);

        // Ranging callbacks will drop out if no beacons are detected
        // Monitoring callbacks will be delayed by up to 25 minutes on region exit
        // beaconManager.setIntentScanningStrategyEnabled(true)

        // The code below will start "monitoring" for beacons matching the region definition at the top of this file
        beaconManager!!.startMonitoring(region)
        beaconManager!!.startRangingBeacons(region)
        // These two lines set up a Live Data observer so this Activity can get beacon data from the Application class
        val regionViewModel = BeaconManager.getInstanceForApplication(context).getRegionViewModel(region)
        // observer will be called each time the monitored regionState changes (inside vs. outside region)
        regionViewModel.regionState.observeForever( centralMonitoringObserver)
        // observer will be called each time a new list of beacons is ranged (typically ~1 second in the foreground)
        regionViewModel.rangedBeacons.observeForever( centralRangingObserver)

    }

    private fun setUpForegroundService() {
        val builder = Notification.Builder(context, "K-HelpApp")
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
        builder.setContentTitle("K-Help Application")
        builder.setContentText("Scanning for Beacons")
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent);
        val channel =  NotificationChannel("k-help-beacon-ref-notification-id",
            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setDescription("My Notification Channel Description")
        val notificationManager =  context.getSystemService(
            Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel);
        builder.setChannelId(channel.getId());
//        Log.d(TAG, "Calling enableForegroundServiceScanning")
        BeaconManager.getInstanceForApplication(context).enableForegroundServiceScanning(builder.build(), 456);
//        Log.d(TAG, "Back from  enableForegroundServiceScanning")
    }

    private val centralMonitoringObserver = Observer<Int> { state ->
        val formatDate = FastDateFormat.getInstance("dd/MM/yyyy", Locale.ITALY)
        val formatHour = FastDateFormat.getInstance("HH:mm", Locale.ITALY)
        if (state == MonitorNotifier.OUTSIDE) {
//            Log.d(TAG, "outside beacon region: "+region)
            val date = Date()
//            sendNotification(formatDate.format(date) + " ore " + formatHour.format(date) + "\nSei uscito dall'ufficio", "beacon-ref-notification-id-out")
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.enableButtonManagerClock, false, R.color.md_grey_400)
        } else {
//            Log.d(TAG, "inside beacon region: "+region)
            val date = Date()
//            sendNotification(formatDate.format(date) + " ore " + formatHour.format(date) + "\nSei entrato in ufficio", "beacon-ref-notification-id-in")
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.enableButtonManagerClock, true, R.color.black)
        }
    }

    private val centralRangingObserver = Observer<Collection<Beacon>> { beacons ->
        val beacon = (beacons.sortedBy { it.distance }).firstOrNull()
        if (beacon != null)
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.setBeaconOffice, beacon)
    }

//    private val centralRangingObserver = Observer<Collection<Beacon>> { beacons ->
//        val rangeAgeMillis = System.currentTimeMillis() - (beacons.firstOrNull()?.lastCycleDetectionTimestamp ?: 0)
//        if (rangeAgeMillis < 10000) {
////            Log.d(TAG, "Ranged: ${beacons.count()} beacons")
//            for (beacon: Beacon in beacons) {
////                Log.d(TAG, "$beacon about ${beacon.distance} meters away")
//            }
//        }
//        else {
////            Log.d(TAG, "Ignoring stale ranged beacons from $rangeAgeMillis millis ago")
//        }
//    }

    private fun sendNotification(text: String, channelId: String) {
        val stackBuilder = TaskStackBuilder.create(context)
//        stackBuilder.addNextIntent(Intent(this, HomeActivity::class.java))
        stackBuilder.addNextIntent(Intent(context, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
        )
        Notify.build(context)
            .setAutoCancel(true)
            .setTitle("K-Help Application")
            .setContent(text)
            .setChannelId(channelId)
            .setChannelName("K-Help Notification Channel")
            .setSmallIcon(R.drawable.notifications)
            .setLargeIcon(R.mipmap.ic_launcher_round)
            .setColor(R.color.colorPrimary)
            .setPendingIntent(resultPendingIntent)
            .show()
        //                .setLargeIcon("https://images.pexels.com/photos/139829/pexels-photo-139829.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=150&w=440")
        //                .largeCircularIcon()
        //                .setPicture("https://images.pexels.com/photos/1058683/pexels-photo-1058683.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940")
        //                .setPicture(R.drawable.sikuel)
    }

//    private fun sendNotification(text: String, channelId: String) {
//        val builder = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("K-Help Application")
//            .setContentText(text)
//            .setSmallIcon(R.mipmap.ic_launcher_round)
//        val stackBuilder = TaskStackBuilder.create(this)
////        stackBuilder.addNextIntent(Intent(this, HomeActivity::class.java))
//        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
//        val resultPendingIntent = stackBuilder.getPendingIntent(
//            0,
//            PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE
//        )
////        builder.setContentIntent(resultPendingIntent)
//        builder.setFullScreenIntent(resultPendingIntent, true)
//        val channel =  NotificationChannel(channelId,
//            "My Notification Name", NotificationManager.IMPORTANCE_DEFAULT)
//        channel.setDescription("My Notification Channel Description")
//        val notificationManager =  getSystemService(
//            Context.NOTIFICATION_SERVICE) as NotificationManager
//        notificationManager.createNotificationChannel(channel);
//        builder.setChannelId(channel.getId());
//        notificationManager.notify(1, builder.build())
//    }
}
