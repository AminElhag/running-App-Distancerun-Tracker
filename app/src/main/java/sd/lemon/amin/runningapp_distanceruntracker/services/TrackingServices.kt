package sd.lemon.amin.runningapp_distanceruntracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.ACTION_PAUSE_SERVICES
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.ACTION_START_OR_RESUME_SERVICES
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.ACTION_STOP_SERVICES
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.FASTEST_LOCATION_INTERVAL
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.LOCATION_UPDATE_INTERVAL
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.NOTIFICATION_CHANNEL_ID
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.NOTIFICATION_CHANNEL_NAME
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.NOTIFICATION_ID
import sd.lemon.amin.runningapp_distanceruntracker.common.Utility
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingServices : LifecycleService() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    lateinit var currentNotificationBuilder: NotificationCompat.Builder

    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        val timeRunInMillis = MutableLiveData<Long>()
        fun sendCommandToService(context: Context, action: String) {
            Intent(context, TrackingServices::class.java).also {
                it.action = action
                context.startService(it)
            }
        }
    }

    private val timeInSeconds = MutableLiveData<Long>()

    private var isFirstTime = true

    private var isServiceKill = false

    private var isTimerTracking = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimesTamp = 0L


    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        postInitialValue()
        currentNotificationBuilder = notificationBuilder
        isTracking.observe(this) {
            updateLocationTracking(it)
            updateNotification(it)
        }
    }

    private fun postInitialValue() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInMillis.postValue(0L)
        timeInSeconds.postValue(0L)
    }

    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerTracking = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimesTamp + 1000L) {
                    timeInSeconds.postValue(timeInSeconds.value!! + 1)
                    lastSecondTimesTamp += 1000L
                }
                delay(50L)
            }
            timeRun += lapTime
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICES -> {
                    if (isFirstTime) {
                        startForegroundService()
                        isFirstTime = true
                    } else {
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICES -> {
                    pauseService()
                }
                ACTION_STOP_SERVICES -> {
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerTracking = false
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (Utility.hasLocationPermissions(this)) {
                val requires = LocationRequest.create().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    requires, locationCallBack, Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallBack)
        }
    }

    private val locationCallBack = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.forEach { location ->
                    addPathPoint(location)
                    Timber.d("THE LOCATION: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val position = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        startTimer()
        isTracking.postValue(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timeInSeconds.observe(this) {
            if (!isServiceKill) {
                val notification =
                    currentNotificationBuilder.setContentText(Utility.getFormattedStopWatchTime(it * 1000L))

                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "UnspecifiedImmutableFlag")
    private fun updateNotification(isTracking: Boolean) {
        val notificationActionTest =
            if (isTracking) getString(R.string.resume) else getString(R.string.start)

        val notificationIActionIcon =
            if (isTracking)
                R.drawable.ic_baseline_pause_presentation_24 else R.drawable.ic_baseline_not_started_24

        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingServices::class.java).apply {
                action = ACTION_PAUSE_SERVICES
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            val pauseIntent = Intent(this, TrackingServices::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICES
            }
            PendingIntent.getService(this, 2, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if (!isServiceKill) {
            currentNotificationBuilder = notificationBuilder
                .addAction(notificationIActionIcon, notificationActionTest, pendingIntent)

            notificationManager.notify(NOTIFICATION_ID, currentNotificationBuilder.build())
        }
    }

    private fun killService() {
        isFirstTime = true
        isServiceKill = true
        postInitialValue()
        pauseService()
        stopForeground(true)
        stopSelf()
    }
}