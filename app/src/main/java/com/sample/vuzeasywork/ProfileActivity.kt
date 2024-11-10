package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameText: TextView
    private lateinit var themeSwitch: SwitchCompat
    private lateinit var notificationSwitch: SwitchCompat
    private lateinit var locationSwitch: SwitchCompat
    private lateinit var userAddressField: EditText
    private lateinit var universityAddressField: EditText
    private lateinit var saveButton: Button
    private lateinit var editButton: Button
    private lateinit var changePasswordLink: TextView
    private lateinit var logoutLink: TextView

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Инициализация полей
        usernameText = findViewById(R.id.username)
        themeSwitch = findViewById(R.id.themeSwitch)
        notificationSwitch = findViewById(R.id.notificationSwitch)
        locationSwitch = findViewById(R.id.locationSwitch)
        userAddressField = findViewById(R.id.userAddress)
        universityAddressField = findViewById(R.id.universityAddress)
        saveButton = findViewById(R.id.saveButton)
        editButton = findViewById(R.id.editButton)
        changePasswordLink = findViewById(R.id.changePassword)
        logoutLink = findViewById(R.id.logout)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            usernameText.text = user.email
            database = FirebaseDatabase.getInstance().reference.child("users").child(user.uid)

            loadUserData()
            loadSwitchStates()

            themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                AppCompatDelegate.setDefaultNightMode(
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                )
                saveSwitchState("themeSwitch", isChecked)
            }

            notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
                saveSwitchState("notificationSwitch", isChecked)
            }

            locationSwitch.setOnCheckedChangeListener { _, isChecked ->
                saveSwitchState("locationSwitch", isChecked)
            }

            editButton.setOnClickListener {
                userAddressField.isEnabled = true
                universityAddressField.isEnabled = true
            }

            saveButton.setOnClickListener {
                val userAddress = userAddressField.text.toString()
                val universityAddress = universityAddressField.text.toString()

                saveUserAddressToDatabase(userAddress)
                saveUniversityAddressToDatabase(universityAddress)

                userAddressField.isEnabled = false
                universityAddressField.isEnabled = false
            }

            changePasswordLink.setOnClickListener {
                startActivity(Intent(this, PasswordRecoveryMailActivity::class.java))
            }

            logoutLink.setOnClickListener {
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun loadUserData() {
        database.child("userAddress").get().addOnSuccessListener { dataSnapshot ->
            userAddressField.setText(dataSnapshot.value as? String ?: "")
        }
        database.child("universityAddress").get().addOnSuccessListener { dataSnapshot ->
            universityAddressField.setText(dataSnapshot.value as? String ?: "")
        }
    }

    private fun saveUserAddressToDatabase(address: String) {
        database.child("userAddress").setValue(address)
        Toast.makeText(this, "Адрес пользователя сохранен", Toast.LENGTH_SHORT).show()
    }

    private fun saveUniversityAddressToDatabase(address: String) {
        database.child("universityAddress").setValue(address)
        Toast.makeText(this, "Адрес университета сохранен", Toast.LENGTH_SHORT).show()
    }

    private fun saveSwitchState(switchId: String, isChecked: Boolean) {
        val sharedPref = getSharedPreferences("UserSettings", MODE_PRIVATE)
        with (sharedPref.edit()) {
            putBoolean(switchId, isChecked)
            apply()
        }
    }

    private fun loadSwitchStates() {
        val sharedPref = getSharedPreferences("UserSettings", MODE_PRIVATE)
        themeSwitch.isChecked = sharedPref.getBoolean("themeSwitch", false)
        notificationSwitch.isChecked = sharedPref.getBoolean("notificationSwitch", false)
        locationSwitch.isChecked = sharedPref.getBoolean("locationSwitch", false)
    }
}
