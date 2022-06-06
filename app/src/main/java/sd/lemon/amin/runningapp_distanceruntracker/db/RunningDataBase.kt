package sd.lemon.amin.runningapp_distanceruntracker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Run::class], version = 1)
@TypeConverters(Converters::class)
abstract class RunningDataBase : RoomDatabase() {
    abstract fun getRunDAO(): RunDAO
}