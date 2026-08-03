package com.cuckoo.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val label = intent.getStringExtra("label") ?: "Task"
        val endHour = intent.getIntExtra("endHour", -1)
        val endMinute = intent.getIntExtra("endMinute", -1)

        NotificationHelper.createChannel(context)
        NotificationHelper.show(context, id, label)
        playSound(context, id)

        // schedule end alarm to fire when task duration is over
        if (endHour >= 0) {
            scheduleEndAlarm(context, id, label, endHour, endMinute)
        }

        // reschedule same item for next day
        val item = ScheduleRepository.getItems(context).find { it.id == id }
        if (item != null && item.enabled) {
            AlarmScheduler.schedule(context, item)
        }
    }

    private fun scheduleEndAlarm(context: Context, id: Int, label: String, endHour: Int, endMinute: Int) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, endHour)
            set(java.util.Calendar.MINUTE, endMinute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, TaskEndReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("label", label)
        }
        val pi = android.app.PendingIntent.getBroadcast(
            context,
            id + 1000,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && am.canScheduleExactAlarms()) {
            am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
        } else {
            am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
        }
    }

    private fun getRingtone(id: Int): Int = when (id) {
        1 -> R.raw.math
        2 -> R.raw.chores
        3 -> R.raw.cpp
        4 -> R.raw.kotlin
        5 -> R.raw.electronics
        6 -> R.raw.electromagnetism
        7 -> R.raw.review
        else -> R.raw.math
    }

    private fun playSound(context: Context, id: Int) {
        try {
            val uri = android.net.Uri.parse("android.resource://${context.packageName}/${getRingtone(id)}")

            val player = MediaPlayer()
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            player.setDataSource(context, uri)
            player.isLooping = true
            player.setOnCompletionListener { it.release() }
            player.prepare()
            player.start()
            MediaPlayerHolder.set(player)

            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.mode = AudioManager.MODE_NORMAL
        } catch (_: Exception) {
            // fail silently, notification sound still plays via channel
        }
    }
}
