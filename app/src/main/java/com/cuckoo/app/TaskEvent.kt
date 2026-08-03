package com.cuckoo.app

enum class EventStatus { STARTED, DONE, SKIPPED, MISSED }

data class TaskEvent(
    val scheduleId: Int,
    val dateEpochDay: Long,   // LocalDate.toEpochDay() — one entry per task per calendar day
    var status: EventStatus,
    val timestamp: Long = System.currentTimeMillis()
)
