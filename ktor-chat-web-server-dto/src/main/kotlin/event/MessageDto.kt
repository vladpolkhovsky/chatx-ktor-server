package event

import kotlinx.serialization.Serializable
import rest.FileDto
import rest.UserDto

@Serializable
data class MessageDto(
	val id: Int,
	val chatId: Int,
	val text: String?,
	val files: List<FileDto>,
	val replyTo: MessageDto?,
	val from: UserDto,
	val timestamp: Long
)