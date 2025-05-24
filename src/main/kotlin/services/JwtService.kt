package by.vpolkhovsy.services

import by.vpolkhovsy.repository.UserEntity
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.server.application.*
import io.ktor.server.auth.jwt.*
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.temporal.ChronoUnit

class JwtService(
	private val application: Application,
	private val userService: UserService
) {

	private val jwtAudience = getConfigValue("jwt.audience")
	private val jwtDomain = getConfigValue("jwt.domain")
	val jwtRealm = getConfigValue("jwt.realm")
	private val jwtSecret = getConfigValue("jwt.secret")

	val jwtVerifier: JWTVerifier = JWT
		.require(Algorithm.HMAC256(jwtSecret))
		.withAudience(jwtAudience)
		.withIssuer(jwtDomain)
		.build()

	private fun getConfigValue(yamlKeyPath: String): String {
		return application.environment.config.property(yamlKeyPath).getString()
	}

	fun createJwt(user: UserEntity?): String? {
		return user?.let {
			JWT
				.create()
				.withIssuer(jwtDomain)
				.withAudience(jwtAudience)
				.withClaim("id", it.id.value)
				.withClaim("username", it.username)
				.withExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
				.sign(Algorithm.HMAC256(jwtSecret))
		}
	}

	fun validateAndMap(credential: JWTCredential): JwtUser? {
		val idClaim: Claim? = credential.payload.claims["id"]
		val usernameClaim: Claim? = credential.payload.claims["username"]

		if (idClaim == null || usernameClaim == null) {
			return null;
		}

		val id = idClaim.asInt()!!
		val username = usernameClaim.asString()!!

		val userExists = userService.hasUserWithIdAndName(id, username)

		if (userExists) {
			return JwtUser(id, username)
		}

		return null
	}
}

@Serializable
data class JwtUser(val id: Int, val name: String);
