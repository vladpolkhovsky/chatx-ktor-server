package by.vpolkhovsy.controller.rest

import by.vpolkhovsy.config.JWT_PROVIDER_NAME
import by.vpolkhovsy.event.NewMessageEventObserver
import by.vpolkhovsy.event.SimpleEventListener
import by.vpolkhovsy.services.JwtUser
import by.vpolkhovsy.services.MessageService
import event.MessageDto
import io.ktor.http.*
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
import rest.NewMessageRequest
import kotlin.time.Duration.Companion.seconds

fun Application.configureRestMessageRouting(
	messageService: MessageService
) {
	routing {
		route("/message") {
			authenticate(JWT_PROVIDER_NAME) {
				post("/") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					val createRequest = call.receive<NewMessageRequest>()
					messageService.saveMessage(principal.id, createRequest)
					call.respond(status = HttpStatusCode.Created, message = "Send to chat " + createRequest.chatId)
				}
				get("/{chatId}") {
					val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!
					val chatId = call.request.pathVariables["chatId"]!!
					call.respond(messageService.listChatMessages(chatId.toInt()))
				}
				route("/sse") {
					sse("/new") {
						val principal = call.principal<JwtUser>(JWT_PROVIDER_NAME)!!

						val simpleNewMessageListener =
							SimpleEventListener<MessageDto>(System.currentTimeMillis(), principal.id) { messageDto ->
								val dataJson = Json.encodeToString(messageDto)
								send(ServerSentEvent(data = dataJson, id = messageDto.id.toString()))
							}

						coroutineContext.get(Job)?.invokeOnCompletion {
							NewMessageEventObserver.remove(simpleNewMessageListener)
						}

						NewMessageEventObserver.subscribe(simpleNewMessageListener)

						send(comments = "Connected user $principal")

						while (NewMessageEventObserver.has(simpleNewMessageListener)) {
							delay(5.seconds)
							send(comments = "Connected user $principal")
						}
					}
				}
			}
		}
	}
}