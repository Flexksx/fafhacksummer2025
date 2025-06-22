package com.penguinsoftmd.nismoktt.domain.ai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class Thread(
    val id: String,
    val `object`: String,
    val created_at: Long,
    val metadata: Map<String, String>
)

data class MessageRequest(
    val role: String,
    val content: String
)

data class MessageContent(
    val type: String,
    val text: MessageText
)

data class MessageText(
    val value: String,
    val annotations: List<Any>
)

data class Message(
    val id: String,
    val `object`: String,
    val created_at: Long,
    val thread_id: String,
    val role: String,
    val content: List<MessageContent>
)

data class MessageList(
    val `object`: String,
    val data: List<Message>
)

data class Run(
    val id: String,
    val `object`: String,
    val created_at: Long,
    val status: String,
    val assistant_id: String,
    val thread_id: String
)

interface AiApiService {

    companion object {
        const val BASE_URL = "YOUR_ASSISTANT_API_BASE_URL/"
    }

    // Threads
    @POST("threads")
    suspend fun createThread(@Body metadata: Map<String, String>? = null): Response<Thread>

    @GET("threads/{threadId}")
    suspend fun getThread(@Path("threadId") threadId: String): Response<Thread>

    // Messages
    @POST("threads/{threadId}/messages")
    suspend fun createMessage(
        @Path("threadId") threadId: String,
        @Body message: MessageRequest
    ): Response<Message>

    @GET("threads/{threadId}/messages")
    suspend fun listMessages(@Path("threadId") threadId: String): Response<MessageList>

    // Runs
    @POST("threads/{threadId}/runs")
    suspend fun createRun(@Path("threadId") threadId: String): Response<Run>

    @GET("threads/{threadId}/runs/{runId}")
    suspend fun getRun(
        @Path("threadId") threadId: String,
        @Path("runId") runId: String
    ): Response<Run>

    @DELETE("threads/{threadId}/runs/{runId}")
    suspend fun cancelRun(
        @Path("threadId") threadId: String,
        @Path("runId") runId: String
    ): Response<Unit>
}