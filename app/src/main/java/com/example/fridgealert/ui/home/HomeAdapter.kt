package com.example.fridgealert.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgealert.R
import com.example.fridgealert.data.item
import java.text.SimpleDateFormat
import java.util.*

class HomeAdapter(
    private val items: List<item>,
    private val onClick: (item) -> Unit
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.item_texts)
        val itemCategory: TextView = itemView.findViewById(R.id.item_category)
        val itemExpired: TextView = itemView.findViewById(R.id.item_expired)
        val itemQuantity: TextView = itemView.findViewById(R.id.item_quantity)
        val cardView: CardView = itemView as CardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        holder.textView.text = currentItem.name
        holder.itemCategory.text = currentItem.category
        holder.itemExpired.text = currentItem.expDate
        holder.itemQuantity.text = currentItem.quantity.toString()

        // ✅ เปลี่ยนสีการ์ดตามวันหมดอายุ
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val expDate = try { sdf.parse(currentItem.expDate ?: "") } catch (e: Exception) { null }
        val today = Calendar.getInstance().time

        if (expDate != null) {
            val diff = (expDate.time - today.time) / (1000 * 60 * 60 * 24)
            when {
                diff <= 0 -> {
                    holder.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, android.R.color.holo_red_light)
                    )
                }
                diff == 1L -> {
                    holder.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, android.R.color.holo_orange_light)
                    )
                }
                else -> {
                    holder.cardView.setCardBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, android.R.color.white)
                    )
                }
            }
        }

        holder.itemView.setOnClickListener { onClick(currentItem) }
    }

    override fun getItemCount(): Int = items.size
}
