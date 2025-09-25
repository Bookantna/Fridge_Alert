package com.example.fridgealert.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgealert.R
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val title: String,
    val message: String,
    val timestamp: Date
)

class NotificationAdapter(private val notifications: List<NotificationItem>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.notificationTitle)
        val message: TextView = view.findViewById(R.id.notificationMessage)
        val time: TextView = view.findViewById(R.id.notificationTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = notifications[position]
        holder.title.text = item.title
        holder.message.text = item.message

        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.time.text = format.format(item.timestamp)
    }

    override fun getItemCount(): Int = notifications.size
}