package com.sample.vuzeasywork

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

        // Логика загрузки и отображения списка задач
        Toast.makeText(this, "Список задач загружен", Toast.LENGTH_SHORT).show()
    }
}
