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

        NotificationHelper.createChannel(context)
        NotificationHelper.show(context, id, label)
        playSound(context, id)

        // reschedule same item for next day
        val item = ScheduleRepository.getItems(context).find { it.id == id }
        if (item != null && item.enabled) {
            AlarmScheduler.schedule(context, item)
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
            player.isLooping = false
            player.setOnCompletionListener { it.release() }
            player.prepare()
            player.start()

            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.mode = AudioManager.MODE_NORMAL
        } catch (_: Exception) {
            // fail silently, notification sound still plays via channel
        }
    }
}
