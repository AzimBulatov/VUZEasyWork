package com.sample.vuzeasywork

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameText: TextView
    private lateinit var phoneText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        usernameText = findViewById(R.id.username)
        phoneText = findViewById(R.id.phone)

        // Здесь должна быть логика загрузки информации о профиле пользователя
        usernameText.text = "Имя пользователя"
        phoneText.text = "Телефон"

        // Для демонстрации обновления профиля
        Toast.makeText(this, "Информация о профиле загружена", Toast.LENGTH_SHORT).show()
    }
}
