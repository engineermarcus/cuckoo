package com.cuckoo.app

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.cuckoo.app.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ScheduleAdapter
    private lateinit var items: MutableList<ScheduleItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createChannel(this)
        requestPermissionsIfNeeded()

        items = ScheduleRepository.getItems(this)
        adapter = ScheduleAdapter(
            items,
            onToggle = { item, enabled ->
                item.enabled = enabled
                persistAndReschedule()
            },
            onClick = { item -> showTimePicker(item) },
            onDelete = { item -> deleteItem(item) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener { showAddDialog() }

        AlarmScheduler.rescheduleAll(this)
    }

    private fun requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }

    private fun showTimePicker(item: ScheduleItem) {
        TimePickerDialog(this, { _, hour, minute ->
            item.hour = hour
            item.minute = minute
            adapter.notifyDataSetChanged()
            persistAndReschedule()
        }, item.hour, item.minute, true).show()
    }

    private fun showAddDialog() {
        val input = TextInputEditText(this)
        input.hint = "Task name"
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("New schedule item")
            .setView(input)
            .setPositiveButton("Next") { _, _ ->
                val label = input.text?.toString()?.trim().orEmpty().ifEmpty { "Task" }
                TimePickerDialog(this, { _, hour, minute ->
                    val newItem = ScheduleItem(
                        ScheduleRepository.nextId(this), label, hour, minute
                    )
                    items.add(newItem)
                    adapter.notifyItemInserted(items.size - 1)
                    persistAndReschedule()
                }, 12, 0, true).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteItem(item: ScheduleItem) {
        AlarmScheduler.cancel(this, item)
        val index = items.indexOf(item)
        items.remove(item)
        if (index >= 0) adapter.notifyItemRemoved(index)
        ScheduleRepository.saveItems(this, items)
    }

    private fun persistAndReschedule() {
        ScheduleRepository.saveItems(this, items)
        items.forEach { AlarmScheduler.schedule(this, it) }
    }
}
