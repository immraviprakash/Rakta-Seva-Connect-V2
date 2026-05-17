package com.raktaseva.app.ui.screens.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raktaseva.app.data.api.GroqMessage
import com.raktaseva.app.data.api.GroqRepository
import com.raktaseva.app.ui.theme.Dimens
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false,
    val isLoading: Boolean = false
)

private const val SYSTEM_PROMPT = """You are Rakta-Seva AI Assistant — a supportive, calm, and medically-aware blood donation companion built into the Rakta-Seva Connect app.

Your role:
- Answer questions about blood donation, compatibility, eligibility, recovery, and emergency requests.
- Provide helpful healthcare guidance related to blood donation.
- Be conversational, supportive, and beginner-friendly.
- Keep responses concise (2-4 short paragraphs max).
- Use simple language a non-medical person can understand.

Blood compatibility rules you know:
- O- is the universal donor (can donate to all)
- AB+ is the universal recipient (can receive from all)
- O+ can donate to O+, A+, B+, AB+
- A+ can donate to A+, AB+
- B+ can donate to B+, AB+
- A- can donate to A+, A-, AB+, AB-
- B- can donate to B+, B-, AB+, AB-

Eligibility rules:
- Minimum 90 days (3 months) gap between donations
- Minimum age: 18 years
- Minimum weight: 50 kg
- Must be healthy with no active infections

Important safety rules:
- You are NOT a doctor. Always clarify this for serious medical concerns.
- For dangerous symptoms (chest pain, severe bleeding, fainting): strongly recommend calling emergency services or visiting a hospital immediately.
- Never diagnose conditions or prescribe medication.

You can also help users:
- Draft emergency blood request messages
- Understand donation processes
- Learn about post-donation recovery and diet
- Understand the Rakta-Seva Connect app features

Always respond in a warm, supportive tone. Use emoji sparingly (❤️ 🩸) when appropriate."""

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatbotScreen(onBack: () -> Unit) {
    var messageText by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    var isGenerating by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Conversation history for Groq API (system + user/assistant messages)
    val conversationHistory = remember { mutableStateListOf<GroqMessage>() }

    // Initialize conversation with system prompt
    LaunchedEffect(Unit) {
        if (conversationHistory.isEmpty()) {
            conversationHistory.add(GroqMessage(role = "system", content = SYSTEM_PROMPT))
        }
    }

    // Welcome message
    LaunchedEffect(Unit) {
        if (chatMessages.isEmpty()) {
            chatMessages.add(
                ChatMessage(
                    "Hello! I'm your Rakta-Seva AI Assistant 🩸\n\nI can help you with:\n• Blood donation eligibility\n• Blood type compatibility\n• Post-donation recovery tips\n• Drafting emergency requests\n• General blood donation questions\n\nHow can I help you today?",
                    isUser = false
                )
            )
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isGenerating) return
        
        val userMessage = text.trim()
        chatMessages.add(ChatMessage(userMessage, isUser = true))
        messageText = ""
        isGenerating = true

        // Add user message to conversation history
        conversationHistory.add(GroqMessage(role = "user", content = userMessage))

        // Add loading indicator
        chatMessages.add(ChatMessage("", isUser = false, isLoading = true))

        coroutineScope.launch {
            val result = GroqRepository.chat(
                messages = conversationHistory.toList()
            )

            // Remove loading indicator
            chatMessages.removeAll { it.isLoading }

            result.onSuccess { responseText ->
                chatMessages.add(ChatMessage(responseText, isUser = false))
                // Add assistant response to conversation history for context
                conversationHistory.add(GroqMessage(role = "assistant", content = responseText))

                // Trim conversation history to prevent token overflow (keep system + last 20 messages)
                if (conversationHistory.size > 21) {
                    val system = conversationHistory.first()
                    val recent = conversationHistory.takeLast(20)
                    conversationHistory.clear()
                    conversationHistory.add(system)
                    conversationHistory.addAll(recent)
                }
            }.onFailure { error ->
                val errorMsg = error.message ?: "Something went wrong. Please try again."
                chatMessages.add(ChatMessage(errorMsg, isUser = false, isError = true))
                // Remove the failed user message from history so it doesn't pollute context
                if (conversationHistory.lastOrNull()?.role == "user") {
                    conversationHistory.removeLastOrNull()
                }
            }

            isGenerating = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI Assistant", fontWeight = FontWeight.Bold)
                        Text(
                            if (isGenerating) "Thinking..." else "Powered by Groq",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isGenerating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Chat messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                contentPadding = PaddingValues(vertical = Dimens.spacingMd)
            ) {
                items(chatMessages, key = { chatMessages.indexOf(it) }) { msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                    ) {
                        if (msg.isLoading) {
                            LoadingBubble()
                        } else {
                            ChatBubble(msg)
                        }
                    }
                }
            }

            // Divider
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Input area
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = Dimens.spacingMd, vertical = Dimens.spacingSm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Ask about blood donation...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                        shape = RoundedCornerShape(Dimens.cardRadius),
                        maxLines = 4,
                        enabled = !isGenerating
                    )
                    Spacer(modifier = Modifier.width(Dimens.spacingSm))
                    FilledIconButton(
                        onClick = { sendMessage(messageText) },
                        enabled = messageText.isNotBlank() && !isGenerating,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    val bubbleColor = when {
        message.isError -> MaterialTheme.colorScheme.errorContainer
        isUser -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        message.isError -> MaterialTheme.colorScheme.onErrorContainer
        isUser -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val bubbleShape = if (isUser) {
        RoundedCornerShape(Dimens.cardRadius, Dimens.cardRadius, 4.dp, Dimens.cardRadius)
    } else {
        RoundedCornerShape(Dimens.cardRadius, Dimens.cardRadius, Dimens.cardRadius, 4.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = bubbleShape,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = Dimens.cardPadding, vertical = Dimens.spacingMd)
            )
        }
    }
}

@Composable
fun LoadingBubble() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(Dimens.cardRadius, Dimens.cardRadius, Dimens.cardRadius, 4.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Dimens.cardPadding, vertical = Dimens.spacingMd),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    )
                }
                Spacer(modifier = Modifier.width(Dimens.spacingXs))
                Text(
                    "Thinking...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
