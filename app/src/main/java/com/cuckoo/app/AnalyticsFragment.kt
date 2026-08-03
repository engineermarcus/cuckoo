package com.cuckoo.app

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import java.time.LocalDate

class AnalyticsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val donutView       = view.findViewById<DonutChartView>(R.id.donutChart)
        val textTotal       = view.findViewById<TextView>(R.id.textTotalScreenTime)
        val textScore       = view.findViewById<TextView>(R.id.textDisciplineScore)
        val recyclerTasks   = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerAnalytics)
        val recyclerScreen  = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerScreenTime)
        val btnGrant        = view.findViewById<Button>(R.id.btnGrantUsage)

        // discipline score
        val events = EventRepository.getAll(requireContext())
        val done   = events.count { it.status == EventStatus.DONE }
        val missed = events.count { it.status == EventStatus.MISSED }
        val total  = done + missed
        val score  = if (total == 0) "--" else "${done * 100 / total}%"
        textScore.text = score
        if (total > 0) {
            val pct = done * 100 / total
            textScore.setTextColor(when {
                pct >= 80 -> 0xFF4CAF50.toInt()
                pct >= 50 -> 0xFFFFC107.toInt()
                else      -> 0xFFF44336.toInt()
            })
        }

        // task breakdown
        val items = ScheduleRepository.getItems(requireContext())
        val taskStats = items.map { item ->
            Triple(item.label,
                events.count { it.scheduleId == item.id && it.status == EventStatus.DONE },
                events.count { it.scheduleId == item.id && it.status == EventStatus.MISSED })
        }
        recyclerTasks.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        recyclerTasks.adapter = TaskStatsAdapter(taskStats)

        // usage stats
        if (!hasUsagePermission()) {
            btnGrant.visibility = View.VISIBLE
            btnGrant.setOnClickListener {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            return
        }

        btnGrant.visibility = View.GONE
        val usageList = getUsageStats()

        if (usageList.isEmpty()) {
            textTotal.text = "No screen time data yet"
            return
        }

        val totalMs = usageList.sumOf { it.second }
        val totalMin = totalMs / 60000
        textTotal.text = "TODAY\n${formatTime(totalMin)}"

        donutView.setData(usageList.map { (name, ms) ->
            Pair(name, ms.toFloat() / totalMs)
        })

        recyclerScreen.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        recyclerScreen.adapter = ScreenTimeAdapter(usageList.map { (name, ms) ->
            Pair(name, formatTime(ms / 60000))
        })
    }

    private fun hasUsagePermission(): Boolean {
        val usm = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 86400000, now)
        return stats != null && stats.isNotEmpty()
    }

    private fun getUsageStats(): List<Pair<String, Long>> {
        val usm = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val midnight = now - (now % 86400000)
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, midnight, now)
        val pm = requireContext().packageManager
        return stats
            .filter { it.totalTimeInForeground > 60000 } // at least 1 min
            .sortedByDescending { it.totalTimeInForeground }
            .take(6)
            .map { stat ->
                val name = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(stat.packageName, 0)).toString()
                } catch (e: Exception) { stat.packageName }
                Pair(name, stat.totalTimeInForeground)
            }
    }

    private fun formatTime(minutes: Long): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
