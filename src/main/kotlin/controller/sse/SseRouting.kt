package by.vpolkhovsy.controller.sse

import by.vpolkhovsy.config.JWT_PROVIDER_NAME
import by.vpolkhovsy.services.JwtUser
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kotlinx.coroutines.delay

fun Application.configureSseRouting() {
	routing {
		route("/sse") {
			authenticate(JWT_PROVIDER_NAME) {
				sse("/message-events") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					repeat(2001) {

						delay(1000)
					}
				}
			}
		}
	}
}