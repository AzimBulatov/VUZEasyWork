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

class ChatActivity : AppCompatActivity() {

    private lateinit var messageField: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var welcomeMessage: TextView

    private val messages = mutableListOf<Message>()
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
            Log.e("ChatActivity", "Error initializing UI components: ${e.message}")
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
        // Функция вызывается, когда добавляется новое сообщение
        if (welcomeMessage.visibility == View.VISIBLE && messages.isNotEmpty()) {
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

        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        chatRecyclerView.adapter = chatAdapter

        // Загружаем сообщения и устанавливаем видимость приветствия
        loadMessages {
            if (messages.isNotEmpty()) {
                welcomeMessage.visibility = View.GONE
            } else {
                welcomeMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun handleUserMessage(userMessage: String) {
        saveMessage(Message(userMessage, isUser = true, style = "default"))

        onChatMessageAdded()


        when (chatStage) {
            0 -> {
                // Шаг 1: Приветствие и выяснение роли пользователя
                val prompt = """
                Создай текст сообщения для пользователя:
                "Здравствуйте! Для начала работы сообщите, кем вы являетесь (вы студент или преподаватель)?"
                Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
            """.trimIndent()

                sendMessageToGPT(prompt) { response ->
                    val formattedMessage = formatResponseForUser(response)
                    saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                    chatStage = 1
                    saveChatState()
                }
            }

            1 -> {
                // Шаг 2: Получение информации о количестве дисциплин
                val role = when (userMessage.lowercase()) {
                    "студент" -> "студента"
                    "преподаватель" -> "преподавателя"
                    else -> "неизвестной роли"
                }

                val prompt = """
                Создай текст сообщения для пользователя:
                "Вы указали, что являетесь $role, сколько у вас дисциплин?"
                Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
            """.trimIndent()

                sendMessageToGPT(prompt) { response ->
                    val formattedMessage = formatResponseForUser(response)
                    saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                    chatStage = 2
                    saveChatState()
                }
            }

            2 -> {
                // Шаг 3: Запрос подробностей по каждой дисциплине
                val prompt = """
                Создай текст сообщения для пользователя:
                "Для каждой дисциплины напишите, сколько лабораторных, практических и самостоятельных работ, а также других заданий."
                Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
            """.trimIndent()

                sendMessageToGPT(prompt) { response ->
                    val formattedMessage = formatResponseForUser(response)
                    saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                    chatStage = 3
                    saveChatState()
                }
            }

            3 -> {
                // Шаг 4: Уточнение про дополнительные задания
                val prompt = """
                Создай текст сообщения для пользователя:
                "Есть ли у вас какие-либо дополнительные задания по учебе (например, курсовая работа)?"
                Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
            """.trimIndent()

                sendMessageToGPT(prompt) { response ->
                    val formattedMessage = formatResponseForUser(response)
                    saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                    chatStage = 4
                    saveChatState()
                }
            }

            4 -> {
                // Шаг 5: Уточнение по дополнительной деятельности
                val prompt = if (userMessage.lowercase() == "да") {
                    """
                    Создай текст сообщения для пользователя:
                    "Опишите, чем именно вы занимаетесь, в какие дни и во сколько."
                    Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
                """.trimIndent()
                } else {
                    """
                    Создай текст сообщения для пользователя:
                    "Планируете ли вы заниматься саморазвитием?"
                    Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
                """.trimIndent()
                }

                sendMessageToGPT(prompt) { response ->
                    val formattedMessage = formatResponseForUser(response)
                    saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                    chatStage = 5
                    saveChatState()
                }
            }

            5 -> {
                // Шаг 6: Уточнение по саморазвитию или завершение
                val prompt = if (userMessage.lowercase() == "да") {
                    """
                    Создай текст сообщения для пользователя:
                    "Чем именно вы хотели бы заниматься и что хотели бы освоить?"
                    Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
                """.trimIndent()
                } else {
                    """
                    Создай текст сообщения для пользователя:
                    "Спасибо за ваши ответы! Сейчас мы сформируем для вас план занятий на учебное полугодие. Для получения плана напишите \"план\"."
                    Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
                """.trimIndent()
                }

                sendMessageToGPT(prompt) { response ->
                    val formattedMessage = formatResponseForUser(response)
                    saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                    chatStage = 6
                    saveChatState()
                }
            }

            6 -> {
                // Шаг 7: Обработка ввода команды "план" и правок
                val planPrompt = if (userMessage.lowercase() == "план") {
                    """
            На основе данных пользователя составь расписание на учебное полугодие.
            Задачи должны быть равномерно распределены, не перегружая пользователя. Формат:
            "07.12.2024: 'Задача 1', 'Задача 2', 'Задача 3'."
            Пиши каждую строку с новой строки.
        """.trimIndent()
                } else {
                    // Переходим в стандартный режим чата, чтобы пользователь мог задать новый вопрос
                    val prompt = userMessage.trim()

                    sendMessageToGPT(prompt) { response ->
                        val formattedMessage = formatResponseForUser(response)
                        saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                    }

                    chatStage = 6
                    saveChatState()
                    return
                }

                sendMessageToGPT(planPrompt) { response ->
                    val formattedMessage = formatResponseForUser(response)
                    saveMessage(Message(formattedMessage, isUser = false, style = "default"))

                    // Сохраняем сгенерированный план в Firebase
                    database.child("users").child(userUID!!).child("generatedPlan").setValue(formattedMessage)
                        .addOnFailureListener { showError("Ошибка сохранения плана в базу данных.") }

                    if (userMessage.lowercase() == "план") {
                        chatStage = 7
                        saveChatState()

                        val followUpPrompt = """
                Создай текст сообщения для пользователя:
                "Ваш план занятий составлен. Если вы хотите внести изменения или задать дополнительные вопросы, напишите их ниже."
                Пиши именно то, что указано в кавычках, не добавляй ничего от себя.
            """.trimIndent()

                        sendMessageToGPT(followUpPrompt) { response ->
                            val formattedMessage = formatResponseForUser(response)
                            saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                        }
                    }
                }
            }

            7 -> {
                // После того как план был сформирован, ИИ продолжает отвечать на вопросы и правки
                val prompt = userMessage.trim()

                sendMessageToGPT(prompt) { response ->
                    val formattedMessage = formatResponseForUser(response)
                    saveMessage(Message(formattedMessage, isUser = false, style = "default"))
                }
            }
        }
    }




    // Вспомогательная функция для форматирования ответа
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

                for (message in messages) {
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
        Log.e("ChatActivity", message)
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
                    onComplete() // Вызываем коллбэк после завершения загрузки
                }

                override fun onCancelled(error: DatabaseError) {
                    showError("Ошибка загрузки сообщений.")
                }
            })
    }

    private fun saveMessage(message: Message) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)
        database.child("users").child(userUID!!).child("messages").push().setValue(message)
            .addOnFailureListener { showError("Ошибка сохранения сообщения в базу данных.") }
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
            val intent = Intent(this, ScheduleActivity::class.java)
            intent.putExtra("USER_UID", userUID) // Передаем UID
            startActivity(intent)
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
            Toast.makeText(this, "Вы уже находитесь на этой странице", Toast.LENGTH_SHORT).show()
        }

        val gptButton = findViewById<Button>(R.id.gptButton)
        gptButton.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra("USER_UID", userUID) // Передаем UID пользователя
            startActivity(intent)
        }
    }

}