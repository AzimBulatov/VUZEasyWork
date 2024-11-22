package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var messageField: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var welcomeMessage: TextView
    private lateinit var chatLogo: ImageView

    private val messages = mutableListOf<Message>()
    private val database = FirebaseDatabase.getInstance().reference
    private var userUID: String? = null
    private var chatStage = 0
    private var totalSubjects: Int = 0
    private var remainingSubjects: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val sharedPreferences = getSharedPreferences("VUZEasyWorkPrefs", MODE_PRIVATE)
        userUID = intent.getStringExtra("USER_UID") ?: sharedPreferences.getString("USER_UID", null)

        if (userUID.isNullOrEmpty()) {
            Toast.makeText(this, "Ошибка: UID пользователя не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sharedPreferences.edit().putString("USER_UID", userUID).apply()

        // Инициализация компонентов
        messageField = findViewById(R.id.messageField)
        sendButton = findViewById(R.id.sendButton)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        welcomeMessage = findViewById(R.id.welcomeMessage)
        chatLogo = findViewById(R.id.chatLogo)

        // Настройка RecyclerView
        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter

        loadChatState()
        loadMessages()

        // Обработчик кнопки отправки
        sendButton.setOnClickListener {
            val messageText = messageField.text.toString().trim()
            if (messageText.isNotEmpty()) {
                if (welcomeMessage.visibility == View.VISIBLE) {
                    welcomeMessage.visibility = View.GONE
                    chatLogo.visibility = View.GONE
                }
                sendUserMessage(messageText)
                messageField.text.clear()
            }
        }

        setupNavigationButtons()
    }

    private fun loadChatState() {
        database.child("users").child(userUID!!).child("chatState")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatStage = snapshot.child("chatStage").getValue(Int::class.java) ?: 0
                    totalSubjects = snapshot.child("totalSubjects").getValue(Int::class.java) ?: 0
                    remainingSubjects = snapshot.child("remainingSubjects").getValue(Int::class.java) ?: 0

                    val isWelcomeMessageVisible = snapshot.child("welcomeMessageVisible")
                        .getValue(Boolean::class.java) ?: true
                    welcomeMessage.visibility = if (isWelcomeMessageVisible) View.VISIBLE else View.GONE
                    chatLogo.visibility = if (isWelcomeMessageVisible) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Ошибка загрузки состояния чата", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadMessages() {
        database.child("users").child(userUID!!).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages.clear()
                    snapshot.children.forEach { data ->
                        val map = data.value as? Map<String, Any?>
                        if (map != null) {
                            messages.add(Message.fromMap(map))
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(messages.size - 1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Ошибка загрузки сообщений", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveChatState() {
        val chatState = mapOf(
            "chatStage" to chatStage,
            "totalSubjects" to totalSubjects,
            "remainingSubjects" to remainingSubjects,
            "welcomeMessageVisible" to (welcomeMessage.visibility == View.VISIBLE)
        )
        database.child("users").child(userUID!!).child("chatState").setValue(chatState)
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка сохранения состояния чата", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendUserMessage(text: String) {
        val userMessage = Message(
            text = text,
            isUser = true,
            style = "default"
        )
        saveMessage(userMessage)
        handleChatScenario(text)
    }

    private fun sendChatMessage(text: String, isWelcome: Boolean = false) {
        val chatMessage = Message(
            text = text,
            isUser = false,
            style = "default"
        )
        saveMessage(chatMessage)

        if (isWelcome) {
            database.child("users").child(userUID!!).child("chatState").child("welcomeMessageVisible")
                .setValue(false)
        }
    }

    private fun saveMessage(message: Message) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)
        database.child("users").child(userUID!!).child("messages").push().setValue(message)
    }

    private fun handleChatScenario(userMessage: String) {
        when (chatStage) {
            0 -> {
                if (containsAnyWord(userMessage, listOf("студент", "преподаватель"))) {
                    sendChatMessage("Введите количество ваших дисциплин (напишите число).")
                    chatStage = 1
                } else {
                    sendChatMessage("Пожалуйста, уточните: вы студент или преподаватель?")
                }
            }
            1 -> {
                val numberOfSubjects = userMessage.toIntOrNull()
                if (numberOfSubjects != null && numberOfSubjects > 0) {
                    totalSubjects = numberOfSubjects
                    remainingSubjects = numberOfSubjects
                    sendChatMessage("Введите название дисциплины и количество заданий по ней.")
                    chatStage = 2
                } else {
                    sendChatMessage("Введите корректное количество дисциплин.")
                }
            }
            2 -> {
                if (remainingSubjects > 0) {
                    remainingSubjects--
                    if (remainingSubjects > 0) {
                        sendChatMessage("Введите следующую дисциплину и количество заданий по ней.")
                    } else {
                        sendChatMessage("Занимаетесь ли вы чем-то дополнительно? (да/нет)")
                        chatStage = 3
                    }
                }
                saveChatState()
            }
            3 -> {
                if (containsAnyWord(userMessage, listOf("да", "нет"))) {
                    if (userMessage.equals("да", ignoreCase = true)) {
                        sendChatMessage("Опишите занятия, их время и дни.")
                        chatStage = 4
                    } else {
                        sendChatMessage("Планируете ли вы саморазвитие?")
                        chatStage = 5
                    }
                } else {
                    sendChatMessage("Уточните, вы занимаетесь чем-то дополнительно? (да/нет)")
                }
            }
            4 -> {
                sendChatMessage("Планируете ли вы саморазвитие?")
                chatStage = 5
            }
            5 -> {
                sendChatMessage("Спасибо! Мы рады помочь с вашим временем.")
                chatStage = 6
            }
        }
        saveChatState()
    }

    private fun containsAnyWord(input: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> input.contains(keyword, ignoreCase = true) }
    }

    private fun setupNavigationButtons() {
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }

        val scheduleButton = findViewById<ImageButton>(R.id.scheduleButton)
        scheduleButton.setOnClickListener {
            val intent = Intent(this, ScheduleActivity::class.java)
            intent.putExtra("USER_UID", userUID)
            startActivity(intent)
        }

        val profileButton = findViewById<ImageButton>(R.id.profileButton)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USER_UID", userUID)
            startActivity(intent)
        }
    }
}
