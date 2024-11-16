package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

class PasswordRecoveryCodeActivity : AppCompatActivity() {

    private lateinit var codeField: EditText
    private lateinit var confirmButton: Button
    private val database = FirebaseDatabase.getInstance().reference
    private var email: String? = null
    private var userUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery_code)

        codeField = findViewById(R.id.codeField)
        confirmButton = findViewById(R.id.confirmButton)

        // Получаем email и UID из Intent
        email = intent.getStringExtra("EMAIL") // Обновлено для согласованности
        userUID = intent.getStringExtra("USER_UID") // Обновлено для согласованности

        if (email.isNullOrEmpty() || userUID.isNullOrEmpty()) {
            Toast.makeText(this, "Ошибка: данные не найдены", Toast.LENGTH_SHORT).show()
            finish() // Закрываем активность, если данные отсутствуют
            return
        }

        confirmButton.setOnClickListener {
            val code = codeField.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Введите код", Toast.LENGTH_SHORT).show()
            } else {
                verifyCode(code)
            }
        }
    }

    private fun verifyCode(enteredCode: String) {
        // Ссылка на пользователя в базе данных
        val userRef = database.child("users").child(userUID!!)
        userRef.child("recoveryCode").get().addOnSuccessListener { snapshot ->
            val storedCode = snapshot.getValue(String::class.java)

            if (storedCode != null && storedCode == enteredCode) {
                // Удаляем код после успешной проверки
                userRef.child("recoveryCode").removeValue()

                // Навигация на PasswordRecoveryPassActivity
                navigateToPasswordChange()
            } else {
                Toast.makeText(this, "Неверный код восстановления", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка проверки кода", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToPasswordChange() {
        val intent = Intent(this, PasswordRecoveryPassActivity::class.java)
        intent.putExtra("EMAIL", email) // Передача email
        intent.putExtra("USER_UID", userUID) // Передача UID
        startActivity(intent)
        finish()
    }
}
