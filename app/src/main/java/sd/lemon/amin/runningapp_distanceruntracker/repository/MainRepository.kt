package sd.lemon.amin.runningapp_distanceruntracker.repository

import sd.lemon.amin.runningapp_distanceruntracker.db.Run
import sd.lemon.amin.runningapp_distanceruntracker.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDAO: RunDAO
) {
    suspend fun insert(run: Run) = runDAO.insertRun(run)

    suspend fun delete(run: Run) = runDAO.deleteRun(run)

    fun getAllRunSortByTimesTamp() = runDAO.getAllRunSortByTimesTamp()

    fun getAllRunSortByAvgSpeedInKMH() = runDAO.getAllRunSortByAvgSpeedInKMH()

    fun getAllRunSortByCaloriesBurned() = runDAO.getAllRunSortByCaloriesBurned()

    fun getAllRunSortByDistanceInMeters() = runDAO.getAllRunSortByDistanceInMeters()

    fun getAllRunSortByTimeInMillis() = runDAO.getAllRunSortByTimeInMillis()

    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned()

    fun getTotalDistanceInMeters() = runDAO.getTotalDistanceInMeters()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()

    fun getAvgOfAvgSpeedInKMH() = runDAO.getAvgOfAvgSpeedInKMH()
}