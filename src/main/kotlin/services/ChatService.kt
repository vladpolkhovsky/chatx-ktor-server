package by.vpolkhovsy.services

import by.vpolkhovsy.event.JoinChatEventObserver
import by.vpolkhovsy.repository.ChatCodeEntity
import by.vpolkhovsy.repository.ChatCodeTable
import by.vpolkhovsy.repository.ChatEntity
import by.vpolkhovsy.repository.ChatParticipantEntity
import by.vpolkhovsy.repository.ChatParticipantTable
import by.vpolkhovsy.repository.ChatTable
import by.vpolkhovsy.repository.UserEntity
import by.vpolkhovsy.repository.UserTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import rest.ChatCodeDto
import rest.ChatDto
import rest.ChatMemberDto
import rest.CreateChatRequest
import rest.UserDto
import java.time.LocalDateTime
import java.util.*

class ChatService {

	fun createChat(profileId: Int, createRequest: CreateChatRequest): ChatDto = transaction {
		val userEntity = UserEntity[profileId]

		val chatEntity = ChatEntity.new {
			this.chatName = createRequest.chatName
			this.createdBy = userEntity.id
			this.createdAt = LocalDateTime.now()
		}

		val participantId = CompositeID {
			it[ChatParticipantTable.chatId] = chatEntity.id.value
			it[ChatParticipantTable.userId] = userEntity.id.value
		}

		val participantEntity = ChatParticipantEntity.new(participantId) {
			this.isAdmin = true
		}

		val chatDto = ChatDto(
			id = chatEntity.id.value,
			name = chatEntity.chatName,
			members = listOf(
				ChatMemberDto(
					chatId = participantEntity.chat.id.value,
					user = UserDto(
						id = participantEntity.user.id.value,
						username = participantEntity.user.username,
					)
				)
			),
			lastMessageTimestamp = 0
		)

		runBlocking {
			JoinChatEventObserver.notify(
				participantEntity.user.id.value,
				chatDto
			)
		}

		chatDto
	}

	fun list(profileId: Int): List<ChatDto> = transaction {
		val userEntity = UserEntity.findById(profileId)

		if (userEntity == null) {
			return@transaction emptyList()
		}

		val participants = ChatParticipantEntity.find { ChatParticipantTable.userId eq userEntity.id }.sortedBy {
			it.chat.createdAt
		}

		participants
			.map { it -> it.chat }
			.map { chatEntity ->
				ChatDto(
					id = chatEntity.id.value,
					name = chatEntity.chatName,
					members = chatEntity.participants.map { participant ->
						ChatMemberDto(
							chatId = participant.chat.id.value,
							user = UserDto(
								id = participant.user.id.value,
								username = participant.user.username,
							)
						)
					},
					lastMessageTimestamp = 0
				)
			}
	}

	fun listAllMembers(chatId: Int): List<UserDto> = transaction {
		val chat = ChatEntity[chatId]

		ChatParticipantEntity.find { ChatParticipantTable.chatId eq chat.id }.notForUpdate()
			.map {
				UserDto(it.user.id.value, it.user.username)
			}
	}

	fun getChatCode(profileId: Int, chatId: Int): ChatCodeDto = transaction {
		val chat = ChatEntity[chatId]
		val user = UserEntity[profileId]

		val participant = chat.participants.first { it.userId == user.id }
		if (participant.isAdmin) {
			val code = ChatCodeEntity.find { ChatCodeTable.chatId eq chat.id }
				.toList().firstOrNull() ?: ChatCodeEntity.new {
				this.chatId = chat.id
				this.code = UUID.randomUUID().toString().replace("-", "").substring(0, 8)
			}
			return@transaction ChatCodeDto(
				code.code,
				code.chatId.value
			)
		}

		throw IllegalStateException("Not a admin")
	}

	fun joinToChat(profileId: Int, code: String) = transaction {
		val chatCode = ChatCodeEntity.find { ChatCodeTable.code eq code }.first()
		val chat = ChatEntity.find { ChatTable.id eq chatCode.chatId }.first()
		val user = UserEntity.find { UserTable.id eq profileId }.first()
		val participant = ChatParticipantEntity.find { ChatParticipantTable.chatId eq chatCode.chatId and (ChatParticipantTable.userId eq user.id) }.firstOrNull()

		if (participant == null) {
			val participantId = CompositeID {
				it[ChatParticipantTable.chatId] = chat.id
				it[ChatParticipantTable.userId] = user.id
			}

			val participantEntity = ChatParticipantEntity.new(participantId) {
				this.isAdmin = false
			}

			val chatDto = ChatDto(
				id = chat.id.value,
				name = chat.chatName,
				members = listOf(
					ChatMemberDto(
						chatId = participantEntity.chat.id.value,
						user = UserDto(
							id = participantEntity.user.id.value,
							username = participantEntity.user.username,
						)
					)
				),
				lastMessageTimestamp = 0
			)

			runBlocking {
				JoinChatEventObserver.notify(
					profileId,
					chatDto
				)
			}
		}
	}
}