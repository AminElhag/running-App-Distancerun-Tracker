package sd.lemon.amin.runningapp_distanceruntracker.common

object Constants {
    const val DATA_BASE_NAME = "running database"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0
    const val LOCATION_UPDATE_FASTEST_INTERVAL = 350L

    const val ACTION_START_OR_RESUME_SERVICES = "ACTION_START_OR_RESUME_SERVICES"
    const val ACTION_PAUSE_SERVICES = "ACTION_PAUSE_SERVICES"
    const val ACTION_STOP_SERVICES = "ACTION_STOP_SERVICES"
    const val ACTION_SHOW_Tracking_FRAGMENT = "ACTION_SHOW_Tracking_FRAGMENT"

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val SHARED_PREFERENCES_NAME = "RunningSharedPreferences"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_NAME"
    const val KEY_WEIGHT = "KEY_WEIGHT"

    const val REQUEST_CODE_GPS = 1998

    //TAGS
    const val GPS_UTILITY_TAG = "GPS_Utility"
}