package by.vpolkhovsy.repository

import org.jetbrains.exposed.dao.CompositeEntity
import org.jetbrains.exposed.dao.CompositeEntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.CompositeID
import org.jetbrains.exposed.dao.id.CompositeIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object ChatTable : IntIdTable("$SCHEMA_NAME.chats") {

	val createdBy = reference("created_by_user_id", UserTable.id)
	val chatName = varchar("chat_name", 270)
	val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

object ChatParticipantTable : CompositeIdTable("$SCHEMA_NAME.chats_participants") {

	val chatId = reference("chat_id", ChatTable.id)
	val userId = reference("user_id", UserTable.id)
	val isAdmin = bool("is_admin").default(false)

	init {
		uniqueIndex(chatId, userId)
		addIdColumn(chatId)
		addIdColumn(userId)
	}
}

object ChatCodeTable : IntIdTable("$SCHEMA_NAME.chat_code") {

	val chatId = reference("chat_id", ChatTable.id)
	val code = varchar("code", 8).uniqueIndex()

	init {
		uniqueIndex(chatId)
	}
}

class ChatCodeEntity(id: EntityID<Int>) : IntEntity(id) {
	companion object : IntEntityClass<ChatCodeEntity>(ChatCodeTable)

	var chatId by ChatCodeTable.chatId
	var code by ChatCodeTable.code
}

class ChatEntity(id: EntityID<Int>) : IntEntity(id) {
	companion object : IntEntityClass<ChatEntity>(ChatTable)

	var createdBy by ChatTable.createdBy
	var chatName by ChatTable.chatName
	var createdAt by ChatTable.createdAt

	val participants by ChatParticipantEntity referrersOn ChatParticipantTable.chatId
}

class ChatParticipantEntity(id: EntityID<CompositeID>) : CompositeEntity(id) {
	companion object : CompositeEntityClass<ChatParticipantEntity>(ChatParticipantTable)

	var chatId by ChatParticipantTable.chatId
	var userId by ChatParticipantTable.userId
	var isAdmin by ChatParticipantTable.isAdmin

	val user by UserEntity referencedOn ChatParticipantTable.userId
	val chat by ChatEntity referencedOn ChatParticipantTable.chatId
}