package sd.lemon.amin.runningapp_distanceruntracker.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sd.lemon.amin.runningapp_distanceruntracker.R

class CancelTrackingDialog : DialogFragment() {

    private var listener: (() -> Unit)? = null

    fun setListener(listener: () -> Unit) {
        this.listener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.cancel_tracking_dialog_title))
            .setMessage(getString(R.string.cancel_tracking_dialog_message))
            .setIcon(R.drawable.ic_baseline_delete_24)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                listener?.let { listener ->
                    listener()
                }
            }
            .setNegativeButton(getString(R.string.no)) { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create()
    }
}