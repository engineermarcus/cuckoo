package com.cuckoo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScheduleAdapter(
    private val items: MutableList<ScheduleItem>,
    private val onClick: (ScheduleItem) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.textLabel)
        val time: TextView = view.findViewById(R.id.textTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.label.text = item.label
        holder.time.text = String.format("%02d:%02d", item.hour, item.minute)
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
