package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {

    private lateinit var homeRecyclerView: RecyclerView
    private lateinit var planButton: Button
    private lateinit var gptButton: Button
    private lateinit var userUID: String // Поле для хранения UID

    private val projectInfo = listOf(
        HomeInfo("О приложении", isHeader = true),
        HomeInfo("VUZEasyWORK помогает студентам организовывать учебный процесс. Будьте уверены, пользуясь нашим сервисом вы будете успевать все и всегда!"),
        HomeInfo("Ключевые особенности", isHeader = true),
        HomeInfo("1. Создание оптимальных планов занятий, учитывая все потребности пользователя."),
        HomeInfo("2. Интеграция приложения с искусственным интеллектом, для грамотного планирования задач."),
        HomeInfo("3. Лёгкий контроль задач, учебных дисциплин и расписания."),
        HomeInfo("Как использовать приложение?", isHeader = true),
        HomeInfo("1. Перейдите на вкладку Учебный план для создания графика."),
        HomeInfo("2. Воспользуйтесь VUZ_GPT для получения помощи по учебе."),
        HomeInfo("3. Создавайте удобные заметки на странице заметок."),
        HomeInfo("4. Настройте внешний вид приложеия под мебя на вкадке профиль."),
        HomeInfo("FAQ", isHeader = true),
        HomeInfo("1)Для чего использвать 2 разных чата с и скусственным интеллектом?"),
        HomeInfo("В одном чате происходит общение по поводу рабочего плана пользователя, а в другом решене проблем пользователя. Сделано это для того, чтобы не мешать все в один чат."),
        HomeInfo("2)Почему нет функционала для редактирования и удаления заметок?"),
        HomeInfo("Он есть, для редактирования и удаления заметок достаточно просто нажать по созданной заметке и для нее откроется окно редактирования."),
        HomeInfo("3)Когда будет исправлен баг со сменой темы?"),
        HomeInfo("Наша команда активно над этим работает, очень сожалеем что это приносит неудобство нашим пользователям.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Проверка UID
        userUID = intent.getStringExtra("USER_UID") ?: run {
            Toast.makeText(this, "Ошибка: UID пользователя не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        println("Получен UID: $userUID") // Отладочный вывод для проверки

        homeRecyclerView = findViewById(R.id.homeRecyclerView)
        planButton = findViewById(R.id.homePlanButton)
        gptButton = findViewById(R.id.homeGptButton)

        setupRecyclerView()
        setupTopButtons()
        setupNavigationButtons()
    }

    private fun setupRecyclerView() {
        homeRecyclerView.layoutManager = LinearLayoutManager(this)
        homeRecyclerView.adapter = HomeInfoAdapter(projectInfo)
    }

    private fun setupNavigationButtons() {
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            Toast.makeText(this, "Вы уже находитесь на этой странице", Toast.LENGTH_SHORT).show()
        }

        val scheduleButton = findViewById<ImageButton>(R.id.scheduleButton)
        scheduleButton.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            intent.putExtra("USER_UID", userUID)
            startActivity(intent)
        }

        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_UID", userUID)
            startActivity(intent)
        }
    }

    private fun setupTopButtons() {
        planButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER_UID", userUID)
            startActivity(intent)
        }

        gptButton.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra("USER_UID", userUID)
            startActivity(intent)
        }
    }
}
