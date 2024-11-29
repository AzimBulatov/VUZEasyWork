package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private var userUID: String? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var monthAdapter: MonthAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Загружаем UID пользователя
        val sharedPreferences = getSharedPreferences("VUZEasyWorkPrefs", MODE_PRIVATE)
        userUID = intent.getStringExtra("USER_UID") ?: sharedPreferences.getString("USER_UID", null)

        if (userUID.isNullOrEmpty()) {
            Toast.makeText(this, "Ошибка: UID пользователя не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Настройка календаря
        viewPager = findViewById(R.id.calendarViewPager)
        setupCalendar()

        // Настройка навигационных кнопок
        setupNavigationButtons()
    }

    private fun setupCalendar() {
        monthAdapter = MonthAdapter { selectedDate ->
            showTasksForDate(selectedDate)
        }
        viewPager.adapter = monthAdapter

        // Устанавливаем начальную позицию на текущий месяц
        viewPager.setCurrentItem(MonthAdapter.START_POSITION, false)
    }

    private fun showTasksForDate(date: String) {
        // Здесь предполагается, что задачи загружаются из базы данных
        val tasks = loadTasksForDate(date)

        if (tasks.isEmpty()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(date)
                .setMessage("Пока что задач на этот день нет")
                .setPositiveButton("OK", null)
                .show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(date)
                .setItems(tasks.toTypedArray(), null)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun loadTasksForDate(date: String): List<String> {
        // Пример задач. В реальности данные нужно загружать из базы Firebase или SQLite
        return when (date) {
            "2024-11-28" -> listOf("Сдать курсовую", "Пройти лекцию по математике")
            else -> emptyList()
        }
    }

    private fun setupNavigationButtons() {
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER_UID", userUID) // Передаем UID
            startActivity(intent)
        }

        val scheduleButton = findViewById<ImageButton>(R.id.scheduleButton)
        scheduleButton.setOnClickListener {
            Toast.makeText(this, "Вы уже находитесь на этой странице", Toast.LENGTH_SHORT).show()
        }

        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_UID", userUID) // Передаем UID
            startActivity(intent)
        }
    }
}
