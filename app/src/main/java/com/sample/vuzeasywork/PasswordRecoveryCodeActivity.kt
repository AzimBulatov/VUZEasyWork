package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasswordRecoveryCodeActivity : AppCompatActivity() {

    private lateinit var codeField: EditText
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery_code)

        codeField = findViewById(R.id.codeField)
        confirmButton = findViewById(R.id.confirmButton)

        confirmButton.setOnClickListener {
            val code = codeField.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Введите код", Toast.LENGTH_SHORT).show()
            } else {
                // Логика подтверждения кода
                startActivity(Intent(this, PasswordRecoveryPassActivity::class.java))
            }
        }
    }
}
