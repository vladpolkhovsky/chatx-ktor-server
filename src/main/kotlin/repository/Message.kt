package by.vpolkhovsy.repository

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object MessageTable : IntIdTable("$SCHEMA_NAME.chat_messages") {
	val user = reference("user_id", UserTable.id)
	val replyTo = optReference("reply_to", MessageTable)
	val chat = reference("chat_id", ChatTable.id)
	val content = varchar("content", 8192).nullable()
	val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

class MessageEntity(id: EntityID<Int>) : IntEntity(id) {
	companion object : IntEntityClass<MessageEntity>(MessageTable)

	var userId by MessageTable.user
	var replyToId by MessageTable.replyTo
	var chatId by MessageTable.chat
	var content by MessageTable.content
	val createdAt by MessageTable.createdAt

	val attachments by FileEntity referrersOn FileTable.message
	val user by UserEntity referencedOn MessageTable.user
}