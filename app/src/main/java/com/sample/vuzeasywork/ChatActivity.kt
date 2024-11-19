package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Связываем кнопки навигации с действиями
        setupNavigationButtons()
    }

    private fun setupNavigationButtons() {
        // Главная страница (ChatActivity) — остаемся на текущей странице
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            Toast.makeText(this, "Вы уже находитесь на этой странице", Toast.LENGTH_SHORT).show()
        }

        // График дел (ScheduleActivity)
        val scheduleButton = findViewById<ImageButton>(R.id.scheduleButton)
        scheduleButton.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            startActivity(intent)
        }

        // Профиль (ProfileActivity)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}
