package rest

import kotlinx.serialization.Serializable

@Serializable
data class FileDto(
	val id: Int,
	val name: String,
	val size: Long
)