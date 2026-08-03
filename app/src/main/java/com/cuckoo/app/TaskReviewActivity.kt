package com.cuckoo.app

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate

class TaskReviewActivity : AppCompatActivity() {

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_review)

        val id = intent.getIntExtra("id", -1)
        val label = intent.getStringExtra("label") ?: "Task"

        val textTitle = findViewById<TextView>(R.id.textReviewTitle)
        val textCountdown = findViewById<TextView>(R.id.textCountdown)
        val btnDone = findViewById<Button>(R.id.btnMarkDone)
        val btnMissed = findViewById<Button>(R.id.btnMarkMissed)

        textTitle.text = "Did you complete $label?"

        // 60 second window to mark done, else auto-missed
        countDownTimer = object : CountDownTimer(60_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                textCountdown.text = "Auto-closing in ${millisUntilFinished / 1000}s"
            }
            override fun onFinish() {
                markMissed(id)
            }
        }.start()

        btnDone.setOnClickListener {
            countDownTimer?.cancel()
            EventRepository.record(this, id, EventStatus.DONE, LocalDate.now().toEpochDay())
            finish()
        }

        btnMissed.setOnClickListener {
            countDownTimer?.cancel()
            markMissed(id)
        }
    }

    private fun markMissed(id: Int) {
        EventRepository.record(this, id, EventStatus.MISSED, LocalDate.now().toEpochDay())
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
