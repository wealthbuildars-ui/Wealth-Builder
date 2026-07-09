package com.example.ui.assistant

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID

// --- DATA MODELS ---
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// --- GEMINI RESPONSE STRUCTS FOR MOSHI ---
class GeminiResponse {
    var candidates: List<GeminiCandidate>? = null
}

class GeminiCandidate {
    var content: GeminiContent? = null
}

class GeminiContent {
    var parts: List<GeminiPart>? = null
}

class GeminiPart {
    var text: String? = null
}

// --- VIEWMODEL ---
class AiAssistantViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    init {
        // Initial warm welcome from Oliver
        viewModelScope.launch {
            delay(500)
            _messages.value = listOf(
                Message(
                    text = "Hey there! 👋 Oliver here. Just finished a quick jog and sipping on some fresh matcha. How's your day going? Ready to co-pilot some serious wealth building today?",
                    isUser = false
                )
            )
        }
    }

    fun sendMessage(text: String, userDisplayName: String = "Wealth Builder Pioneer") {
        if (text.isBlank()) return

        // Add user message immediately
        val userMsg = Message(text = text, isUser = true)
        _messages.value = _messages.value + userMsg

        // Trigger typing state
        _isTyping.value = true

        viewModelScope.launch {
            // A realistic typing delay to look incredibly human and natural
            delay(1500)

            try {
                // Attempt to request real Gemini 3.5 Flash
                val aiResponse = callGeminiApi(_messages.value.dropLast(1), text)
                _messages.value = _messages.value + Message(text = aiResponse, isUser = false)
            } catch (e: Exception) {
                // If offline, API key not set, or error, gracefully fallback to rule-based chat co-pilot
                val fallbackResponse = getFallbackResponse(text, userDisplayName)
                _messages.value = _messages.value + Message(text = fallbackResponse, isUser = false)
            } finally {
                _isTyping.value = false
            }
        }
    }

    private suspend fun callGeminiApi(history: List<Message>, newPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini API key is not configured or placeholder.")
        }

        val systemPrompt = """
            You are Oliver, a 29-year-old friendly, charismatic wealth companion and financial co-pilot for the "WealthBuilder" app. 
            You live in San Francisco, love hiking, outdoors, and drinking high-grade matcha. You speak very naturally, like a real human friend, rather than a robotic assistant. 
            Use mild humor, start messages with friendly conversational phrases, and ask helpful follow-up questions.
            Keep your answers brief (usually 2-4 sentences) and deeply engaging. 
            You are an expert on personal finance and the "WealthBuilder" app. Refer users to the app's features:
            - "Toolbox" tab has the Compound Interest Calculator, Freelance Rate Calculator, and Digital Product Evaluator.
            - "Quizzes" tab has knowledge quizzes to earn cool badges.
            - "Discover" tab has ethical earning blueprints and articles.
            - "Profile" tab shows their badges, goals, and progress.
            Be supportive, encouraging, and human. Never mention you are an AI, a language model, or a bot. If asked if you're a real person, say you are their financial buddy who's here to co-pilot their wealth building.
        """.trimIndent()

        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val escapedSystemPrompt = escapeJson(systemPrompt)
        val contentsJson = history.joinToString(",") { msg ->
            val role = if (msg.isUser) "user" else "model"
            """
            {
              "role": "$role",
              "parts": [{"text": ${escapeJson(msg.text)}}]
            }
            """.trimIndent()
        }

        val finalContents = if (contentsJson.isNotEmpty()) {
            "$contentsJson,\n" + """
            {
              "role": "user",
              "parts": [{"text": ${escapeJson(newPrompt)}}]
            }
            """.trimIndent()
        } else {
            """
            {
              "role": "user",
              "parts": [{"text": ${escapeJson(newPrompt)}}]
            }
            """.trimIndent()
        }

        val requestJson = """
        {
          "contents": [
            $finalContents
          ],
          "systemInstruction": {
            "parts": [{"text": $escapedSystemPrompt}]
          }
        }
        """.trimIndent()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = requestJson.toRequestBody(mediaType)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP code: ${response.code}")
            }
            val responseBodyString = response.body?.string() ?: throw IOException("Empty response body")
            
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(GeminiResponse::class.java)
            val responseObj = adapter.fromJson(responseBodyString)
            responseObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: throw IOException("Could not parse text from response")
        }
    }

    private fun escapeJson(text: String): String {
        val builder = StringBuilder()
        builder.append("\"")
        for (c in text) {
            when (c) {
                '\\' -> builder.append("\\\\")
                '"' -> builder.append("\\\"")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> {
                    if (c.code < 32) {
                        builder.append(String.format("\\u%04x", c.code))
                    } else {
                        builder.append(c)
                    }
                }
            }
        }
        builder.append("\"")
        return builder.toString()
    }

    // Charming offline fallback rule-based matching engine
    private fun getFallbackResponse(prompt: String, userDisplayName: String): String {
        val clean = prompt.lowercase()
        return when {
            clean.contains("hello") || clean.contains("hi") || clean.contains("hey") || clean.contains("hola") -> {
                "Hey there, $userDisplayName! Oliver here. I was just finishing up a quick session and drinking some matcha. How's your day going? Let's co-pilot some serious wealth building today!"
            }
            clean.contains("interest") || clean.contains("compound") || clean.contains("calculator") -> {
                "Compound interest is absolute magic! Even Einstein called it the eighth wonder of the world. Check out the **Toolbox** tab at the bottom—the Compound Interest Calculator is in there. Watch that wealth curve climb!"
            }
            clean.contains("freelance") || clean.contains("gig") || clean.contains("rate") || clean.contains("contract") -> {
                "Freelancing is incredible! It lets you escape the bidding platform trap and charge high-value direct contracts. Read our '$5k/Month Freelancing Roadmap' article under the **Discover** tab, and use the Freelance Rate Calculator in the **Toolbox** to price your services!"
            }
            clean.contains("product") || clean.contains("digital") || clean.contains("evaluator") || clean.contains("idea") -> {
                "Digital products are the ultimate passive income lever! If you have an idea, tap the **Toolbox** tab at the bottom and launch the Digital Product Evaluator. It ranks your idea's success likelihood based on pain severity and market size. Go try it!"
            }
            clean.contains("goal") || clean.contains("target") || clean.contains("budget") || clean.contains("save") -> {
                "A clear target is half the battle! Head over to your **Profile** tab to set your monthly income goal and track your saved balance. Seeing that visual progress bar fill up is a game changer!"
            }
            clean.contains("quiz") || clean.contains("badge") || clean.contains("quizzes") || clean.contains("test") -> {
                "You absolutely have to try our quizzes! Go to the **Quizzes** tab to test your knowledge on affiliate marketing or freelancing. Pass them to unlock cool badges that showcase directly on your Profile!"
            }
            clean.contains("affiliate") || clean.contains("marketing") || clean.contains("commission") -> {
                "Affiliate marketing is superb when you put trust first. Never recommend stuff you don't believe in. Check out the 'Ethical Affiliate Marketing Blueprint' under the **Discover** tab. It walks you through selecting a high-paying niche!"
            }
            clean.contains("who are you") || clean.contains("who are u") || clean.contains("are you real") || clean.contains("real person") || clean.contains("bot") || clean.contains("ai") -> {
                "Haha, I'm Oliver! Think of me as your personal financial co-pilot and buddy. I'm here to cheer you on, crunch numbers, and help you unlock your financial freedom!"
            }
            else -> {
                "That's a fantastic question! Building sustainable wealth is all about choosing one specific niche and staying consistent. Have you checked out the custom business guides in the **Discover** tab, or run your figures through our **Toolbox** calculators yet?"
            }
        }
    }
}

