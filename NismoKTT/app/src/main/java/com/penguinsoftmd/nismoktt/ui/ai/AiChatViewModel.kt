package com.penguinsoftmd.nismoktt.ui.ai

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinsoftmd.nismoktt.domain.ai.AiApiService
import com.penguinsoftmd.nismoktt.domain.ai.Message
import com.penguinsoftmd.nismoktt.domain.ai.MessageRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiChatViewModel(
    private val aiApiService: AiApiService // Assuming this is injected via a factory
) : ViewModel() {

    val messages = mutableStateListOf<Message>()
    private val _isProcessing = MutableStateFlow(true)
    val isProcessing = _isProcessing.asStateFlow()

    private var threadId: String? = null

    fun startConversation(initialPrompt: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                aiApiService.createThread().body()?.id?.let { newThreadId ->
                    threadId = newThreadId
                    aiApiService.createMessage(
                        newThreadId,
                        MessageRequest(role = "user", content = initialPrompt)
                    )
                    aiApiService.createRun(newThreadId).body()?.id?.let { runId ->
                        pollRunStatus(newThreadId, runId)
                    }
                }
            } catch (e: Exception) {
                // Handle exceptions
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun sendMessage(content: String) {
        val currentThreadId = threadId ?: return
        viewModelScope.launch {
            _isProcessing.value = true
            try {
                aiApiService.createMessage(
                    currentThreadId,
                    MessageRequest(role = "user", content = content)
                )
                aiApiService.createRun(currentThreadId).body()?.id?.let { runId ->
                    pollRunStatus(currentThreadId, runId)
                }
            } catch (e: Exception) {
                // Handle exceptions
            } finally {
                _isProcessing.value = false
            }
        }
    }

    private suspend fun pollRunStatus(threadId: String, runId: String) {
        while (true) {
            val run = aiApiService.getRun(threadId, runId).body()
            when (run?.status) {
                "completed" -> {
                    fetchMessages(threadId)
                    break
                }

                "failed", "cancelled", "expired" -> {
                    // Handle unsuccessful run states
                    break
                }
            }
            delay(1000) // Poll every second
        }
    }

    private suspend fun fetchMessages(threadId: String) {
        aiApiService.listMessages(threadId).body()?.data?.let { data ->
            val allMessages = data.sortedBy { it.created_at }
            messages.clear()
            // Drop the first message which is the hidden initial prompt
            if (allMessages.isNotEmpty()) {
                messages.addAll(allMessages.drop(1))
            }
        }
    }
}

