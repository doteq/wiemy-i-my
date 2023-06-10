package me.doteq.dolinabaryczy.utilities

import me.doteq.dolinabaryczy.R
import me.doteq.dolinabaryczy.data.models.QuizQuestion
import me.doteq.dolinabaryczy.data.models.Poi

object Constants {
    const val MAP_DEFAULT_LATITUDE = 51.65
    const val MAP_DEFAULT_LONGITUDE = 17.81
    const val MAP_DEFAULT_ZOOM = 12.0
    const val MAP_DEFAULT_SELECTION_ZOOM = 15.0

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRIP_FRAGMENT = "ACTION_SHOW_TRIP_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "trip_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Trip"
    const val NOTIFICATION_NEARBY_CHANNEL_ID = "trip_nearby_channel"
    const val NOTIFICATION_NEARBY_CHANNEL_NAME = "Point Nearby"
    const val NOTIFICATION_ID = 1

    const val CORRECT_ANSWER_POINTS = 10
    const val HALF_POINTS_ANSWER_POINTS = 5

}