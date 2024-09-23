package it.mm.support_library

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import it.mm.support_library.core.BuildVars
import it.mm.support_library.core.FileLog
import it.mm.support_library.volley.RequestManager
import kotlin.concurrent.Volatile

/**
 * Created by Dott. Marco Mezzasalma on 18/09/2024.
 */
class Application : Application(), ActivityLifecycleCallbacks {
    private var prefs: SharedPreferences? = null
    private var inForeground = false

    private var androidDefaultUEH: Thread.UncaughtExceptionHandler? = null
    private val handler =
        Thread.UncaughtExceptionHandler { thread, ex ->
            FileLog.e(BuildVars.TAG, ex)
            androidDefaultUEH!!.uncaughtException(thread, ex)
        }

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

        prefs = getSharedPreferences("it.mm.support_library_preferences", Context.MODE_PRIVATE)
        val filesPath = prefs!!.getString("files_path", "")
        if (filesPath.isNullOrEmpty()) prefs!!.edit().putString("files_path", "/storage/emulated/0/Documents/").commit()

    }

    private fun setAppContext(mAppContext: Context?) {
        Companion.mAppContext = mAppContext
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
        const val TAG = "Support Library"
        var mInstance: Application? = null
            private set

        @Volatile
        private var mAppContext: Context? = null

        @Volatile
        var applicationHandler: Handler? = null
            private set

        var prefs: SharedPreferences? = null

        var loadingDistricts: Boolean = false
        var mMediaPlayer: MediaPlayer? = null

        fun getAppContext(): Context? {
            return mAppContext
        }

    }
}