package sd.lemon.amin.runningapp_distanceruntracker.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.ACTION_SHOW_Tracking_FRAGMENT
import sd.lemon.amin.runningapp_distanceruntracker.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigationToTrackingFragmentIfNeeded(intent)
        setSupportActionBar(binding.toolbar)
        binding.bottomNavigationView.setupWithNavController(
            binding.flFragment[0].findNavController()
        )
        binding.bottomNavigationView.setOnItemReselectedListener {}
        binding.flFragment[0].findNavController().addOnDestinationChangedListener { _, des, _ ->
            when (des.id) {
                R.id.runFragment, R.id.settingsFragment, R.id.statisticsFragment -> binding.bottomNavigationView.visibility =
                    View.VISIBLE
                else -> binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigationToTrackingFragmentIfNeeded(intent)
    }

    private fun navigationToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_Tracking_FRAGMENT) {
            binding.flFragment[0].findNavController()
                .navigate(R.id.action_global_to_trackingFragment)
        }
    }
}