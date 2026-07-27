package com.sleeplessdog.banquerito.ui.screens.consultant


import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import com.sleeplessdog.banquerito.data.FileOpener
import com.sleeplessdog.banquerito.domain.model.ChatMessage
import com.sleeplessdog.banquerito.domain.model.ChatRole
import com.sleeplessdog.banquerito.presentation.consultant.AttachState
import com.sleeplessdog.banquerito.presentation.consultant.AttachedFile
import com.sleeplessdog.banquerito.presentation.consultant.ConsultantUiState
import com.sleeplessdog.banquerito.presentation.consultant.ConsultantViewModel
import com.sleeplessdog.banquerito.presentation.consultant.rememberFilePickerLauncher
import com.sleeplessdog.banquerito.ui.BanqueritoColorScheme
import com.sleeplessdog.banquerito.ui.icons.AppIcons
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConsultantScreen(
    viewModel: ConsultantViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePicker = rememberFilePickerLauncher(onFilePicked = { name, mimeType, bytes ->
        viewModel.onFileAttached(AttachedFile(name, mimeType, bytes))
    }, onError = {
        viewModel.onFileAttachFailed()
    })

    ConsultantContent(
        uiState = uiState,
        onSendMessage = viewModel::sendMessage,
        onClearChat = viewModel::clearChat,
        onStartSpeechRecognition = viewModel::startSpeechRecognition,
        onAttachClick = { filePicker.launch() },
        onClearAttachment = viewModel::clearAttachment,
    )
}

@Composable
fun ConsultantContent(
    uiState: ConsultantUiState,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    onStartSpeechRecognition: () -> Unit,
    onAttachClick: () -> Unit,
    onClearAttachment: () -> Unit,
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // заголовок
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.consultant_title),
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(Res.string.consultant_subtitle),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (uiState.messages.isNotEmpty()) {
                    TextButton(onClick = { onClearChat() }) {
                        Text(stringResource(Res.string.action_clear), fontSize = 12.sp)
                    }
                }
            }
        }

        // сообщения
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👋", fontSize = 40.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(Res.string.consultant_greeting_text),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        // быстрые вопросы
                        listOf(
                            stringResource(Res.string.consultant_suggestion_taxes),
                            stringResource(Res.string.consultant_suggestion_balance),
                            stringResource(Res.string.consultant_suggestion_quarter),
                        ).forEach { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    onSendMessage(suggestion)
                                },
                                label = { Text(suggestion, fontSize = 12.sp) },
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            items(uiState.messages) { message ->
                MessageBubble(message = message)
            }

            if (uiState.isLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 4.dp,
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            ), color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(3) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(6.dp), strokeWidth = 1.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

// индикатор прикреплённого файла
        if (uiState.attachedFile != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = AppIcons.chatAttach(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.attachedFile.name,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onClearAttachment, modifier = Modifier.size(24.dp)) {
                        Text("✕", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        } else if (uiState.attachState == AttachState.FAILED) {
            Text(
                text = stringResource(Res.string.consultant_attach_failed),
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

// поле ввода — теперь ВСЕГДА рендерится, без if
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onAttachClick, enabled = !uiState.isLoading
                ) {
                    Icon(
                        painter = AppIcons.chatAttach(),
                        contentDescription = stringResource(Res.string.action_attach),
                        tint = if (uiState.attachedFile != null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            stringResource(Res.string.consultant_placeholder), fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedPlaceholderColor = Color.Transparent,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !uiState.isLoading) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        })
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            if (!uiState.isLoading) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                        } else {
                            onStartSpeechRecognition()
                        }
                    }, enabled = !uiState.isLoading
                ) {
                    Crossfade(targetState = inputText.isNotBlank()) { hasText ->
                        if (hasText) {
                            Icon(
                                painter = AppIcons.chatSend(),
                                contentDescription = stringResource(Res.string.action_send),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                painter = AppIcons.chatStt(),
                                contentDescription = stringResource(Res.string.consultant_voice),
                                tint = if (uiState.isListening) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val fileOpener = koinInject<FileOpener>()
    val isUser = message.role == ChatRole.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (message.attachedFileName != null && message.attachedFilePath != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .clickable {
                                fileOpener.openFile(
                                    message.attachedFilePath,
                                    message.attachedFileMimeType ?: "*/*"
                                )
                            }
                    ) {
                        Icon(
                            painter = AppIcons.chatAttach(),
                            contentDescription = null,
                            tint = if (isUser) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = message.attachedFileName,
                            fontSize = 12.sp,
                            textDecoration = TextDecoration.Underline,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Preview
@Composable
fun ConsultantScreenPreview() {
    MaterialTheme(colorScheme = BanqueritoColorScheme) {
        ConsultantContent(
            uiState = ConsultantUiState(
                messages = listOf(
                    ChatMessage(
                        id = "1",
                        role = ChatRole.ASSISTANT,
                        content = "Hello! I am your financial consultant. How can I help you today?",
                        createdAt = Clock.System.now()
                    ), ChatMessage(
                        id = "2",
                        role = ChatRole.USER,
                        content = "What are my taxes for this quarter?",
                        createdAt = Clock.System.now()
                    ), ChatMessage(
                        id = "3",
                        role = ChatRole.ASSISTANT,
                        content = "Your estimated taxes for Q3 are $1,250 based on your current income.",
                        createdAt = Clock.System.now()
                    )
                )
            ),
            onSendMessage = {},
            onClearChat = {},
            onStartSpeechRecognition = {},
            onAttachClick = {},
            onClearAttachment = {})
    }
}

@Preview
@Composable
fun ConsultantScreenEmptyPreview() {
    MaterialTheme(colorScheme = BanqueritoColorScheme) {
        ConsultantContent(
            uiState = ConsultantUiState(messages = emptyList()),
            onSendMessage = {},
            onClearChat = {},
            onStartSpeechRecognition = {},
            onAttachClick = {},
            onClearAttachment = {})
    }
}
