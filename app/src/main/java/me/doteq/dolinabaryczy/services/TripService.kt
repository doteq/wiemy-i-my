package me.doteq.dolinabaryczy.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.*
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import me.doteq.dolinabaryczy.R
import me.doteq.dolinabaryczy.data.models.Poi
import me.doteq.dolinabaryczy.utilities.Constants.ACTION_START_OR_RESUME_SERVICE
import me.doteq.dolinabaryczy.utilities.Constants.ACTION_STOP_SERVICE
import me.doteq.dolinabaryczy.utilities.Constants.NOTIFICATION_CHANNEL_ID
import me.doteq.dolinabaryczy.utilities.Constants.NOTIFICATION_CHANNEL_NAME
import me.doteq.dolinabaryczy.utilities.Constants.NOTIFICATION_ID
import me.doteq.dolinabaryczy.utilities.Constants.NOTIFICATION_NEARBY_CHANNEL_ID
import me.doteq.dolinabaryczy.utilities.Constants.NOTIFICATION_NEARBY_CHANNEL_NAME
import me.doteq.dolinabaryczy.utilities.Utilities.toDistanceString
import javax.inject.Inject

data class Direction(
    val distance: Float?,
    val bearing: Float?
)

@AndroidEntryPoint
class TripService : LifecycleService() {

    var isFirstRun = true

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var pendingIntent: PendingIntent

    companion object {
        val direction = MutableLiveData<Direction>()
        val trackedPoi = MutableStateFlow<Poi?>(null)
    }

    private fun postInitialValues() {
        direction.postValue(Direction(null, null))
    }


    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        val request = LocationRequest.Builder(5000).build()
                        fusedLocationProviderClient.requestLocationUpdates(
                            request,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                        isFirstRun = false
                    }
                }
                ACTION_STOP_SERVICE -> {
                    isFirstRun = true
                    postInitialValues()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)

            result.lastLocation?.let { location ->
                trackedPoi.value?.let { poi ->
                    val nextLocation = Location("nextPointLocation")
                    nextLocation.latitude = poi.lat
                    nextLocation.longitude = poi.lon
                    direction.postValue(
                        Direction(
                            location.distanceTo(nextLocation),
                            location.bearingTo(nextLocation)
                        )
                    )

                    val notificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        buildNotification(poi.name, location.distanceTo(nextLocation))
                    )
                }
            }
        }
    }

    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)


        startForeground(NOTIFICATION_ID, buildNotification(null, null))
    }

    private fun buildNotification(poiName: String?, distance: Float?): Notification {
        val notificationText = if (poiName != null && distance != null) {
            "$poiName, ${distance.toDistanceString()}"
        } else null

        return if (distance === null || distance > 100) {
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_map_24)
                .setContentTitle("Gra w toku")
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setColor(ContextCompat.getColor(this, R.color.poi_nearby_notification))
                .build()
        } else {
            NotificationCompat.Builder(this, NOTIFICATION_NEARBY_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_baseline_map_24)
                .setContentTitle("Gra w toku")
                .setContentText("Jesteś blisko punktu")
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(this, R.color.poi_nearby_notification))
                .setColorized(true)
                .setOnlyAlertOnce(true)
                .build()
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        notificationManager.createNotificationChannel(
            NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_NEARBY_CHANNEL_ID,
                NOTIFICATION_NEARBY_CHANNEL_NAME,
                IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
            }
        )
    }
}