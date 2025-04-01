package com.example.cookeasy

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.material3.TextFieldDefaults
// Custom colors
val AccentOrange = Color(0xFFFF5722)
val DarkGray = Color(0xFF2D2D2D)
val LightGray = Color(0xFFF5F5F5)
val MediumGray = Color(0xFFE0E0E0)
val backgroundColor = Color.White
val textColor = Color.Black


// Data class for a message (used in RecipeChatbotScreen)
data class Message(val text: String, val isUser: Boolean)

// Function to fetch recipe advice from Gemini API (shared between screens)
suspend fun fetchRecipeAdvice(input: String? = null, isTipsOnly: Boolean = false): String {
    val apiKey = BuildConfig.apiKey // Load API key from BuildConfig
    val client = OkHttpClient()
    val prompt = if (input.isNullOrBlank()) {
        """
        You are a friendly cooking expert chatbot.
        Provide a welcoming message and 5 concise cooking tips for beginners to improve recipe preparation.
        Each tip should be a single sentence focusing on practical advice, formatted as a numbered list.
        """
    } else if (isTipsOnly) {
        """
        You are a friendly cooking expert chatbot.
        Provide 5 concise cooking tips to improve the preparation of this recipe: "$input".
        Each tip should be a single sentence focusing on practical advice, formatted as a numbered list.
        """
    } else {
        """
        You are a friendly cooking expert chatbot.
        Respond to this user query about recipes in a conversational, helpful tone: "$input".
        If the query asks for tips, provide 5 concise tips in a numbered list.
        Otherwise, give a clear, practical answer tailored to the question.
        Keep it engaging and easy to understand.
        """
    }.trimIndent()

    // Prepare the JSON request body for Gemini API
    val jsonBody = JSONObject().apply {
        put("contents", JSONArray().put(JSONObject().apply {
            put("parts", JSONArray().put(JSONObject().apply {
                put("text", prompt)
            }))
        }))
    }.toString()

    val request = Request.Builder()
        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
        .post(jsonBody.toRequestBody("application/json".toMediaType()))
        .build()

    // Execute the API call
    val response = withContext(Dispatchers.IO) {
        client.newCall(request).execute()
    }

    if (!response.isSuccessful) {
        throw Exception("API call failed with code ${response.code}: ${response.message}")
    }

    val responseBody = response.body?.string() ?: throw Exception("Empty response from API")
    val jsonResponse = JSONObject(responseBody)
    return jsonResponse
        .getJSONArray("candidates")
        .getJSONObject(0)
        .getJSONObject("content")
        .getJSONArray("parts")
        .getJSONObject(0)
        .getString("text")
}

// Improved TipsScreen with animations and enhanced design
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(initialRecipeContext: String? = null) {
    val tips = remember { mutableStateOf<List<Tip>>(emptyList()) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val recipeInput = remember { mutableStateOf(initialRecipeContext ?: "") }
    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Fetch initial cooking tips when the screen is composed
    LaunchedEffect(initialRecipeContext) {
        isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val response = fetchRecipeAdvice(initialRecipeContext, isTipsOnly = true)
                val generatedTips = response.split("\n")
                    .filter { it.isNotBlank() }
                    .mapIndexed { index, tipText ->
                        Tip(id = index + 1, content = tipText.trim().removePrefix("${index + 1}. "))
                    }
                tips.value = generatedTips
            } catch (e: Exception) {
                errorMessage.value = "Failed to load tips: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Function to fetch new tips
    fun fetchNewTips() {
        scope.launch {
            isLoading.value = true
            errorMessage.value = null
            focusManager.clearFocus()

            try {
                val response = fetchRecipeAdvice(recipeInput.value.takeIf { it.isNotBlank() }, isTipsOnly = true)
                val newTips = response.split("\n")
                    .filter { it.isNotBlank() }
                    .mapIndexed { index, tipText ->
                        Tip(id = index + 1, content = tipText.trim().removePrefix("${index + 1}. "))
                    }
                tips.value = newTips
            } catch (e: Exception) {
                errorMessage.value = "Failed to load tips: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cooking Tips",
                        color = textColor,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Input field and button with enhanced design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = recipeInput.value,
                        onValueChange = { recipeInput.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        label = { Text("What are you cooking?") },
                        placeholder = { Text("E.g., chicken parmesan, pancakes, pasta") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { fetchNewTips() }
                        ),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = AccentOrange,
                            focusedLabelColor = AccentOrange,
                            cursorColor = AccentOrange
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { fetchNewTips() },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentOrange
                        )
                    ) {
                        Text("Get Tips", color = Color.White)
                    }
                }
            }

            // Loading indicator with animation
            AnimatedVisibility(
                visible = isLoading.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = AccentOrange,
                        strokeWidth = 4.dp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Asking ...",
                        color = DarkGray,
                        fontSize = 16.sp
                    )
                }
            }

            // Error message with animation
            AnimatedVisibility(
                visible = errorMessage.value != null && !isLoading.value,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage.value ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Display tips with animations
            AnimatedVisibility(
                visible = tips.value.isNotEmpty() && !isLoading.value,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut()
            ) {
                LazyColumn {
                    itemsIndexed(tips.value) { index, tip ->
                        TipCard(tip, index)
                    }
                }
            }
        }
    }
}

