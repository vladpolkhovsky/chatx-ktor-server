package rest

import kotlinx.serialization.Serializable

@Serializable
data class ChatDto(
	val id: Int,
	val name: String,
	val members: List<ChatMemberDto>,
	val lastMessageTimestamp: Long
)