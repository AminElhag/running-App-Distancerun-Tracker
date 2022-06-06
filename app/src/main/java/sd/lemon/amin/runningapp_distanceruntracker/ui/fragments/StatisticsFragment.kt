package sd.lemon.amin.runningapp_distanceruntracker.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.common.CustomMarkerView
import sd.lemon.amin.runningapp_distanceruntracker.common.Utility
import sd.lemon.amin.runningapp_distanceruntracker.databinding.FragmentStatisticsBinding
import sd.lemon.amin.runningapp_distanceruntracker.ui.viewmodels.StatisticsViewModel
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private lateinit var binding: FragmentStatisticsBinding

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBarChart()
        subscribeToObservers()
    }

    private fun setupBarChart() {
        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.apply {
            description.text = requireContext().getString(R.string.avg_speed_over_time)
            legend.isEnabled = false
        }
    }

    private fun subscribeToObservers() {
        viewModel.totalDistanceInMeters.observe(viewLifecycleOwner) {
            it?.let {
                val distance = "${round((it / 1000f) * 10f) / 10f}kml"
                binding.tvTotalDistance.text = distance
            }
        }
        viewModel.totalTimeRun.observe(viewLifecycleOwner) {
            it?.let {
                val time = Utility.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = time
            }
        }
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner) {
            it?.let {
                val calories = "${it}calo"
                binding.tvTotalCalories.text = calories
            }
        }
        viewModel.getAvgOfAvgSpeedInKMH.observe(viewLifecycleOwner) {
            it?.let {
                val avgSpeed = "${round((it / 1000f) * 10f) / 10f}KM/H"
                binding.tvAverageSpeed.text = avgSpeed
            }
        }
        viewModel.runSortByData.observe(viewLifecycleOwner) {
            it?.let {
                val allAvgSpeed = it.indices.map { i -> BarEntry(i.toFloat(), it[i].avgSpeedInKMH) }
                val barDataSet = BarDataSet(
                    allAvgSpeed,
                    requireContext().getString(R.string.avg_speed_over_time)
                ).apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.md_blue_A100)
                }
                binding.barChart.data = BarData(barDataSet)
                binding.barChart.marker =
                    CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
                binding.barChart.invalidate()
            }
        }
    }
}