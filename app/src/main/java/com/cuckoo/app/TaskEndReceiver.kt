package com.cuckoo.app

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskEndReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        val label = intent.getStringExtra("label") ?: "Task"
        if (id == -1) return

        // stop the ringtone
        MediaPlayerHolder.stop()

        // dismiss the ongoing notification
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(id)

        // check if task was already marked done/started
        val event = EventRepository.eventFor(context, id, java.time.LocalDate.now().toEpochDay())
        if (event != null && event.status == EventStatus.DONE) return

        // open the "Mark as Done?" activity
        val reviewIntent = Intent(context, TaskReviewActivity::class.java).apply {
            putExtra("id", id)
            putExtra("label", label)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(reviewIntent)
    }
}
