package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        val loginSubmitButton: Button = findViewById(R.id.loginSubmitButton)
        val notRegisteredButton: Button = findViewById(R.id.notRegisteredButton)

        database = FirebaseDatabase.getInstance().getReference("users")

        loginSubmitButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Не все поля заполнены", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authenticateUser(email, password)
        }

        notRegisteredButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun authenticateUser(email: String, password: String) {
        val query = database.orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val dbPassword = userSnapshot.child("password").value.toString()
                        if (dbPassword == password) {
                            Toast.makeText(this@LoginActivity, "Вход выполнен!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, ChatActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Неверный пароль", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "Неизвестный пользователь, зарегистрируйтесь", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Ошибка проверки пользователя", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
