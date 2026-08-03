package com.cuckoo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScreenTimeAdapter(
    private val items: List<Pair<String, String>>
) : RecyclerView.Adapter<ScreenTimeAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val appName : TextView = view.findViewById(R.id.textAppName)
        val time    : TextView = view.findViewById(R.id.textAppTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_screen_time, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.appName.text = items[position].first
        holder.time.text    = items[position].second
    }

    override fun getItemCount() = items.size
}
