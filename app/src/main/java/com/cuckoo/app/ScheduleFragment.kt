package com.cuckoo.app

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScheduleFragment : Fragment() {

    private lateinit var items: MutableList<ScheduleItem>
    private lateinit var adapter: ScheduleAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        items = ScheduleRepository.getItems(requireContext())
        adapter = ScheduleAdapter(items) { item -> showTimePicker(item) }

        view.findViewById<RecyclerView>(R.id.recyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ScheduleFragment.adapter
        }

        AlarmScheduler.rescheduleAll(requireContext())
    }

    private fun showTimePicker(item: ScheduleItem) {
        TimePickerDialog(requireContext(), { _, hour, minute ->
            item.hour = hour
            item.minute = minute
            adapter.notifyDataSetChanged()
            ScheduleRepository.saveItems(requireContext(), items)
            AlarmScheduler.rescheduleAll(requireContext())
        }, item.hour, item.minute, true).show()
    }
}