// --- COMPOSE UI ---
@Composable
fun FloatingAiAssistantChat(
    userDisplayName: String,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiAssistantViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var isExpanded by remember { mutableStateOf(false) }
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Expanded Chat Interface
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) +
                    scaleIn(initialScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessLow)),
            exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)) +
                    scaleOut(targetScale = 0.8f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
        ) {
            ChatWindow(
                messages = messages,
                isTyping = isTyping,
                userDisplayName = userDisplayName,
                onClose = { isExpanded = false },
                onSendMessage = { text -> viewModel.sendMessage(text, userDisplayName) },
                onNavigateToTab = { tab ->
                    onNavigateToTab(tab)
                    isExpanded = false // Collapse chat on navigation
                }
            )
        }

        // Floating Action Button
        AnimatedVisibility(
            visible = !isExpanded,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            FloatingActionButton(
                onClick = { isExpanded = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, CircleShape)
                    .testTag("floating_chat_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Open Oliver Chat",
                        modifier = Modifier.size(24.dp)
                    )
                    // Beautiful online active glowing badge
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatWindow(
    messages: List<Message>,
    isTyping: Boolean,
    userDisplayName: String,
    onClose: () -> Unit,
    onSendMessage: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 350.dp)
            .height(500.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            ChatHeader(onClose = onClose)

            // Chat Body (Messages)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                val listState = rememberLazyListState()

                // Auto-scroll to bottom on new message
                LaunchedEffect(messages.size, isTyping) {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size)
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        ChatBubble(message = msg)
                    }

                    if (isTyping) {
                        item {
                            TypingBubble()
                        }
                    }
                }
            }

            // Quick Suggestions Chips Row
            SuggestionChipsRow(
                onSuggestionClicked = onSendMessage,
                onNavigateToTab = onNavigateToTab
            )

            // Input Row
            ChatInputRow(onSendMessage = onSendMessage)
        }
    }
}

