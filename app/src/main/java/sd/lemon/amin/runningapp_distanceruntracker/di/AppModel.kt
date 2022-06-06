package sd.lemon.amin.runningapp_distanceruntracker.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.DATA_BASE_NAME
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.KEY_FIRST_TIME_TOGGLE
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.KEY_NAME
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.KEY_WEIGHT
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.SHARED_PREFERENCES_NAME
import sd.lemon.amin.runningapp_distanceruntracker.common.GPSUtility
import sd.lemon.amin.runningapp_distanceruntracker.db.RunningDataBase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModel {

    @Singleton
    @Provides
    fun provideRunningDataBase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context, RunningDataBase::class.java,
        DATA_BASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDAO(
        dataBase: RunningDataBase
    ) = dataBase.getRunDAO()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideGetNameFormSharedPreference(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(
            KEY_NAME, ""
        )

    @Singleton
    @Provides
    fun provideGetWeightFormSharedPreference(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(
            KEY_WEIGHT, 80f
        )

    @Singleton
    @Provides
    fun provideGetFirstTimeFormSharedPreference(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(
            KEY_FIRST_TIME_TOGGLE, true
        )

    @Singleton
    @Provides
    fun provideGPSUtility(@ApplicationContext context: Context) = GPSUtility(context)
}