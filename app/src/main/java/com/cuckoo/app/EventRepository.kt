package com.cuckoo.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

object EventRepository {
    private const val PREFS = "cuckoo_prefs"
    private const val KEY_EVENTS = "task_events"
    private val gson = Gson()

    fun getAll(context: Context): MutableList<TaskEvent> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_EVENTS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<TaskEvent>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveAll(context: Context, events: List<TaskEvent>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_EVENTS, gson.toJson(events)).apply()
    }

    /** Record or overwrite today's event for a given schedule item. */
    fun record(context: Context, scheduleId: Int, status: EventStatus, dateEpochDay: Long = LocalDate.now().toEpochDay()) {
        val events = getAll(context)
        val existing = events.find { it.scheduleId == scheduleId && it.dateEpochDay == dateEpochDay }
        if (existing != null) {
            existing.status = status
        } else {
            events.add(TaskEvent(scheduleId, dateEpochDay, status))
        }
        saveAll(context, events)
    }

    fun forSchedule(context: Context, scheduleId: Int): List<TaskEvent> =
        getAll(context).filter { it.scheduleId == scheduleId }.sortedBy { it.dateEpochDay }

    fun forDay(context: Context, dateEpochDay: Long): List<TaskEvent> =
        getAll(context).filter { it.dateEpochDay == dateEpochDay }

    fun eventFor(context: Context, scheduleId: Int, dateEpochDay: Long): TaskEvent? =
        getAll(context).find { it.scheduleId == scheduleId && it.dateEpochDay == dateEpochDay }
}
