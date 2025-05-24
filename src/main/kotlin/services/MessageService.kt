package by.vpolkhovsy.services

import by.vpolkhovsy.event.NewMessageEventObserver
import by.vpolkhovsy.repository.ChatEntity
import by.vpolkhovsy.repository.ChatParticipantEntity
import by.vpolkhovsy.repository.ChatParticipantTable
import by.vpolkhovsy.repository.MessageEntity
import by.vpolkhovsy.repository.MessageTable
import by.vpolkhovsy.repository.UserEntity
import by.vpolkhovsy.repository.UserTable
import event.MessageDto
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import rest.FileDto
import rest.NewMessageRequest
import rest.UserDto
import java.time.ZoneId
import kotlin.random.Random

class MessageService {

	fun saveMessage(profileId: Int, newMessageRequest: NewMessageRequest) = transaction {
		val fromUser = UserEntity[profileId]

		val chat = ChatEntity[newMessageRequest.chatId]

		val replyToMessage = newMessageRequest.replyTo?.let {
			MessageEntity.findById(it)
		}

		val new = MessageEntity.new {
			this.userId = fromUser.id
			this.chatId = chat.id
			this.replyToId = replyToMessage?.id
			this.content = newMessageRequest.text
		}

		runBlocking {
			val message = listChatMessages(newMessageRequest.chatId).find { it.id == new.id.value }
			message?.let {
				val participants = ChatParticipantEntity.find { ChatParticipantTable.chatId eq chat.id }
				for (participant in participants) {
					launch {
						NewMessageEventObserver.notify(participant.user.id.value, message)
					}
				}
			}
		}
	}

	fun listChatMessages(chatId: Int): List<MessageDto> = transaction {
		val chat = ChatEntity[chatId]

		val messagesInChat = MessageEntity.find { MessageTable.chat eq chat.id }.sortedBy {
			it.createdAt
		}

		val messageToId = messagesInChat.associateBy { it.id.value }

		val allProfileIds = messagesInChat.flatMap { message ->
			val profileIds = mutableSetOf<Int>()
			var ref: Int? = message.id.value
			while (ref != null) {
				ref = messageToId[ref]?.let {
					profileIds.add(it.userId.value)
					it.replyToId?.value
				}
			}
			profileIds
		}.toSet()

		val userToId = UserEntity.find(UserTable.id inList allProfileIds)
			.associateBy { it.id.value }

		messagesInChat.mapNotNull { mapMessageToDto(it, userToId, messageToId) }
			.toList()
	}

	private fun mapMessageToDto(
		messageEntity: MessageEntity?,
		userToId: Map<Int, UserEntity>,
		messageToId: Map<Int, MessageEntity>
	): MessageDto? {
		return messageEntity?.let {
			val files = it.attachments.map {
				FileDto(
					it.id.value,
					it.filename,
					Random.nextInt(8 * 1000, Int.MAX_VALUE).toLong()
				)
			}

			val replyTo = it.replyToId?.value?.let {
				messageToId[it]
			}

			val profile = it.user.let {
				UserDto(it.id.value, it.username)
			}

			MessageDto(
				it.id.value,
				it.chatId.value,
				it.content,
				files,
				mapMessageToDto(replyTo, userToId, messageToId),
				profile,
				it.createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
			)
		}
	}
}