package sd.lemon.amin.runningapp_distanceruntracker.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.common.Utility
import sd.lemon.amin.runningapp_distanceruntracker.databinding.ItemRunBinding
import sd.lemon.amin.runningapp_distanceruntracker.db.Run
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    private var listener: ((run: Run) -> Unit)? = null

    fun setDeleteListener(listener: ((run: Run) -> Unit)) {
        this.listener = listener
    }

    private val differCallBack = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, differCallBack)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunAdapter.RunViewHolder {
        return RunViewHolder(
            ItemRunBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RunAdapter.RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            holder.bind(run)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class RunViewHolder(private val binding: ItemRunBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(run: Run) {
            binding.layoutRunView.setOnLongClickListener {
                MaterialAlertDialogBuilder(it.context)
                    .setTitle(it.context.getString(R.string.delete_run))
                    .setMessage(it.context.getString(R.string.are_you_sure_to_dekete_run))
                    .setIcon(R.drawable.ic_baseline_delete_24)
                    .setPositiveButton(it.context.getString(R.string.yes)) { _, _ ->
                        listener?.let { listener ->
                            listener(run)
                        }
                    }
                    .setNegativeButton(it.context.getString(R.string.no)) { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.create().show()
                return@setOnLongClickListener true
            }
            Glide.with(binding.root).load(run.image).into(binding.ivRunImage)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timesTamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calendar.time)

            val distance = "${run.distanceInMeters / 1000f} km"
            binding.tvDistance.text = distance

            binding.tvTime.text = Utility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned} cals"
            binding.tvCalories.text = caloriesBurned

            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            binding.tvAvgSpeed.text = avgSpeed
        }
    }
}