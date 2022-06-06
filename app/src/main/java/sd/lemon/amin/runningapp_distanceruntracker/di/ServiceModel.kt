package sd.lemon.amin.runningapp_distanceruntracker.di

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.NOTIFICATION_CHANNEL_ID
import sd.lemon.amin.runningapp_distanceruntracker.ui.MainActivity

@Module
@InstallIn(ServiceComponent::class)
object ServiceModel {

    @SuppressLint("VisibleForTests")
    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context) =
        FusedLocationProviderClient(context)


    @ServiceScoped
    @Provides
    fun providesPendingIntent(@ApplicationContext context: Context): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_Tracking_FRAGMENT
            }, PendingIntent.FLAG_UPDATE_CURRENT
        )

    @ServiceScoped
    @Provides
    fun providesBaseNotificationCompat(
        @ApplicationContext context: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
        .setContentTitle(context.getString(R.string.app_name))
        .setContentText("You run: 00:00:00")
        .setContentIntent(pendingIntent)
}