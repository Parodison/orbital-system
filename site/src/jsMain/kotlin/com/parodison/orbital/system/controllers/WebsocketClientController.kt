package com.parodison.orbital.system.controllers

import com.parodison.orbit.core.protocol.WebsocketPayload
import com.parodison.shared.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Connects to the server's `/clients` websocket on init and stays connected (reconnects on
 * drop). [onMessage] gives a composable a [Flow] of just the [WebsocketPayload] subtype
 * it cares about, e.g. `websocketClientController.addEventListener<WebsocketPayload.StatusUpdate>()`.
 */
class WebsocketClientController(
    private val scope: CoroutineScope,
    private val client: HttpClient,
) {
    private val _messages = MutableSharedFlow<WebsocketPayload>(extraBufferCapacity = 64)
    @PublishedApi
    internal val messages = _messages.asSharedFlow()

    private val _session = MutableStateFlow<DefaultClientWebSocketSession?>(null)

    @OptIn(ExperimentalUuidApi::class)
    val clientId: Uuid = Uuid.generateV7()

    init {
        connect()
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun connect() {
        scope.launch {
            val wsUrl = BuildKonfig.BACKEND_URL.replaceFirst("http", "ws")
            while (true) {
                try {
                    client.webSocket("$wsUrl/ws/clients") {
                        sendPayload(WebsocketPayload.ClientHandshake(clientId))
                        _session.value = this
                        try {
                            for (frame in incoming) {
                                if (frame is Frame.Binary) {
                                    val payload = Cbor.decodeFromByteArray(WebsocketPayload.serializer(), frame.readBytes())
                                    _messages.emit(payload)
                                }
                            }
                        } finally {
                            _session.value = null
                        }
                    }
                } catch (e: Exception) {
                    println("WebsocketClientController: conexión perdida (${e.message}), reintentando...")
                }
                delay(1.seconds)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun send(payload: WebsocketPayload) {
        // Espera a que haya sesión en vez de descartar — evita perder mensajes que salen
        // antes de que el handshake termine (p. ej. justo al cargar la página).
        val currentSession = _session.filterNotNull().first()
        currentSession.sendPayload(payload)
    }

    inline fun <reified T : WebsocketPayload> onMessage(): Flow<T> = messages.filterIsInstance<T>()
}

@OptIn(ExperimentalSerializationApi::class)
private suspend fun DefaultClientWebSocketSession.sendPayload(payload: WebsocketPayload) {
    val bytes = Cbor.encodeToByteArray(WebsocketPayload.serializer(), payload)
    send(Frame.Binary(true, bytes))
}