package com.cuckoo.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate

class AnalyticsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val textScore       = view.findViewById<TextView>(R.id.textDisciplineScore)
        val recyclerTasks   = view.findViewById<RecyclerView>(R.id.recyclerAnalytics)
        val recyclerScreen  = view.findViewById<RecyclerView>(R.id.recyclerScreenTime)

        val items  = ScheduleRepository.getItems(requireContext())
        val events = EventRepository.getAll(requireContext())

        // discipline score = done / (done + missed) * 100
        val done   = events.count { it.status == EventStatus.DONE }
        val missed = events.count { it.status == EventStatus.MISSED }
        val total  = done + missed
        val score  = if (total == 0) "--" else "${(done * 100 / total)}%"
        textScore.text = score

        // color score
        if (total > 0) {
            val pct = done * 100 / total
            textScore.setTextColor(when {
                pct >= 80 -> 0xFF4CAF50.toInt()
                pct >= 50 -> 0xFFFFC107.toInt()
                else      -> 0xFFF44336.toInt()
            })
        }

        // task breakdown
        val taskStats = items.map { item ->
            val itemDone   = events.count { it.scheduleId == item.id && it.status == EventStatus.DONE }
            val itemMissed = events.count { it.scheduleId == item.id && it.status == EventStatus.MISSED }
            Triple(item.label, itemDone, itemMissed)
        }

        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        recyclerTasks.adapter = TaskStatsAdapter(taskStats)

        // screen time — placeholder until UsageStatsManager is wired
        recyclerScreen.layoutManager = LinearLayoutManager(requireContext())
        recyclerScreen.adapter = ScreenTimeAdapter(emptyList())
    }
}
