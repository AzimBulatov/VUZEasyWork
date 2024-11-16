package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class PasswordRecoveryMailActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var sendCodeButton: Button
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_recovery_mail)

        emailField = findViewById(R.id.emailField)
        sendCodeButton = findViewById(R.id.sendCodeButton)

        sendCodeButton.setOnClickListener {
            val email = emailField.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Введите email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверяем email в базе данных
            checkEmailInDatabase(email)
        }
    }

    private fun checkEmailInDatabase(email: String) {
        database.child("users").get().addOnSuccessListener { snapshot ->
            var userFound = false
            for (userSnapshot in snapshot.children) {
                val registeredEmail = userSnapshot.child("email").getValue(String::class.java)
                if (registeredEmail == email) {
                    val userUID = userSnapshot.key

                    userFound = true
                    val verificationCode = generateVerificationCode()

                    // Сохраняем код в базе данных
                    saveCodeToDatabase(userUID!!, verificationCode)

                    // Отправляем email с кодом
                    sendEmailInBackground(email, verificationCode)

                    // Переходим к следующему экрану
                    val intent = Intent(this, PasswordRecoveryCodeActivity::class.java)
                    intent.putExtra("USER_UID", userUID)
                    intent.putExtra("EMAIL", email)
                    startActivity(intent)
                    break
                }
            }
            if (!userFound) {
                Toast.makeText(this, "Пользователь с таким email не найден", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка доступа к базе данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }

    private fun saveCodeToDatabase(userId: String, code: String) {
        database.child("users").child(userId).child("recoveryCode").setValue(code)
    }

    private fun sendEmailInBackground(email: String, code: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                sendEmailWithSMTP(email, code)
                runOnUiThread {
                    Toast.makeText(this@PasswordRecoveryMailActivity, "Код отправлен на почту", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@PasswordRecoveryMailActivity, "Ошибка отправки email", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendEmailWithSMTP(email: String, code: String) {
        val smtpHost = "smtp.gmail.com"
        val smtpPort = "587"
        val smtpUsername = "azimbulatov21@gmail.com"
        val smtpPassword = "bavz oegw lzwp pdud"

        val properties = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort)
        }

        val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(smtpUsername, smtpPassword)
            }
        })

        val message = MimeMessage(session).apply {
            setFrom(InternetAddress(smtpUsername))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(email))
            subject = "Код восстановления пароля"
            setText("Ваш код восстановления пароля: $code")
        }

        Transport.send(message)
    }
}
