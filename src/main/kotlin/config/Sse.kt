package by.vpolkhovsy.config

import io.ktor.server.application.*
import io.ktor.server.sse.*

fun Application.configureSse() {
	install(SSE)
}