package rest

import kotlinx.serialization.Serializable

@Serializable
data class NewMessageRequest(
	val fromUserId: Int,
	val chatId: Int,
	val replyTo: Int?,
	val text: String?,
	val fileIds: List<Int>?
)