package sd.lemon.amin.runningapp_distanceruntracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import sd.lemon.amin.runningapp_distanceruntracker.repository.MainRepository
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {

    val totalTimeRun = repository.getTotalTimeInMillis()
    val totalCaloriesBurned = repository.getTotalCaloriesBurned()
    val totalDistanceInMeters = repository.getTotalDistanceInMeters()
    val getAvgOfAvgSpeedInKMH = repository.getAvgOfAvgSpeedInKMH()

    val runSortByData = repository.getAllRunSortByTimesTamp()
}