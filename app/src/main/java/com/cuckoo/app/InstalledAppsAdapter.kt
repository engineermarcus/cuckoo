package com.cuckoo.app

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    var checked: Boolean
)

class InstalledAppsAdapter(
    private val apps: List<AppEntry>
) : RecyclerView.Adapter<InstalledAppsAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.imgAppIcon)
        val label: TextView = view.findViewById(R.id.textAppLabel)
        val check: CheckBox = view.findViewById(R.id.checkAllowed)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_installed_app, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.label.text = app.label
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = app.checked
        holder.check.setOnCheckedChangeListener { _, isChecked -> app.checked = isChecked }
        holder.itemView.setOnClickListener { holder.check.toggle() }
    }

    override fun getItemCount() = apps.size
}
