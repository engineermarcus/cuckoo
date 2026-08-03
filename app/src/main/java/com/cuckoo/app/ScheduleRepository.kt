package com.cuckoo.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ScheduleRepository {
    private const val PREFS = "cuckoo_prefs"
    private const val KEY_ITEMS = "schedule_items"
    private val gson = Gson()

    private fun defaultItems(): List<ScheduleItem> = listOf(
        ScheduleItem(1, "Math", 13, 0),
        ScheduleItem(2, "Chores", 14, 0),
        ScheduleItem(3, "C++", 16, 0),
        ScheduleItem(4, "Kotlin", 17, 45),
        ScheduleItem(5, "Electronics", 19, 30),
        ScheduleItem(6, "Electromagnetism", 21, 30),
        ScheduleItem(7, "Review / buffer", 23, 0)
    )

    fun getItems(context: Context): MutableList<ScheduleItem> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ITEMS, null) ?: run {
            val defaults = defaultItems()
            saveItems(context, defaults)
            return defaults.toMutableList()
        }
        val type = object : TypeToken<MutableList<ScheduleItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun saveItems(context: Context, items: List<ScheduleItem>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ITEMS, gson.toJson(items)).apply()
    }

    fun nextId(context: Context): Int {
        val items = getItems(context)
        return (items.maxOfOrNull { it.id } ?: 0) + 1
    }
}
