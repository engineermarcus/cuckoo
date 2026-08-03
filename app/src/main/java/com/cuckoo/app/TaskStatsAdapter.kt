package com.cuckoo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskStatsAdapter(
    private val stats: List<Triple<String, Int, Int>>
) : RecyclerView.Adapter<TaskStatsAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val label  : TextView = view.findViewById(R.id.textStatLabel)
        val done   : TextView = view.findViewById(R.id.textStatDone)
        val missed : TextView = view.findViewById(R.id.textStatMissed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_task_stat, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (label, done, missed) = stats[position]
        holder.label.text  = label
        holder.done.text   = "✅ $done"
        holder.missed.text = "❌ $missed"
    }

    override fun getItemCount() = stats.size
}
