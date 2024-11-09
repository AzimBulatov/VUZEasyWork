package com.sample.vuzeasywork

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ScheduleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Логика загрузки и отображения графика дел
        Toast.makeText(this, "График дел загружен", Toast.LENGTH_SHORT).show()
    }
}
