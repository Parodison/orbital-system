package com.parodison.orbital.server.controllers

import com.parodison.orbit.core.groundstation.dto.GroundStation
import com.parodison.orbit.core.groundstation.dto.GroundStationStatus
import com.parodison.orbit.core.protocol.WebsocketPayload
import com.parodison.orbital.server.db.repositories.GroundStationRepository.findGroundStationById
import com.parodison.orbital.server.db.repositories.GroundStationRepository.updateGroundStationStatus
import com.parodison.orbital.server.db.repositories.GroundStationRepository.upsertGroundStation
import com.parodison.shared.dto.GroundStationDTO
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.sendSerialized
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.uuid.Uuid

class WebsocketsController {
    private val stations = ConcurrentHashMap<Uuid, DefaultWebSocketServerSession>()
    private val clients = ConcurrentHashMap<Uuid, DefaultWebSocketServerSession>()

    suspend fun registerStationIfNotExists(station: WebsocketPayload.StationHandshake) {
        suspendTransaction {
            upsertGroundStation(
                GroundStation(
                    id = station.stationId,
                    name = station.name,
                    status = GroundStationStatus.Connected.Ready
                )
            )
            findGroundStationById(station.stationId)!!
        }
    }

    suspend fun addStation(stationId: Uuid, connection: DefaultWebSocketServerSession) {
        stations[stationId] = connection
    }

    suspend fun removeStation(stationId: Uuid) {
        stations.remove(stationId)
        markStationOffline(stationId)
    }

    private suspend fun markStationOffline(stationId: Uuid) {
        suspendTransaction {
            updateGroundStationStatus(stationId, GroundStationStatus.Idle)
        }
        notifyStationUpdate(WebsocketPayload.StatusUpdate(stationId, GroundStationStatus.Idle))
    }

    fun addClient(clientId: Uuid, connection: DefaultWebSocketServerSession) {
        clients[clientId] = connection
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun notifyStationUpdate(message: WebsocketPayload.StatusUpdate) {
        suspendTransaction {
            updateGroundStationStatus(message.stationId, message.status)
        }
        clients.forEach { (_, session) ->
            session.sendCbor(message)
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun updateStationLocation(message: WebsocketPayload.UpdateStationLocation) {
        clients.forEach { (_, session) ->
            session.sendCbor(message)
        }
    }

    suspend fun dispatchStationMessage(message: WebsocketPayload) {
        when (message) {
            is WebsocketPayload.StatusUpdate -> {
                notifyStationUpdate(message)
            }
            is WebsocketPayload.UpdateStationLocation -> {
                updateStationLocation(message)
            }
            is WebsocketPayload.ResponseCompleteStationStatus -> {
                clients[message.requesterId]?.sendCbor(message)
            }
            is WebsocketPayload.RotatorPositionChange -> {
                clients.forEach { (_, session) -> session.sendCbor(message) }
            }
            else -> {}
        }
    }

    suspend fun dispatchClientMessage(message: WebsocketPayload) {
        when (message) {
            is WebsocketPayload.TrackSatellite -> {
                stations[message.stationId]?.sendCbor(message)
            }
            is WebsocketPayload.StopTracking -> {
                stations[message.stationId]?.sendCbor(message)
            }
            is WebsocketPayload.UpdateStationLocation -> {
                stations[message.stationId]?.sendCbor(message)
            }
            is WebsocketPayload.RequestCompleteStationStatus -> {
                stations[message.stationId]?.sendCbor(message)
            }

            else -> {}
        }
    }

    fun removeClient(clientId: Uuid) {
        clients.remove(clientId)
    }
}