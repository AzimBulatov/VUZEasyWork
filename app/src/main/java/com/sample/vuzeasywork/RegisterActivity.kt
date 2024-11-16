package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        val registerSubmitButton: Button = findViewById(R.id.registerSubmitButton)
        val alreadyRegisteredButton: Button = findViewById(R.id.alreadyRegisteredButton)

        auth = FirebaseAuth.getInstance()

        registerSubmitButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password)
        }

        alreadyRegisteredButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserEmailToDatabase(email)
                    Toast.makeText(this, "Пользователь зарегистрирован!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Ошибка регистрации: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserEmailToDatabase(email: String) {
        val userId = auth.currentUser?.uid ?: return
        database.child("users").child(userId).child("email").setValue(email)
    }
}
