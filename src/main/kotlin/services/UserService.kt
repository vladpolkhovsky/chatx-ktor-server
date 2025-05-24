package by.vpolkhovsy.services

import by.vpolkhovsy.repository.UserEntity
import by.vpolkhovsy.repository.UserTable
import by.vpolkhovsy.utils.encodePassword
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class UserService {

	fun register(username: String, password: String): UserEntity = transaction {
		UserEntity.new {
			this.username = username
			this.passwordHash = encodePassword(password)
		}
	}

	fun getUserWithName(username: String): UserEntity? = transaction {
		UserEntity
			.find { UserTable.username eq username }
			.firstOrNull()
	}

	fun hasUserWithIdAndName(id: Int, username: String): Boolean = transaction {
		UserEntity
			.find { (UserTable.id eq id) and (UserTable.username eq username) }
			.any()
	}
}