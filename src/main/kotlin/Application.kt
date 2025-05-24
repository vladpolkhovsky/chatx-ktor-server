package by.vpolkhovsy

import by.vpolkhovsy.config.configureDatabases
import by.vpolkhovsy.config.configureSecurity
import by.vpolkhovsy.config.configureSerialization
import by.vpolkhovsy.config.configureSse
import by.vpolkhovsy.controller.rest.configureRestChatRouting
import by.vpolkhovsy.controller.rest.configureRestMessageRouting
import by.vpolkhovsy.controller.rest.configureRestUserRouting
import by.vpolkhovsy.controller.sse.configureSseRouting
import by.vpolkhovsy.services.ChatService
import by.vpolkhovsy.services.JwtService
import by.vpolkhovsy.services.MessageService
import by.vpolkhovsy.services.UserService
import io.ktor.server.application.*

fun main(args: Array<String>) {
	io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
	val userService = UserService()
	val chatService = ChatService()
	val messageService = MessageService()
	val jwtService = JwtService(this, userService)

	configureDatabases()
	configureSerialization()
	configureSse()
	configureSecurity(jwtService)
	configureRestUserRouting(userService, jwtService)
	configureRestChatRouting(chatService)
	configureRestMessageRouting(messageService)
	configureSseRouting()
}
