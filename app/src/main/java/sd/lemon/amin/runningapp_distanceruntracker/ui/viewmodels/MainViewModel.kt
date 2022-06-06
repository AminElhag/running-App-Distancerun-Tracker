package sd.lemon.amin.runningapp_distanceruntracker.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import sd.lemon.amin.runningapp_distanceruntracker.common.SortType
import sd.lemon.amin.runningapp_distanceruntracker.db.Run
import sd.lemon.amin.runningapp_distanceruntracker.repository.MainRepository
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {
    fun insert(run: Run) = viewModelScope.launch {
        repository.insert(run)
    }

    private fun runsSortByDate() = repository.getAllRunSortByTimesTamp()
    private fun runsSortByTimeInMillis() = repository.getAllRunSortByTimeInMillis()
    private fun runsSortByDistanceInMeters() = repository.getAllRunSortByDistanceInMeters()
    private fun runsSortByAvgSpeedInKMH() = repository.getAllRunSortByAvgSpeedInKMH()
    private fun runsSortByCaloriesBurned() = repository.getAllRunSortByCaloriesBurned()

    fun deleteRun(run: Run) = viewModelScope.launch {
        repository.delete(run)
    }

    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(runsSortByDate()) { result ->
            if (sortType == SortType.DATE) {
                result?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runsSortByTimeInMillis()) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runsSortByAvgSpeedInKMH()) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runsSortByCaloriesBurned()) { result ->
            if (sortType == SortType.CALORIES_BURNED) {
                result?.let {
                    runs.value = it
                }
            }
        }
        runs.addSource(runsSortByDistanceInMeters()) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let {
                    runs.value = it
                }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> runsSortByDate().value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortByDistanceInMeters().value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortByAvgSpeedInKMH().value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortByCaloriesBurned().value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortByTimeInMillis().value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }
}