package rest

import kotlinx.serialization.Serializable

@Serializable
data class ChatMemberDto(
	val chatId: Int,
	val user: UserDto
)