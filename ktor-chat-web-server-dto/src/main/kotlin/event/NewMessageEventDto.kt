package event

import kotlinx.serialization.Serializable

@Serializable
data class NewMessageEventDto(
	val chatId: Int,
	val message: MessageDto
)
