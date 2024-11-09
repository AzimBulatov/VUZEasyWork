package com.sample.vuzeasywork

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasswordRecoveryPassActivity : AppCompatActivity() {

    private lateinit var newPasswordField: EditText
    private lateinit var confirmNewPasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery_pass)

        newPasswordField = findViewById(R.id.newPasswordField)
        confirmNewPasswordButton = findViewById(R.id.confirmNewPasswordButton)

        confirmNewPasswordButton.setOnClickListener {
            val newPassword = newPasswordField.text.toString().trim()
            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Введите новый пароль", Toast.LENGTH_SHORT).show()
            } else {
                // Логика изменения пароля
                Toast.makeText(this, "Пароль изменен", Toast.LENGTH_SHORT).show()
                finish()  // Закрытие после успешного изменения пароля
            }
        }
    }
}
