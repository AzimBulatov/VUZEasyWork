package com.sample.vuzeasywork

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private var userUID: String? = null
    private lateinit var viewPager: ViewPager2
    private lateinit var monthAdapter: MonthAdapter
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        // Загружаем UID пользователя
        val sharedPreferences = getSharedPreferences("VUZEasyWorkPrefs", MODE_PRIVATE)
        userUID = intent.getStringExtra("USER_UID") ?: sharedPreferences.getString("USER_UID", null)

        if (userUID.isNullOrEmpty()) {
            Toast.makeText(this, "Ошибка: UID пользователя не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Настройка календаря
        viewPager = findViewById(R.id.calendarViewPager)
        setupCalendar()

        // Настройка навигационных кнопок
        setupNavigationButtons()
        setupTopButtons()
    }

    private fun setupCalendar() {
        monthAdapter = MonthAdapter { selectedDate ->
            showTasksForDate(selectedDate)
        }
        viewPager.adapter = monthAdapter

        // Устанавливаем начальную позицию на текущий месяц
        viewPager.setCurrentItem(MonthAdapter.START_POSITION, false)
    }

    private fun showTasksForDate(date: String) {
        database.child("users").child(userUID!!).child("notes").child(date)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Pair<String, String>>()
                    snapshot.children.forEach {
                        val taskId = it.key ?: ""
                        val taskText = it.getValue(String::class.java) ?: ""
                        tasks.add(Pair(taskId, taskText))
                    }

                    if (tasks.isEmpty()) {
                        MaterialAlertDialogBuilder(this@ScheduleActivity)
                            .setTitle(date)
                            .setMessage("Пока что задач на этот день нет")
                            .setPositiveButton("Добавить заметку") { _, _ ->
                                openNoteDialog(date)
                            }
                            .setNegativeButton("Отмена", null)
                            .show()
                    } else {
                        val taskTexts = tasks.map { it.second }.toTypedArray()
                        MaterialAlertDialogBuilder(this@ScheduleActivity)
                            .setTitle(date)
                            .setItems(taskTexts) { _, which ->
                                val selectedTask = tasks[which]
                                openEditNoteDialog(date, selectedTask.first, selectedTask.second)
                            }
                            .setPositiveButton("Добавить заметку") { _, _ ->
                                openNoteDialog(date)
                            }
                            .setNegativeButton("Отмена", null)
                            .show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ScheduleActivity, "Ошибка загрузки задач", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun openNoteDialog(date: String) {
        val noteInput = EditText(this).apply {
            setBackgroundResource(R.drawable.rounded_edittext_tasker)
            setPadding(32, 16, 32, 16)
            setHintTextColor(ContextCompat.getColor(context, R.color.hint_color))
            setTextColor(ContextCompat.getColor(context, R.color.dialog_text_color))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)

            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 16
                )
            }

            addView(spacer)
            addView(noteInput)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Добавить заметку на $date")
            .setView(container)
            .setPositiveButton("Сохранить") { _, _ ->
                val note = noteInput.text.toString().trim()
                if (note.isNotEmpty()) {
                    saveNoteToFirebase(date, note)
                } else {
                    Toast.makeText(this, "Заметка не может быть пустой", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openEditNoteDialog(date: String, taskId: String, currentNote: String) {
        val noteInput = EditText(this).apply {
            setBackgroundResource(R.drawable.rounded_edittext_tasker)
            setPadding(32, 16, 32, 16)
            setHintTextColor(ContextCompat.getColor(context, R.color.hint_color))
            setTextColor(ContextCompat.getColor(context, R.color.dialog_text_color))
            setText(currentNote)
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)

            val spacer = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 16
                )
            }

            addView(spacer)
            addView(noteInput)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Редактировать заметку")
            .setView(container)
            .setPositiveButton("Сохранить") { _, _ ->
                val updatedNote = noteInput.text.toString().trim()
                if (updatedNote.isNotEmpty()) {
                    updateNoteInFirebase(date, taskId, updatedNote)
                } else {
                    Toast.makeText(this, "Заметка не может быть пустой", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Удалить") { _, _ ->
                deleteNoteFromFirebase(date, taskId)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun saveNoteToFirebase(date: String, note: String) {
        val notesRef = database.child("users").child(userUID!!).child("notes").child(date)
        val newNoteRef = notesRef.push()
        newNoteRef.setValue(note).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Заметка сохранена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка сохранения заметки", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateNoteInFirebase(date: String, taskId: String, updatedNote: String) {
        val noteRef = database.child("users").child(userUID!!).child("notes").child(date).child(taskId)
        noteRef.setValue(updatedNote).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Заметка обновлена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка обновления заметки", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteNoteFromFirebase(date: String, taskId: String) {
        val noteRef = database.child("users").child(userUID!!).child("notes").child(date).child(taskId)
        noteRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Заметка удалена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка удаления заметки", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigationButtons() {
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USER_UID", userUID) // Передаем UID
            startActivity(intent)
        }

        val scheduleButton = findViewById<ImageButton>(R.id.scheduleButton)
        scheduleButton.setOnClickListener {
            Toast.makeText(this, "Вы уже находитесь на этой странице", Toast.LENGTH_SHORT).show()
        }

        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_UID", userUID) // Передаем UID
            startActivity(intent)
        }
    }

    private fun setupTopButtons() {
        val planButton = findViewById<Button>(R.id.planButton)
        planButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER_UID", userUID) // Передаем UID пользователя
            startActivity(intent)
        }

        val gptButton = findViewById<Button>(R.id.gptButton)
        gptButton.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra("USER_UID", userUID) // Передаем UID пользователя
            startActivity(intent)
        }
    }
}
