package sd.lemon.amin.runningapp_distanceruntracker.common

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.GPS_UTILITY_TAG
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.REQUEST_CODE_GPS
import timber.log.Timber


class GPSUtility(private val context: Context) {

    private var listener: (() -> Unit)? = null

    fun setListener(listener: () -> Unit) {
        this.listener = listener
    }

    private var settingClient: SettingsClient? = null
    private var locationSettingRequest: LocationSettingsRequest? = null
    private var locationManager: LocationManager? = null
    private var locationRequest: LocationRequest? = null

    init {
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        settingClient = LocationServices.getSettingsClient(context)
        locationRequest = LocationRequest.create()
        locationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest?.interval = 1000
        locationRequest?.fastestInterval = 500
        if (locationRequest != null) {
            val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(locationRequest!!)
            locationSettingRequest = builder.build()
        }
    }

    fun turnOnGPS() {
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
            settingClient?.checkLocationSettings(locationSettingRequest!!)
                ?.addOnSuccessListener {
                    listener?.let { listener ->
                        listener()
                    }
                }?.addOnFailureListener { exception ->
                    if ((exception as ApiException).statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            val resolvableApiException = exception as ResolvableApiException
                            resolvableApiException.startResolutionForResult(
                                context as Activity,
                                REQUEST_CODE_GPS
                            )
                        } catch (e: Exception) {
                            Timber.tag(GPS_UTILITY_TAG)
                                .d("Turn On GPS: Unable to start default functionality of GPS")
                        }
                    } else {
                        if (exception.statusCode == LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE) {
                            val errorMessage =
                                "Location setting are inadequate, and cannot be fixed.Fix in Settings."
                            Timber.tag(GPS_UTILITY_TAG)
                                .d(errorMessage)
                            Toast.makeText(
                                context,
                                "Location setting are inadequate, and cannot be fixed.Fix in Settings.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

        }
    }
}