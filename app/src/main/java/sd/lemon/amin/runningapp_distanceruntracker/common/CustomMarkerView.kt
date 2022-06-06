package sd.lemon.amin.runningapp_distanceruntracker.common

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import sd.lemon.amin.runningapp_distanceruntracker.R
import sd.lemon.amin.runningapp_distanceruntracker.db.Run
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("ViewConstructor")
class CustomMarkerView(
    private val runs: List<Run>,
    context: Context, resId: Int
) : MarkerView(context, resId) {

    override fun getOffset(): MPPointF {
        return MPPointF(-width / 2f, -height.toFloat())
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        e?.let {
            val curRunId = e.x.toInt()
            val run = runs[curRunId]

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timesTamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            findViewById<TextView>(R.id.tvDate).text = dateFormat.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMH}km/h"
            findViewById<TextView>(R.id.tvAvgSpeed).text = avgSpeed

            val distance = "${run.distanceInMeters / 1000f} km"
            findViewById<TextView>(R.id.tvDistance).text = distance

            findViewById<TextView>(R.id.tvDistance).text =
                Utility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesBurned = "${run.caloriesBurned} cals"
            findViewById<TextView>(R.id.tvCaloriesBurned).text = caloriesBurned

        }
    }
}