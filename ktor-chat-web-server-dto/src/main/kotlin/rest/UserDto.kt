package rest

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
	val id: Int,
	val username: String
)

