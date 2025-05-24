package by.vpolkhovsy.controller.rest

import by.vpolkhovsy.config.JWT_PROVIDER_NAME
import by.vpolkhovsy.dto.LoginRequest
import by.vpolkhovsy.services.JwtService
import by.vpolkhovsy.services.JwtUser
import by.vpolkhovsy.services.UserService
import by.vpolkhovsy.utils.verifyPassword
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import rest.UserDto

fun Application.configureRestUserRouting(userService: UserService, jwtService: JwtService) {
	routing {
		route("/user") {
			authenticate(JWT_PROVIDER_NAME) {
				get("/iam") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					call.respond(UserDto(principal.id, principal.name))
				}
			}
			post("/login") {
				val loginRequest = call.receive<LoginRequest>()

				val userByUsername = userService.getUserWithName(loginRequest.username)

				if (userByUsername == null) {
					val registeredUser = userService.register(loginRequest.username, loginRequest.password)
					val token = jwtService.createJwt(registeredUser)
					call.respond(
						mapOf("token" to token)
					)
					return@post
				}

				if (verifyPassword(loginRequest.password, userByUsername.passwordHash)) {
					val token = jwtService.createJwt(userByUsername)
					call.respond(
						mapOf("token" to token)
					)
					return@post
				}

				call.respond(HttpStatusCode.Unauthorized)
			}
		}
	}
}
