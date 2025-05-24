package rest

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRequest(
	val createdByUserId: Int,
	val chatName: String
) {
}