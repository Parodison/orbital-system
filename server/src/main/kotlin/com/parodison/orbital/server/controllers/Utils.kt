package com.parodison.orbital.server.controllers

import com.parodison.orbit.core.protocol.WebsocketPayload
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor

@OptIn(ExperimentalSerializationApi::class)
suspend fun DefaultWebSocketServerSession.sendCbor(payload: WebsocketPayload) {
    val bytes = Cbor.encodeToByteArray(WebsocketPayload.serializer(), payload)
    send(Frame.Binary(true, bytes))
}