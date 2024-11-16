package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class PasswordRecoveryPassActivity : AppCompatActivity() {

    private lateinit var newPasswordField: EditText
    private lateinit var confirmNewPasswordButton: Button
    private val auth = FirebaseAuth.getInstance()
    private var userUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery_pass)

        newPasswordField = findViewById(R.id.newPasswordField)
        confirmNewPasswordButton = findViewById(R.id.confirmNewPasswordButton)

        // Получаем userUID из Intent
        userUID = intent.getStringExtra("USER_UID")

        if (userUID.isNullOrEmpty()) {
            Toast.makeText(this, "Ошибка: пользователь не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        confirmNewPasswordButton.setOnClickListener {
            val newPassword = newPasswordField.text.toString().trim()
            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Введите новый пароль", Toast.LENGTH_SHORT).show()
            } else {
                updatePassword(newPassword)
            }
        }
    }

    private fun updatePassword(newPassword: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Обновляем пароль напрямую
            currentUser.updatePassword(newPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Пароль успешно обновлен", Toast.LENGTH_SHORT).show()
                        navigateToLogin()
                    } else {
                        Toast.makeText(this, "Ошибка обновления пароля: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Ошибка обновления пароля: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Ошибка: текущий пользователь не найден", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
