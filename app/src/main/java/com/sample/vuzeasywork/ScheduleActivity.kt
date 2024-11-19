package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ScheduleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Логика загрузки и отображения графика дел
        Toast.makeText(this, "График дел пока загружен", Toast.LENGTH_SHORT).show()

        // Связываем кнопки навигации с действиями
        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        // Главная страница (ChatActivity)
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        // График дел (ScheduleActivity) — остаемся на текущей странице
        val scheduleButton = findViewById<ImageButton>(R.id.scheduleButton)
        scheduleButton.setOnClickListener {
            Toast.makeText(this, "Вы уже находитесь на этой странице", Toast.LENGTH_SHORT).show()
        }

        // Профиль (ProfileActivity)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
