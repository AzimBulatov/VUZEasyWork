package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        val registerSubmitButton: Button = findViewById(R.id.registerSubmitButton)
        val alreadyRegisteredButton: Button = findViewById(R.id.alreadyRegisteredButton)

        database = FirebaseDatabase.getInstance().getReference("users")

        registerSubmitButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkIfUserExists(email) { exists ->
                if (exists) {
                    Toast.makeText(this, "Такой пользователь уже зарегистрирован", Toast.LENGTH_SHORT).show()
                } else {
                    registerUser(email, password)
                }
            }
        }

        alreadyRegisteredButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun checkIfUserExists(email: String, callback: (Boolean) -> Unit) {
        val query = database.orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegisterActivity, "Ошибка проверки пользователя", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun registerUser(email: String, password: String) {
        val userId = database.push().key
        if (userId != null) {
            val user = User(email, password)
            database.child(userId).setValue(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Пользователь зарегистрирован!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

data class User(val email: String, val password: String)
