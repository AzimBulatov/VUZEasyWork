// Fixed CalendarAdapter.kt
package com.sample.vuzeasywork

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private val onDateClick: (String) -> Unit) :
    RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val days: List<Date> = generateDaysForMonth()
    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val notesMap = mutableMapOf<String, Boolean>() // Map to track dates with notes

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(day)

        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        holder.dayText.text = dayFormat.format(day)

        // Highlight current day
        if (formattedDate == currentDate) {
            holder.dayText.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.purple_200)
            )
            holder.dayText.setTextColor(Color.WHITE)
        } else {
            holder.dayText.setBackgroundColor(Color.TRANSPARENT)
            holder.dayText.setTextColor(Color.BLACK)
        }

        // Check for notes and show an indicator
        if (notesMap[formattedDate] == true) {
            holder.dayText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_note_indicator, 0)
        } else {
            holder.dayText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        }

        // Handle click events
        holder.itemView.setOnClickListener {
            onDateClick(formattedDate)
        }
    }

    override fun getItemCount(): Int = days.size

    private fun generateDaysForMonth(): List<Date> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val days = mutableListOf<Date>()
        for (i in 0 until daysInMonth) {
            days.add(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    fun loadNotesForMonth(userUID: String) {
        val database = FirebaseDatabase.getInstance().reference
        val notesRef = database.child("users").child(userUID).child("notes")

        notesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notesMap.clear()
                snapshot.children.forEach { dateSnapshot ->
                    val date = dateSnapshot.key ?: return@forEach
                    notesMap[date] = true // Mark this date as having notes
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
    }
}
