package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        val loginSubmitButton: Button = findViewById(R.id.loginSubmitButton)
        val notRegisteredButton: Button = findViewById(R.id.notRegisteredButton)
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)

        auth = FirebaseAuth.getInstance()

        // Обработка нажатия на кнопку входа
        loginSubmitButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        // Обработка перехода на регистрацию
        notRegisteredButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Обработка перехода на восстановление пароля
        forgotPasswordText.setOnClickListener {
            startActivity(Intent(this, PasswordRecoveryMailActivity::class.java))
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userUID = currentUser.uid // Получаем UID пользователя
                        Toast.makeText(this, "Вход выполнен!", Toast.LENGTH_SHORT).show()

                        // Передаем UID в ChatActivity
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("USER_UID", userUID) // Добавляем UID в Intent
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Ошибка: текущий пользователь не найден", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Ошибка входа: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
