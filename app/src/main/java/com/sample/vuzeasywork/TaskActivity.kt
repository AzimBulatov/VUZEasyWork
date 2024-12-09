package com.sample.vuzeasywork

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class TaskActivity : AppCompatActivity() {

    private lateinit var messageField: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var welcomeMessage: TextView

    private val GptHelpMessages = mutableListOf<Message>() // Используем GptHelpMessages вместо messages
    private val database = FirebaseDatabase.getInstance().reference
    private var userUID: String? = null
    private var chatStage = 0

    private val apiUrl = "https://llm.api.cloud.yandex.net/foundationModels/v1/completion"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        try {
            initializeUIComponents()
        } catch (e: Exception) {
            Log.e("TaskActivity", "Error initializing UI components: ${e.message}")
            Toast.makeText(this, "Ошибка инициализации", Toast.LENGTH_SHORT).show()
            finish()
        }

        loadChatState()
        loadMessages()

        sendButton.setOnClickListener {
            val messageText = messageField.text.toString().trim()
            if (messageText.isNotEmpty()) {
                if (welcomeMessage.visibility == View.VISIBLE) {
                    welcomeMessage.visibility = View.GONE
                }
                handleUserMessage(messageText)
                messageField.text.clear()
            } else {
                Toast.makeText(this, "Введите сообщение", Toast.LENGTH_SHORT).show()
            }
        }

        setupNavigationButtons()
        setupTopButtons()
    }

    private fun onChatMessageAdded() {
        if (welcomeMessage.visibility == View.VISIBLE && GptHelpMessages.isNotEmpty()) {
            welcomeMessage.visibility = View.GONE
        }
    }

    private fun initializeUIComponents() {
        val sharedPreferences = getSharedPreferences("VUZEasyWorkPrefs", MODE_PRIVATE)
        userUID = intent.getStringExtra("USER_UID") ?: sharedPreferences.getString("USER_UID", null)

        if (userUID.isNullOrEmpty()) {
            Toast.makeText(this, "Ошибка: UID пользователя не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        sharedPreferences.edit().putString("USER_UID", userUID).apply()

        messageField = findViewById(R.id.messageField)
        sendButton = findViewById(R.id.sendButton)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        welcomeMessage = findViewById(R.id.welcomeMessage)

        chatAdapter = ChatAdapter(GptHelpMessages) // Используем GptHelpMessages в адаптере
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter

        loadMessages {
            if (GptHelpMessages.isNotEmpty()) {
                welcomeMessage.visibility = View.GONE
            } else {
                welcomeMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun handleUserMessage(userMessage: String) {
        // Сохраняем сообщение от пользователя в GptHelpMessages
        val userMessageObj = Message(userMessage, isUser = true, style = "default")
        saveMessage(userMessageObj) // Сохраняем сообщение от пользователя в базе данных

        val prompt = userMessage.trim()

        sendMessageToGPT(prompt) { response ->
            // Форматируем ответ от ИИ и сохраняем его в GptHelpMessages
            val formattedMessage = formatResponseForUser(response)
            val aiMessage = Message(formattedMessage, isUser = false, style = "default")
            saveMessage(aiMessage) // Сохраняем сообщение от ИИ в базе данных
        }
    }


    private fun formatResponseForUser(response: String): String {
        return response.trim().replace("\n", " ")
    }

    private fun sendMessageToGPT(prompt: String, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val messageHistory = JSONArray()

                for (message in GptHelpMessages) {
                    val jsonMessage = JSONObject().apply {
                        put("role", if (message.isUser) "user" else "assistant")
                        put("text", message.text)
                    }
                    messageHistory.put(jsonMessage)
                }

                val jsonMessage = JSONObject().apply {
                    put("role", "user")
                    put("text", prompt)
                }
                messageHistory.put(jsonMessage)

                val jsonString = """
                    {
                        "modelUri": "gpt://<ваш идентификатор каталога>/yandexgpt/latest",
                        "completionOptions": {
                            "temperature": 0.7,
                            "maxTokens": 2000
                        },
                        "messages": $messageHistory
                    }
                """

                val body = RequestBody.create("application/json".toMediaTypeOrNull(), jsonString)
                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Api-Key <ваш апи ключ>")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    val aiResponse = jsonResponse.optJSONObject("result")
                        ?.optJSONArray("alternatives")
                        ?.getJSONObject(0)
                        ?.getJSONObject("message")
                        ?.getString("text")
                    if (aiResponse != null) {
                        withContext(Dispatchers.Main) { callback(aiResponse) }
                    } else {
                        withContext(Dispatchers.Main) { showError("Ошибка: пустой ответ от AI.") }
                    }
                } else {
                    withContext(Dispatchers.Main) { showError("Ошибка: сервер вернул пустой ответ.") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { showError("Ошибка API: ${e.message}") }
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e("TaskActivity", message)
    }

    private fun saveChatState() {
        val chatState = mapOf(
            "chatStage" to chatStage,
            "welcomeMessageVisible" to (welcomeMessage.visibility == View.VISIBLE)
        )
        database.child("users").child(userUID!!).child("chatState").setValue(chatState)
            .addOnFailureListener { showError("Ошибка сохранения состояния чата.") }
    }

    private fun loadChatState() {
        database.child("users").child(userUID!!).child("chatState")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatStage = snapshot.child("chatStage").getValue(Int::class.java) ?: 0
                    welcomeMessage.visibility = if (snapshot.child("welcomeMessageVisible").getValue(Boolean::class.java) == true) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Ошибка загрузки состояния чата.")
                }
            })
    }

    private fun loadMessages(onComplete: () -> Unit = {}) {
        database.child("users").child(userUID!!).child("GptHelpMessages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    GptHelpMessages.clear()  // Заменяем messages на GptHelpMessages
                    snapshot.children.forEach { data ->
                        val map = data.value as? Map<String, Any?>
                        if (map != null) {
                            GptHelpMessages.add(Message.fromMap(map))
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                    chatRecyclerView.scrollToPosition(GptHelpMessages.size - 1)
                    onComplete()
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Ошибка загрузки сообщений.")
                }
            })
    }

    private fun saveMessage(message: Message) {
        GptHelpMessages.add(message)  // Заменяем messages на GptHelpMessages
        chatAdapter.notifyItemInserted(GptHelpMessages.size - 1)
        chatRecyclerView.scrollToPosition(GptHelpMessages.size - 1)
        database.child("users").child(userUID!!).child("GptHelpMessages").push().setValue(message)
            .addOnFailureListener { showError("Ошибка сохранения сообщения в базу данных.") }
    }

    private fun setupNavigationButtons() {
        val homeButton = findViewById<ImageButton>(R.id.homeButton)
        homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USER_UID", userUID)
            startActivity(intent)
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

    private fun setupTopButtons() {
        val planButton = findViewById<Button>(R.id.planButton)
        planButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("USER_UID", userUID)
            startActivity(intent)
        }

        val gptButton = findViewById<Button>(R.id.gptButton)
        gptButton.setOnClickListener {
            Toast.makeText(this, "Вы уже находитесь на этой странице", Toast.LENGTH_SHORT).show()
        }
    }
}
