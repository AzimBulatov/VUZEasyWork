package com.sample.vuzeasywork

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sample.vuzeasywork.R
import java.util.*

class MonthAdapter(private val onDateClick: (String) -> Unit) :
    RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

    companion object {
        const val START_POSITION = Int.MAX_VALUE / 2 // Начальная позиция для текущего месяца

        // Названия месяцев в именительном падеже
        val MONTH_NAMES = listOf(
            "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"
        )
    }

    private var userUID: String? = null // UID пользователя

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_month, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, position - START_POSITION)

        // Получаем название месяца в именительном падеже
        val monthName = MONTH_NAMES[calendar.get(Calendar.MONTH)]
        val year = calendar.get(Calendar.YEAR)
        holder.monthTitle.text = "$monthName $year"

        // Генерация дней для отображения полного месяца
        val daysInMonth = generateDaysInMonth(calendar)
        val dayAdapter = DayAdapter(daysInMonth, onDateClick)
        holder.daysRecyclerView.layoutManager = GridLayoutManager(holder.itemView.context, 7)
        holder.daysRecyclerView.adapter = dayAdapter
    }

    override fun getItemCount(): Int = Int.MAX_VALUE

    fun setUserUID(uid: String) {
        this.userUID = uid
        notifyDataSetChanged()
    }

    private fun generateDaysInMonth(calendar: Calendar): List<Date> {
        val days = mutableListOf<Date>()

        // Устанавливаем первый день месяца
        val monthStart = calendar.clone() as Calendar
        monthStart.set(Calendar.DAY_OF_MONTH, 1)

        // Находим день недели первого дня месяца (1 - воскресенье, 2 - понедельник и т.д.)
        val firstDayOfWeek = monthStart.get(Calendar.DAY_OF_WEEK)

        // Сдвигаем календарь назад на количество дней до понедельника
        val offset = if (firstDayOfWeek == Calendar.SUNDAY) 6 else firstDayOfWeek - 2
        monthStart.add(Calendar.DAY_OF_MONTH, -offset)

        // Добавляем 42 дня (6 строк по 7 дней)
        for (i in 0 until 42) {
            days.add(monthStart.time)
            monthStart.add(Calendar.DAY_OF_MONTH, 1)
        }

        return days
    }

    class MonthViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val monthTitle: TextView = itemView.findViewById(R.id.monthTitle)
        val daysRecyclerView: RecyclerView = itemView.findViewById(R.id.daysRecyclerView)
    }
}
