package by.vpolkhovsy.utils

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

object BcryptHolder {
	val bCryptPasswordEncoder = BCryptPasswordEncoder()
}

fun encodePassword(password: String): String {
	return BcryptHolder.bCryptPasswordEncoder.encode(password)
}

fun verifyPassword(passwordRaw: String, passwordHash: String): Boolean {
	return BcryptHolder.bCryptPasswordEncoder.matches(passwordRaw, passwordHash)
}