package by.vpolkhovsy.config

import by.vpolkhovsy.repository.ChatCodeTable
import by.vpolkhovsy.repository.ChatParticipantTable
import by.vpolkhovsy.repository.ChatTable
import by.vpolkhovsy.repository.FileTable
import by.vpolkhovsy.repository.MessageTable
import by.vpolkhovsy.repository.SCHEMA_NAME
import by.vpolkhovsy.repository.UserTable
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
	log.info("Initializing Database")

	val useInMemoryDatabase = environment.config
		.property("database.use-in-memory-database").getString().toBoolean()

	log.debug("Property useInMemoryDatabase is $useInMemoryDatabase")

	if (useInMemoryDatabase) {
		connectToH2()
	} else {
		connectToPostgres()
	}

	val schema = Schema(SCHEMA_NAME)

	transaction {
		addLogger(StdOutSqlLogger)
		SchemaUtils.createSchema(
			schema
		)
		SchemaUtils.setSchema(
			schema
		)
		SchemaUtils.create(
			UserTable,
			ChatTable,
			MessageTable,
			FileTable,
			ChatParticipantTable,
			ChatCodeTable
		)
	}
}

private fun Application.connectToH2() {
	log.info("Connect to H2 in memory database");

	val driverClass = "org.h2.Driver"

	Database.connect(
		url = "jdbc:h2:file:./in-memory;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS $SCHEMA_NAME;",
		driver = driverClass,
		user = "admin",
		password = "",
	)
}

fun Application.connectToPostgres() {
	log.info("Connect to Postgres database");

	val driverClass = "org.postgresql.Driver"

	Database.connect(
		url = environment.config.property("database.postgres.url").getString(),
		driver = driverClass,
		user = environment.config.property("database.postgres.user").getString(),
		password = environment.config.property("database.postgres.password").getString(),
	)
}
