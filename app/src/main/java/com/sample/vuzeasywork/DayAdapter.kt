package com.sample.vuzeasywork

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.sample.vuzeasywork.R
import java.text.SimpleDateFormat
import java.util.*

class DayAdapter(
    private val days: List<Date>,
    private val onDateClick: (String) -> Unit
) : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val date = days[position]
        val dayCalendar = Calendar.getInstance().apply { time = date }
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)

        // Устанавливаем текст для ячейки
        holder.dayText.text = dayFormat.format(date)

        // Проверяем, принадлежит ли день текущему месяцу
        val isCurrentMonth = dayCalendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)

        // Цвет для дней текущего и соседних месяцев
        if (isCurrentMonth) {
            holder.dayText.setTextColor(Color.BLACK) // Текущий месяц
        } else {
            holder.dayText.setTextColor(Color.LTGRAY) // Соседние месяцы
        }

        // Выделяем текущий день
        if (formattedDate == currentDate) {
            holder.dayText.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.teal_200)
            )
        } else {
            holder.dayText.setBackgroundColor(Color.TRANSPARENT)
        }

        // Добавляем обработчик клика
        holder.itemView.setOnClickListener {
            onDateClick(formattedDate)
        }
    }

    override fun getItemCount(): Int = days.size

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
    }
}
