package by.vpolkhovsy.repository

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object FileTable : IntIdTable("$SCHEMA_NAME.files") {
	val content = blob("content")
	val filename = varchar("filename", 64)
	val message = reference("message_id", MessageTable.id)
	val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

class FileEntity(id: EntityID<Int>) : IntEntity(id) {
	companion object : IntEntityClass<FileEntity>(FileTable)

	var createdAt by FileTable.createdAt
	var filename by FileTable.filename
	var content by FileTable.content
	var message by FileTable.message
}