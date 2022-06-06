package sd.lemon.amin.runningapp_distanceruntracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.KEY_FIRST_TIME_TOGGLE
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.KEY_NAME
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.KEY_WEIGHT
import sd.lemon.amin.runningapp_distanceruntracker.databinding.FragmentSetupBinding
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var isFirstOpen = true

    private lateinit var binding: FragmentSetupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, inclusive = true)
                .build()

            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment, savedInstanceState, navOptions
            )

        }

        binding.tvContinue.setOnClickListener {
            val success = writePersonalDataInSharedPreferences()
            if (success) {
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            } else {
                Snackbar.make(
                    requireView(),
                    getString(R.string.please_enter_all_fields),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun writePersonalDataInSharedPreferences(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        val toolbarText = "Let's go, $name"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText
        return true
    }
}