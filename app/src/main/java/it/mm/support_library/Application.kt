package it.mm.support_library

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.github.pwittchen.prefser.library.Prefser
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region
import java.util.Observable
import kotlin.concurrent.Volatile

/**
 * Created by Dott. Marco Mezzasalma on 18/09/2024.
 */
class Application : Application(), java.util.Observer, ActivityLifecycleCallbacks {
    private var prefser: Prefser? = null
    private var prefs: SharedPreferences? = null
    private var inForeground = false
    private var host = ""
    private var beaconManager: BeaconManager? = null

    private var account: Account? = null

    private var androidDefaultUEH: Thread.UncaughtExceptionHandler? = null
    private val handler =
        Thread.UncaughtExceptionHandler { thread, ex ->
            FileLog.e(BuildVars.TAG, ex)
            androidDefaultUEH!!.uncaughtException(thread, ex)
        }

    var beaconDevices: List<BeaconDevice>? = null
    var region =
        Region("all-beacons", Identifier.parse("acfd065e-c3c0-11e3-9bbe-1a514932ac01"), null, null)

    override fun onCreate() {
        super.onCreate()

        registerActivityLifecycleCallbacks(this)

        androidDefaultUEH = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(handler)

        mInstance = this

        this.setAppContext(applicationContext)
        applicationHandler = Handler(mAppContext!!.mainLooper)

        System.setProperty("http.keepAlive", "false")

        RequestManager.initializeWith(applicationContext)
        RequestManager.queue().useBackgroundQueue().start()

        prefser = Prefser(getAppContext()!!, GsonConverter())
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        account = Account()

        //BuildVars.LOCAL_SERVER_RUN = prefser.getPreferences().getBoolean(PrefUtils.PREF_LOCAL_SERVER_RUN, false);
        val manager = prefser!!.get("Manager", Manager::class.java, Manager())
        account!!.setCurrentManager(manager)

//        account!!.addObserver(this)
        account!!.currentManager.observeForever(accountObserver)

        mMediaPlayer = MediaPlayer() //   Initialize sound
        mMediaPlayer = MediaPlayer.create(mAppContext, R.raw.chimes)
        mMediaPlayer!!.setLooping(false)
    }

    private val accountObserver = Observer<Manager> { manager ->
        //save object
        Utilities.globalQueue.postRunnable {
            prefser!!.put(
                "Manager",
                manager
            )
        }

    }

    fun setAppContext(mAppContext: Context?) {
        Companion.mAppContext = mAppContext
    }

    fun getPrefser(): Prefser? {
        return prefser
    }

    fun getPrefs(): SharedPreferences? {
        return prefs
    }

    fun getAccount(): Account? {
        return account
    }

    fun logout() {
        prefser!!.put("not_authorized", true)
        if (account!!.currentManager != null) account!!.logout()
    }

    fun setServerHost(host: String) {
        this.host = "https://$host"
    }

    fun getServerHost(): String {
        if (BuildVars.LOCAL_SERVER_RUN) {
            return BuildVars.LOCAL_SERVER_URL
        }
        return host
    }

    override fun update(observable: Observable, data: Any) {
        val manager = (observable as Account).currentManager

        if (manager != null) {
            //save object
            Utilities.globalQueue.postRunnable {
                prefser!!.put(
                    "Manager",
                    manager
                )
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        inForeground = activity is AppCompatActivity
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.moveTaskToFront)
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
//        beaconManager!!.stopMonitoring(region)
//        beaconManager!!.stopRangingBeacons(region)
    }

    fun isInForeground(): Boolean {
        return inForeground
    }

    companion object {
        val TAG = "K-Help"
        var mInstance: Application? = null
            private set

        @Volatile
        private var mAppContext: Context? = null

        @Volatile
        var applicationHandler: Handler? = null
            private set

        var loadingDistricts: Boolean = false
        var mMediaPlayer: MediaPlayer? = null

        fun getInstance(): Application? {
            return mInstance
        }

        fun getAppContext(): Context? {
            return mAppContext
        }

        fun getServerUrl(): String {
            if (BuildVars.LOCAL_SERVER_RUN) return BuildVars.LOCAL_SERVER_URL
            return BuildVars.SERVER_URL
        }
    }
}