@Composable
fun TipCard(tip: Tip, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(tip) {
        delay(index * 100L) // Staggered animation delay
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .shadow(2.dp, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier
                        .size(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentOrange
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tip.id}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = tip.content,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// Improved RecipeChatbotScreen with animations and enhanced design
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeChatbotScreen() {
    val messages = remember { mutableStateListOf<Message>() }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val userInput = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Fetch initial welcome message
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val welcomeResponse = fetchRecipeAdvice(null)
                messages.add(Message(welcomeResponse, false))
            } catch (e: Exception) {
                errorMessage.value = "Failed to load welcome message: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    // Auto-scroll to bottom when new messages are added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Function to send a message
    fun sendMessage() {
        if (userInput.value.isNotBlank()) {
            val input = userInput.value
            messages.add(Message(input, true))
            userInput.value = ""
            focusManager.clearFocus()
            isLoading.value = true

            scope.launch {
                try {
                    val response = fetchRecipeAdvice(input)
                    messages.add(Message(response, false))
                    errorMessage.value = null
                } catch (e: Exception) {
                    errorMessage.value = "Failed to get response: ${e.message}"
                } finally {
                    isLoading.value = false
                }
            }
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recipe Assistant",
                        color = textColor,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Chat history with enhanced design
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(2.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = LightGray
                    )
                ) {
                    // Chat messages
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        state = listState
                    ) {
                        itemsIndexed(messages) { index, message ->
                            ChatBubble(message, index)
                        }

                        // Loading indicator for bot response
                        if (isLoading.value && messages.isNotEmpty()) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }
                }

                // Empty state
                this@Column.AnimatedVisibility(
                    visible = messages.isEmpty() && !isLoading.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ask me anything about cooking!",
                            fontSize = 18.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Initial loading state
                this@Column.AnimatedVisibility(
                    visible = messages.isEmpty() && isLoading.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = AccentOrange,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Getting your assistant ready...",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Error message with animation
            AnimatedVisibility(
                visible = errorMessage.value != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = errorMessage.value ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Input field and button with enhanced design
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .shadow(2.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = userInput.value,
                        onValueChange = { userInput.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        placeholder = { Text("Ask about recipes...") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = { sendMessage() }
                        ),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = AccentOrange,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedLabelColor = AccentOrange,
                            cursorColor = AccentOrange,
                            disabledContainerColor = LightGray,
                            unfocusedContainerColor = LightGray
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = { sendMessage() },
                        containerColor = AccentOrange,
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message, index: Int) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 100L) // Staggered animation delay
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { if (message.isUser) 50 else -50 },
        exit = fadeOut()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .padding(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isUser) AccentOrange else Color.White
                ),
                shape = RoundedCornerShape(
                    topStart = if (message.isUser) 12.dp else 4.dp,
                    topEnd = if (message.isUser) 4.dp else 12.dp,
                    bottomStart = 12.dp,
                    bottomEnd = 12.dp
                )
            ) {
                Text(
                    text = message.text,
                    fontSize = 16.sp,
                    color = if (message.isUser) Color.White else Color.Black,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 12.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "typing")

                // First dot animation
                val dot1Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot1"
                )

                // Second dot animation with delay
                val dot2Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(100)
                    ),
                    label = "dot2"
                )

                // Third dot animation with more delay
                val dot3Alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(400),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(200)
                    ),
                    label = "dot3"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.DarkGray.copy(alpha = dot1Alpha))
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.DarkGray.copy(alpha = dot2Alpha))
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.DarkGray.copy(alpha = dot3Alpha))
                )
            }
        }
    }
}