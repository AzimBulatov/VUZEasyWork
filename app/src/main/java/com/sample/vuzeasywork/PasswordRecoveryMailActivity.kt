package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasswordRecoveryMailActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var sendCodeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery_mail)

        emailField = findViewById(R.id.emailField)
        sendCodeButton = findViewById(R.id.sendCodeButton)

        sendCodeButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show()
            } else {
                // Логика отправки кода на email
                startActivity(Intent(this, PasswordRecoveryCodeActivity::class.java))
            }
        }
    }
}
