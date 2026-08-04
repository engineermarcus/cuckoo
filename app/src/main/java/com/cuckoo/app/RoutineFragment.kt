package com.cuckoo.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.fragment.app.Fragment
import java.time.LocalDate
import java.time.LocalTime

class RoutineFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable
    private var tickCount = 0
    private var accessibilityEnabledCache = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_routine, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val textCurrentTask    = view.findViewById<TextView>(R.id.textCurrentTask)
        val textTaskStatus     = view.findViewById<TextView>(R.id.textTaskStatus)
        val textDuration       = view.findViewById<TextView>(R.id.textDurationCounter)
        val textFrozenApps     = view.findViewById<TextView>(R.id.textFrozenApps)
        val textAccessHint     = view.findViewById<MaterialButton>(R.id.textAccessibilityHint)
        val textCompletions    = view.findViewById<TextView>(R.id.textCompletionCount)

        textAccessHint.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        runnable = object : Runnable {
            override fun run() {
                val now = LocalTime.now()
                val items = ScheduleRepository.getItems(requireContext())

                val active = items.firstOrNull { item ->
                    val start = LocalTime.of(item.hour, item.minute)
                    val end   = LocalTime.of(item.endHour, item.endMinute)
                    if (end.isAfter(start)) now.isAfter(start) && now.isBefore(end)
                    else now.isAfter(start) || now.isBefore(end) // overnight
                }

                if (active != null) {
                    val end = LocalTime.of(active.endHour, active.endMinute)
                    val nowSec = now.toSecondOfDay()
                    var endSec = end.toSecondOfDay()
                    if (endSec < nowSec) endSec += 86400 // overnight
                    val remaining = endSec - nowSec

                    val h = remaining / 3600
                    val m = (remaining % 3600) / 60
                    val s = remaining % 60

                    textCurrentTask.text = active.label
                    textTaskStatus.text  = "In progress"
                    textDuration.text    = String.format("%02d:%02d:%02d", h, m, s)
                    textDuration.setTextColor(0xFF4CAF50.toInt())

                    // completion count
                    val done = EventRepository.forSchedule(requireContext(), active.id)
                        .count { it.status == EventStatus.DONE }
                    textCompletions.text = done.toString()

                    if (tickCount % 5 == 0) {
                        accessibilityEnabledCache = AppFreezeService.isEnabled(requireContext())
                    }
                    if (accessibilityEnabledCache) {
                        textFrozenApps.text = "All apps except Cuckoo"
                        textAccessHint.visibility = View.GONE
                    } else {
                        textFrozenApps.text = "Not enforced"
                        textAccessHint.visibility = View.VISIBLE
                    }

                } else {
                    textCurrentTask.text = "Spawn Time"
                    textTaskStatus.text = "No active task - rest or reshuffle"
                    textDuration.text    = "--:--:--"
                    textDuration.setTextColor(0xFF888888.toInt())
                    textFrozenApps.text  = "None"
                    textAccessHint.visibility = View.GONE
                    textCompletions.text = "-"
                }

                tickCount++
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
    }
}
