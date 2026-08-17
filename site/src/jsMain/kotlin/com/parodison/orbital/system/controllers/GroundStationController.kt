package com.parodison.orbital.system.controllers

import com.parodison.orbit.core.groundstation.dto.GroundStation
import com.parodison.orbit.core.groundstation.dto.GroundStationStatus
import com.parodison.orbit.core.protocol.WebsocketPayload
import com.parodison.orbit.core.satellite.ObserverCoordinates
import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.orbital.system.components.groundstation.MockTestGroundStationId
import com.parodison.orbital.system.components.groundstation.mockGroundStation
import com.parodison.orbital.system.components.groundstation.mockGroundStationStatuses
import com.parodison.shared.dto.Page
import com.parodison.shared.resources.GroundStationResource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.resources.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid


sealed class GroundStationListState {
    object Idle : GroundStationListState()
    object Loading : GroundStationListState()
    data class Success(val data: Page<GroundStation>) : GroundStationListState()
    data class Error(val message: String) : GroundStationListState()
}

class GroundStationController(
    private val client: HttpClient,
    private val wsController: WebsocketClientController,
    private val scope: CoroutineScope,
) {
    private val _groundStationListState = MutableStateFlow<GroundStationListState>(GroundStationListState.Idle)
    val groundStationListState: StateFlow<GroundStationListState> = _groundStationListState

    private val _selectedStation = MutableStateFlow<GroundStation?>(null)
    val selectedStation: StateFlow<GroundStation?> = _selectedStation

    private val mockTestStatuses = mockGroundStationStatuses()

    init {
        loadGroundStationList()
        handleIncomingMessages()
    }

    fun loadGroundStationList(
        searchText: String? = null,
        page: Int = 1,
        pageSize: Int = 100,
    ) {
        _groundStationListState.value = GroundStationListState.Loading
        scope.launch {
            try {
                val response = client.get(
                    GroundStationResource(searchText = searchText, page = page, pageSize = pageSize)
                )
                if (response.status.isSuccess()) {
                    val data = response.body<Page<GroundStation>>()
                    _groundStationListState.value = GroundStationListState.Success(data)
                    requestAllStationsData()
                    appendMockTestStation()
                } else {
                    _groundStationListState.value = GroundStationListState.Error(
                        "Ha ocurrido un error con status code ${response.status.value}"
                    )
                }
            } catch (e: ResponseException) {
                _groundStationListState.value = GroundStationListState.Error(e.message.orEmpty())
            } catch (e: Exception) {
                _groundStationListState.value = GroundStationListState.Error(e.message.orEmpty())
            }
        }
    }

    suspend fun requestAllStationsData() {
        val current = _groundStationListState.value
        if (current !is GroundStationListState.Success) return

        current.data.content.forEach { station ->
            wsController.send(WebsocketPayload.RequestCompleteStationStatus(stationId = station.id, requesterId = wsController.clientId))
        }
    }

    private fun handleIncomingMessages() {
        wsController.onMessage<WebsocketPayload.StatusUpdate>()
            .onEach { update -> updateStationStatus(update.stationId, update.status) }
            .launchIn(scope)
        wsController.onMessage<WebsocketPayload.ResponseCompleteStationStatus>()
            .onEach { update -> updateStationCompleteData(update.station) }
            .launchIn(scope)
        wsController.onMessage<WebsocketPayload.RotatorPositionChange>()
            .onEach { payload -> updateRotatorPosition(payload) }
            .launchIn(scope)
        wsController.onMessage<WebsocketPayload.UpdateStationLocation>()
            .onEach { payload -> applyCoordinates(payload.stationId, payload.coordinates) }
            .launchIn(scope)
    }

    private fun updateRotatorPosition(payload: WebsocketPayload.RotatorPositionChange) {
        val current = _groundStationListState.value
        if (current !is GroundStationListState.Success) return
        val items = current.data.content.map { item ->
            if (item.id == payload.stationId) item.copy(rotatorPosition = payload.rotatorPosition) else item
        }
        _groundStationListState.value = GroundStationListState.Success(
            current.data.copy(content = items)
        )
        _selectedStation.value?.let { selected ->
            if (selected.id == payload.stationId) {
                _selectedStation.value = selected.copy(rotatorPosition = payload.rotatorPosition)
            }
        }
    }

    private fun updateStationCompleteData(station: GroundStation) {
        val current = _groundStationListState.value
        if (current !is GroundStationListState.Success) return
        val items = current.data.content.map { item ->
            if (item.id == station.id) station else item
        }
        _groundStationListState.value = GroundStationListState.Success(
            current.data.copy(content = items)
        )
        _selectedStation.value?.let { selected ->
            if (selected.id == station.id) {
                _selectedStation.value = station
            }
        }
    }

    private fun updateStationStatus(stationId: Uuid, status: GroundStationStatus) {
        val current = _groundStationListState.value
        if (current is GroundStationListState.Success) {
            val updatedItems = current.data.content.map { station ->
                if (station.id == stationId) station.copy(status = status) else station
            }
            _groundStationListState.value = GroundStationListState.Success(
                current.data.copy(content = updatedItems)
            )
        }

        _selectedStation.value?.let { selected ->
            if (selected.id == stationId) {
                _selectedStation.value = selected.copy(status = status)
            }
        }
    }

    /** Agrega la "Estación prueba" mock a la lista, junto con lo que devuelve la API. */
    private fun appendMockTestStation() {
        val current = _groundStationListState.value
        if (current !is GroundStationListState.Success) return
        if (current.data.content.any { it.id == MockTestGroundStationId }) return
        _groundStationListState.value = GroundStationListState.Success(
            current.data.copy(content = current.data.content + mockGroundStation(status = mockTestStatuses.first()))
        )
    }

    /** Alterna el status de la "Estación prueba" mock entre los distintos [GroundStationStatus] de ejemplo. */
    fun cycleMockTestStationStatus() {
        val current = _groundStationListState.value
        if (current !is GroundStationListState.Success) return
        val mockStation = current.data.content.firstOrNull { it.id == MockTestGroundStationId } ?: return
        val currentIndex = mockTestStatuses.indexOf(mockStation.status).coerceAtLeast(0)
        val nextStatus = mockTestStatuses[(currentIndex + 1) % mockTestStatuses.size]
        updateStationStatus(MockTestGroundStationId, nextStatus)
    }

    fun selectStation(station: GroundStation) {
        _selectedStation.value = station
    }

    fun clearSelectedStation() {
        _selectedStation.value = null
    }

    fun trackSatellite(stationId: Uuid, omm: OrbitMeanElementsMessage) {
        scope.launch {
            wsController.send(WebsocketPayload.TrackSatellite(stationId, omm))
        }
    }

    fun stopTracking(stationId: Uuid) {
        scope.launch {
            wsController.send(WebsocketPayload.StopTracking(stationId))
        }
    }

    fun updateStationLocation(stationId: Uuid, coordinates: ObserverCoordinates) {
        scope.launch {
            wsController.send(WebsocketPayload.UpdateStationLocation(stationId, coordinates))
        }

        // Optimistic local update — la estación va a confirmarlo por su cuenta (ver
        // WebsocketPayload.UpdateStationLocation en handleIncomingMessages), esto solo evita
        // esperar el viaje de ida y vuelta para que quien lo pidió lo vea reflejado.
        applyCoordinates(stationId, coordinates)
    }

    private fun applyCoordinates(stationId: Uuid, coordinates: ObserverCoordinates) {
        val current = _groundStationListState.value
        if (current is GroundStationListState.Success) {
            val updatedItems = current.data.content.map { station ->
                if (station.id == stationId) station.copy(coordinates = coordinates) else station
            }
            _groundStationListState.value = GroundStationListState.Success(current.data.copy(content = updatedItems))
        }
        _selectedStation.value?.let { selected ->
            if (selected.id == stationId) {
                _selectedStation.value = selected.copy(coordinates = coordinates)
            }
        }
    }
}
