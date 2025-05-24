package rest

import kotlinx.serialization.Serializable

@Serializable
data class ChatCodeDto(val code: String, val chatId: Int)