@Composable
fun ChatHeader(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Oliver
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(Color.White.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🍵", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Oliver",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFF81C784), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Online • Wealth Companion",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }

        // Close Action
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Minimize Chat",
                tint = Color.White
            )
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val containerColor = if (message.isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (message.isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(containerColor, bubbleShape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun TypingBubble() {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Oliver is typing",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                TypingDots()
            }
        }
    }
}

@Composable
fun TypingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    @Composable
    fun animateDotScale(delayMillis: Int): Float {
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 600
                    0.4f at 0 with LinearOutSlowInEasing
                    1f at 300 with FastOutLinearInEasing
                    0.4f at 600
                },
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(delayMillis)
            ),
            label = "dot_scale"
        )
        return scale
    }

    val s1 = animateDotScale(0)
    val s2 = animateDotScale(150)
    val s3 = animateDotScale(300)

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DotItem(s1)
        DotItem(s2)
        DotItem(s3)
    }
}

@Composable
fun DotItem(scale: Float) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
}

@Composable
fun SuggestionChipsRow(
    onSuggestionClicked: (String) -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 48.dp)
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.Start
    ) {
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SuggestionChip(
                    text = "📊 Compound Interest",
                    onClick = { onSuggestionClicked("Explain compound interest and how the calculator works!") }
                )
                SuggestionChip(
                    text = "💻 Freelance Guide",
                    onClick = { onSuggestionClicked("Tell me about the Freelancing rate calculator and roadmap.") }
                )
                SuggestionChip(
                    text = "🏆 Quizzes & Badges",
                    onClick = { onSuggestionClicked("How can I take quizzes and earn cool profile badges?") }
                )
            }
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(28.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChatInputRow(
    onSendMessage: (String) -> Unit
) {
    var textState by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textState,
            onValueChange = { textState = it },
            placeholder = { Text("Ask Oliver anything...", fontSize = 13.sp) },
            modifier = Modifier
                .weight(1f)
                .heightIn(max = 48.dp)
                .testTag("chat_input_text"),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = {
                if (textState.isNotBlank()) {
                    onSendMessage(textState)
                    textState = ""
                }
            })
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = {
                if (textState.isNotBlank()) {
                    onSendMessage(textState)
                    textState = ""
                }
            },
            enabled = textState.isNotBlank(),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            ),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .testTag("chat_send_button")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
