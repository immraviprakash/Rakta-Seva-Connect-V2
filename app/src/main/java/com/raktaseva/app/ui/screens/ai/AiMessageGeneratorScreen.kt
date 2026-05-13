package com.raktaseva.app.ui.screens.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.raktaseva.app.BuildConfig
import com.raktaseva.app.ui.state.LocalUserState
import com.raktaseva.app.ui.theme.Dimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMessageGeneratorScreen(onBack: () -> Unit) {
    var generatedMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val generativeModel = remember {
        try {
            GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY
            )
        } catch (e: Exception) { null }
    }

    fun generateMessage() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val bloodGroup = LocalUserState.bloodGroup.value
                val name = LocalUserState.name.value
                val prompt = """Generate an urgent blood donation request message for social media/WhatsApp.

Details:
- Blood group needed: $bloodGroup
- Requester: $name
- App: Rakta-Seva Connect

Requirements:
- Keep it under 150 words
- Make it urgent but professional
- Include a call to action
- Add relevant hashtags
- Do NOT include any phone numbers or hospital names (user will add those)
- Make it shareable and emotionally impactful"""

                val response = generativeModel?.generateContent(prompt)
                val text = response?.text?.trim()

                if (text.isNullOrBlank()) {
                    errorMessage = "Couldn't generate a message. Try again."
                } else {
                    generatedMessage = text
                }
            } catch (e: Exception) {
                errorMessage = "Failed to generate. Please check your connection."
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Message Generator", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = Dimens.screenHorizontal)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(Dimens.spacingLg))

            Text(
                "Generate Emergency Request",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Dimens.spacingXs))
            Text(
                "AI will craft a shareable blood request message based on your profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spacingXxl))

            if (generatedMessage.isEmpty()) {
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(Dimens.cardRadius)
                    ) {
                        Text(
                            errorMessage!!,
                            modifier = Modifier.padding(Dimens.cardPadding),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                }

                Button(
                    onClick = { generateMessage() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(Dimens.buttonHeight),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(Dimens.buttonRadius)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(Dimens.spacingSm))
                        Text("Generating...")
                    } else {
                        Text("Generate Message", fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(Dimens.cardRadius)
                ) {
                    Text(
                        generatedMessage,
                        modifier = Modifier.padding(Dimens.cardPadding),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.spacingLg))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Blood Request", generatedMessage))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(Dimens.buttonHeight),
                        shape = RoundedCornerShape(Dimens.buttonRadius)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Dimens.spacingSm))
                        Text("Copy")
                    }
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, generatedMessage)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share via"))
                        },
                        modifier = Modifier.weight(1f).height(Dimens.buttonHeight),
                        shape = RoundedCornerShape(Dimens.buttonRadius)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(Dimens.spacingSm))
                        Text("Share")
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingSm))

                TextButton(
                    onClick = {
                        generatedMessage = ""
                        errorMessage = null
                    },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                ) {
                    Text("Generate Another", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingHuge))
        }
    }
}
