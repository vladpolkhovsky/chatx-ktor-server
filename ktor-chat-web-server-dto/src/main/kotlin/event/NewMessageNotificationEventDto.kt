package event

import kotlinx.serialization.Serializable
import rest.UserDto

@Serializable
data class NewMessageNotificationEventDto(
	val chatId: Int,
	val messageId: Int,
	val text: String?,
	val hasFiles: Boolean,
	val fileNames: List<String>,
	val from: UserDto
)