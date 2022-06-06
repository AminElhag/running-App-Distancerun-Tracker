package sd.lemon.amin.runningapp_distanceruntracker.ui.fragments

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.ACTION_PAUSE_SERVICES
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.ACTION_START_OR_RESUME_SERVICES
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.ACTION_STOP_SERVICES
import sd.lemon.amin.runningapp_distanceruntracker.common.GPSUtility
import sd.lemon.amin.runningapp_distanceruntracker.common.Utility
import sd.lemon.amin.runningapp_distanceruntracker.databinding.FragmentTrackingBinding
import sd.lemon.amin.runningapp_distanceruntracker.db.Run
import sd.lemon.amin.runningapp_distanceruntracker.services.Polyline
import sd.lemon.amin.runningapp_distanceruntracker.services.TrackingServices
import sd.lemon.amin.runningapp_distanceruntracker.ui.viewmodels.MainViewModel
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val DIALOG_TAG = "DIALOG_TAG"

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private lateinit var binding: FragmentTrackingBinding
    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoint = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L
    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    @Inject
    lateinit var gpsUtility: GPSUtility

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        val getResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    toggleRun()
                } else {
                    GPSUtility(requireContext()).turnOnGPS()
                }
            }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync {
            map = it
            addAllPoint()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomOutToSeeWholeTrack()
            endAndSaveRun()
        }
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        subscribe()
        savedInstanceState?.let {
            val cancelTrackingDialog =
                parentFragmentManager.findFragmentByTag(DIALOG_TAG) as CancelTrackingDialog
            cancelTrackingDialog.setListener {
                stopRun()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeInMillis < 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    private fun addLatestPolyline() {
        if (pathPoint.isNotEmpty() && pathPoint.last().size > 1) {
            val perLast = pathPoint.last()[pathPoint.last().size - 1]
            val last = pathPoint.last().last()
            val polylineOptions = PolylineOptions()
                .color(Color.RED)
                .width(5f)
                .add(perLast)
                .add(last)
            map?.addPolyline(polylineOptions)
            moveCamera()
        }
    }

    private fun addAllPoint() {
        pathPoint.forEach {
            val polylineOptions = PolylineOptions()
                .color(Color.RED)
                .width(5f)
                .addAll(it)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun moveCamera() {
        if (pathPoint.isNotEmpty() && pathPoint.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoint.last().last(), 15f
                )
            )
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (isTracking) {
            binding.btnToggleRun.text = getString(R.string.stop)
            binding.btnFinishRun.visibility = View.GONE
        } else if (!isTracking && curTimeInMillis > 0L) {
            menu?.getItem(0)?.isVisible = true
            binding.btnToggleRun.text = getString(R.string.start)
            binding.btnFinishRun.visibility = View.VISIBLE
        }
    }

    private fun toggleRun() {
        if (isTracking) {
            TrackingServices.sendCommandToService(requireContext(), ACTION_PAUSE_SERVICES)
        } else {
            menu?.getItem(0)?.isVisible = true
            TrackingServices.sendCommandToService(requireContext(), ACTION_START_OR_RESUME_SERVICES)
        }
    }

    private fun subscribe() {
        TrackingServices.isTracking.observe(viewLifecycleOwner) {
            updateTracking(it)
        }
        TrackingServices.pathPoints.observe(viewLifecycleOwner) {
            pathPoint = it
            addLatestPolyline()
        }

        TrackingServices.timeRunInMillis.observe(viewLifecycleOwner) {
            curTimeInMillis = it
            val formattedTime = Utility.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        }
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setListener {
                stopRun()
            }
        }.show(parentFragmentManager, DIALOG_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        TrackingServices.sendCommandToService(requireContext(), ACTION_STOP_SERVICES)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun zoomOutToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()

        pathPoint.forEach { polyline ->
            polyline.forEach { latLng ->
                bounds.include(latLng)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endAndSaveRun() {
        map?.snapshot { bitmap ->
            var distanceInMeters = 0
            pathPoint.forEach { polyline ->
                distanceInMeters = Utility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dataTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(
                bitmap,
                dataTimestamp,
                avgSpeed,
                distanceInMeters,
                curTimeInMillis,
                caloriesBurned
            )

            viewModel.insert(run)
            stopRun()
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                getString(R.string.run_saved_successfully),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}