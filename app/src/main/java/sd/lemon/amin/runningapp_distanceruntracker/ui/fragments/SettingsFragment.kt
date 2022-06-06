package sd.lemon.amin.runningapp_distanceruntracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.w3c.dom.Text
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.KEY_NAME
import sd.lemon.amin.runningapp_distanceruntracker.common.Constants.KEY_WEIGHT
import sd.lemon.amin.runningapp_distanceruntracker.databinding.FragmentSettingsBinding
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDataFormSharedPreferences()
        binding.btnApplyChanges.setOnClickListener { view ->
            val success = updatePersonalDataInSharedPreferences()
            if (success) {
                Snackbar.make(view, getString(R.string.update_success), Snackbar.LENGTH_SHORT)
                    .show()
            } else {
                Snackbar.make(
                    view,
                    getString(R.string.please_enter_all_fields),
                    Snackbar.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun loadDataFormSharedPreferences() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    private fun updatePersonalDataInSharedPreferences(): Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(Constants.KEY_NAME, name)
            .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
            .putBoolean(Constants.KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        val toolbarText = "Let's go, $name"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText
        return true
    }
}