package com.cuckoo.app

data class ScheduleItem(
    val id: Int,
    var label: String,
    var hour: Int,
    var minute: Int,
    var enabled: Boolean = true
)
