package com.parodison.orbital.server.routes

import com.parodison.orbit.core.protocol.WebsocketPayload
import com.parodison.orbital.server.controllers.WebsocketsController
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readBytes
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@OptIn(ExperimentalSerializationApi::class)
fun Route.websocketRoutes() {
    val controller = WebsocketsController()

    webSocket("/clients") {
        var clientId: Uuid? = null


        try {
            val handshakeFrame = withTimeoutOrNull(10.seconds) {
                incoming.receiveCatching().getOrNull()
            }

            if (handshakeFrame !is Frame.Binary) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "handshake no recibido a tiempo"))
                return@webSocket
            }

            val message = Cbor.decodeFromByteArray<WebsocketPayload>(handshakeFrame.readBytes())
            if (message !is WebsocketPayload.ClientHandshake) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "primer mensaje debe ser handshake"))
                return@webSocket
            }
            println(message)


            clientId = message.clientId
            controller.addClient(clientId, this)
            for (frame in incoming) {
                if (frame is Frame.Binary) {
                    val message = Cbor.decodeFromByteArray<WebsocketPayload>(frame.readBytes())
                    controller.dispatchClientMessage(message)
                }
            }
        } finally {
            clientId?.let { controller.removeClient(it) }
        }
    }
    webSocket("/stations") {
        var stationId: Uuid? = null

        try {
            val handshakeFrame = withTimeoutOrNull(10.seconds) {
                incoming.receiveCatching().getOrNull()
            }

            if (handshakeFrame !is Frame.Binary) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "handshake no recibido a tiempo"))
                return@webSocket
            }

            val message = Cbor.decodeFromByteArray<WebsocketPayload>(handshakeFrame.readBytes())
            if (message !is WebsocketPayload.StationHandshake) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "primer mensaje debe ser handshake"))
                return@webSocket
            }

            controller.registerStationIfNotExists(message)
            controller.addStation(message.stationId, this)
            stationId = message.stationId

            for (frame in incoming) {
                when (frame) {
                    is Frame.Binary -> {
                        val payload = Cbor.decodeFromByteArray<WebsocketPayload>(frame.readBytes())
                        controller.dispatchStationMessage(payload)
                    }
                    else -> println(frame)
                }
            }
        } finally {
            stationId?.let { controller.removeStation(it) }
        }
    }}