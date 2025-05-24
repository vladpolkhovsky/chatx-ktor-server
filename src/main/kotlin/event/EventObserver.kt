package by.vpolkhovsy.event

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

interface EventObserver<E> {

	fun subscribe(listener: EventListener<E>)
	fun remove(listener: EventListener<E>)
	fun has(listener: EventListener<E>): Boolean
	suspend fun notify(userId: Int, event: E)
}

interface EventListener<in T> {

	val userId: Int
	suspend fun onEvent(event: T)
}

abstract class AbstractEventObserver<T> : EventObserver<T> {

	private val listeners: MutableMap<Int, MutableList<EventListener<T>>> = mutableMapOf()

	override fun has(listener: EventListener<T>): Boolean {
		return listeners[listener.userId]?.isNotEmpty() ?: false
	}

	override fun remove(listener: EventListener<T>) {
		listeners[listener.userId]?.removeIf { it == listener }
	}

	override fun subscribe(listener: EventListener<T>) {
		listeners.computeIfAbsent(listener.userId) { mutableListOf() }.add(listener)
	}

	override suspend fun notify(userId: Int, event: T) = coroutineScope {
		for (listener in listeners[userId] ?: emptyList()) {
			launch {
				runCatching { listener.onEvent(event) }
					.exceptionOrNull()?.let { print(it) }
			}
		}
	}
}

abstract class AbstractEventListener<in T> : EventListener<T> {

	abstract val uniqueListenerId: Long

	override fun hashCode(): Int {
		return Objects.hash(userId, uniqueListenerId)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as AbstractEventListener<*>

		return uniqueListenerId == other.uniqueListenerId &&
			userId == other.userId
	}
}

class SimpleEventListener<in T>(
	override val uniqueListenerId: Long,
	override val userId: Int,
	val callback: suspend (event: T) -> Unit
) : AbstractEventListener<T>() {

	override suspend fun onEvent(event: T) {
		callback(event)
	}
}