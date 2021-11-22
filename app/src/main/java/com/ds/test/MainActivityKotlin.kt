package com.ds.test

import android.Manifest

import androidx.appcompat.app.AppCompatActivity
import com.drivesmartsdk.interfaces.DSManagerInterface
import com.drivesmartsdk.singleton.DSManager
import com.drivesmartsdk.DSDKUserSession
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.content.Intent
import android.os.Handler
import android.util.Log
import com.drivesmartsdk.enums.DSNotification
import com.drivesmartsdk.enums.DSInternalMotionActivities
import com.drivesmartsdk.enums.DSMotionEvents
import com.drivesmartsdk.enums.DSResult
import com.drivesmartsdk.singleton.DSTrackerLite
import com.ds.test.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivityKotlin: AppCompatActivity(), DSManagerInterface{
    private lateinit var binding: ActivityMainBinding
    private lateinit var dsTrackerLite: DSTrackerLite
    private lateinit var apkID: String
    private lateinit var userID: String
    private lateinit var handlerTrip: Handler
    private var userSession: String? = null

    companion object {
        val PERMISSIONS_GPS = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val PERMISSIONS_GPS_AUTO = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    private fun defineConstants() {
        apkID = "7F87144D-D621-4056-B981-5A15AA6228DD-2-26"
        userID = "10432929d379826"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        defineConstants()
        prepareEnvironment()
        prepareView()


    }

    private fun prepareView() {
        handlerTrip = Handler(mainLooper)
        binding.logText.movementMethod = ScrollingMovementMethod()
        checkPerms()
        binding.checkPermButton.setOnClickListener {
            dsTrackerLite.upload()
            //checkPerms()
        }
        binding.startTripButton.setOnClickListener {
            LiteService.startService(this, "Foreground Service Example in Android", "PMD")
        }
        binding.stopTripButton.setOnClickListener {
            handlerTrip.removeCallbacksAndMessages(updateTimerThread)
            LiteService.stopService(this, applicationContext)
        }
        binding.setUserButton.setOnClickListener {
            if (userSession != null) {
                identifyEnvironmet(userSession!!)
            } else {
                addLog("no user-session info")
            }
        }
        binding.getUserButton.setOnClickListener {
            if (binding.userId.text.toString().isNotEmpty()) {
                getOrAddUser(binding.userId.text.toString())
            }
        }
    }

    private fun checkPerms() {
        if (UtilsMethods.isDozing(this)) {
            binding.permBatteryStatus.text = getString(R.string.optimized)
        } else {
            binding.permBatteryStatus.text = getString(R.string.no_optimized)
        }
        if (UtilsMethods.isGPSEnabled(this)) {
            binding.permGpsStatus.text = getString(R.string.enabled)
        } else {
            binding.permGpsStatus.text = getString(R.string.disabled)
        }
        if (UtilsMethods.checkPermissions(this, PERMISSIONS_GPS)) {
            binding.permGpsSemiStatus.text = getString(R.string.ok)
        } else {
            binding.permGpsSemiStatus.text = getString(R.string.nok)
        }
        if (UtilsMethods.checkPermissions(this, PERMISSIONS_GPS_AUTO)) {
            binding.permGpsAutoStatus.text = getString(R.string.ok)
        } else {
            binding.permGpsAutoStatus.text = getString(R.string.nok)
        }
    }

    private suspend fun  getUserSession(user: String): String =
        withContext(Dispatchers.IO) {
            return@withContext  dsTrackerLite.getOrAddUserIdBy(user)
        }

    private fun getOrAddUser(user: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val session = getUserSession(user)

            userSession = session
            addLog("User id created: $session")
            }
    }

    private fun prepareEnvironment() {
        dsTrackerLite = DSTrackerLite.getInstance(this)
        dsTrackerLite.setListener(this)
        dsTrackerLite.configure(apkID) { dsResult: DSResult ->
            if (dsResult is DSResult.Success) {
                addLog("DSTracker configured")
                identifyEnvironmet(userID)
            } else {
                val error: String = dsResult.toString()
                addLog("Configure DSTracker: $error")
            }
        }
    }

    private fun identifyEnvironmet(uid: String) {
        dsTrackerLite.setUserId(uid) {
            addLog("Defining USER ID: $uid")
        }
    }

    // ****************************************v****************************************
    // ******************************* Client Stuff ************************************
    // ****************************************v****************************************
    private fun addLog(text: String) {
        binding.logText.append(
            """
    
    $text
    """.trimIndent()
        )
    }

    private val updateTimerThread: Runnable = object : Runnable {
        override fun run() {
            val beanStatus = dsTrackerLite.getStatus()

            addLog("Timer: " + convertMillisecondsToHMmSs(beanStatus.serviceTime))
            addLog("Distance: " + beanStatus.totalDistance)
            handlerTrip.postDelayed(this, 2000)
        }
    }

    private fun convertMillisecondsToHMmSs(millisenconds: Long): String {
        val seconds = millisenconds / 1000
        val s = seconds % 60
        val m = seconds / 60 % 60
        val h = seconds / (60 * 60) % 24
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    override fun motionDetectedActivity(activity: DSInternalMotionActivities, percentage: Int) {
    }

    override fun motionStatus(result: DSMotionEvents) {
    }

    override fun startService(result: DSResult) {
        Log.i("LITE", ">>"+result.toString())
    }

    override fun statusEventService(result: DSResult) {
    }

    override fun stopService(result: DSResult) {
        Log.i("LITE", ">"+result.toString())
    }
    // ****************************************v****************************************
    // ******************************* Client Stuff ************************************
    // ****************************************v****************************************

}