package by.vpolkhovsy.repository

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

const val SCHEMA_NAME = "ktor_chat"

object UserTable : IntIdTable("$SCHEMA_NAME.users") {
    val username = varchar("username", 64).uniqueIndex("username_unique_index")
    val password = varchar("password_hash", 70)
    val createdAt = timestamp("created_at").default(Instant.now())
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UserTable)

    var username by UserTable.username
    var passwordHash by UserTable.password
    var createdAt by UserTable.createdAt
}