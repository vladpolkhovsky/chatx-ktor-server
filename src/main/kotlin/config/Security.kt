package by.vpolkhovsy.config

import by.vpolkhovsy.services.JwtService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

const val JWT_PROVIDER_NAME = "ktor-chat-jwt-provider"

fun Application.configureSecurity(jwtService: JwtService) {

	environment.log.info("Configure security is called")

	authentication {
		jwt(JWT_PROVIDER_NAME) {
			realm = jwtService.jwtRealm
			verifier(jwtService.jwtVerifier)
			validate { jwtService.validateAndMap(it) }
		}
	}
}
