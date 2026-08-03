package com.cuckoo.app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_START = "com.cuckoo.app.ACTION_START"
        const val ACTION_DONE = "com.cuckoo.app.ACTION_DONE"
        const val ACTION_SKIP = "com.cuckoo.app.ACTION_SKIP"
        const val EXTRA_ID = "id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID, -1)
        if (id == -1) return

        val status = when (intent.action) {
            ACTION_START -> EventStatus.STARTED
            ACTION_DONE -> EventStatus.DONE
            ACTION_SKIP -> EventStatus.SKIPPED
            else -> return
        }

        EventRepository.record(context, id, status)

        // Dismiss the notification, unless it was just "Start" (task still running)
        if (status != EventStatus.STARTED) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(id)
        }
    }
}
