package by.vpolkhovsy.controller.rest

import by.vpolkhovsy.config.JWT_PROVIDER_NAME
import by.vpolkhovsy.event.JoinChatEventObserver
import by.vpolkhovsy.event.SimpleEventListener
import by.vpolkhovsy.services.ChatService
import by.vpolkhovsy.services.JwtUser
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import rest.ChatDto
import rest.CreateChatRequest
import kotlin.time.Duration.Companion.seconds

fun Application.configureRestChatRouting(
	chatService: ChatService
) {
	routing {
		route("/chat") {
			authenticate(JWT_PROVIDER_NAME) {
				post("/create") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					val createRequest = call.receive<CreateChatRequest>()
					call.respond(chatService.createChat(principal.id, createRequest))
				}
				get("/") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					call.respond(chatService.list(principal.id))
				}
				get("/{chat-id}/code") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					val chatId = call.request.pathVariables["chat-id"]!!.toInt()
					call.respond(chatService.getChatCode(principal.id, chatId))
				}
				post("/join/code/{code}") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					val code = call.request.pathVariables["code"]!!
					chatService.joinToChat(principal.id, code)
					call.respond(status = HttpStatusCode.Created, message = "Join to chat by code $code")
				}
				get("/members/{chat-id}") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					val chatId = call.request.pathVariables["chat-id"]!!.toInt()
					call.respond(chatService.listAllMembers(chatId))
				}
				route("/sse") {
					sse("/new") {
						val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!

						val simpleJoinChatListener = SimpleEventListener<ChatDto>(System.currentTimeMillis(), principal.id) { chatDto ->
							val dataJson = Json.encodeToString(chatDto)
							send(ServerSentEvent(data = dataJson, id = chatDto.id.toString()))
						}

						coroutineContext.get(Job)?.invokeOnCompletion {
							JoinChatEventObserver.remove(simpleJoinChatListener)
						}

						JoinChatEventObserver.subscribe(simpleJoinChatListener)

						send(comments = "Connected user $principal")

						while (JoinChatEventObserver.has(simpleJoinChatListener)) {
							delay(5.seconds)
							send(comments = "Connected user $principal")
						}
					}
				}
			}
		}
	}
}