package com.parodison.orbital.system.controllers

import com.parodison.orbit.core.satellite.Satellite
import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.orbit.core.satellite.model.toSatellite
import com.parodison.shared.dto.Page
import com.parodison.shared.dto.SatGroup
import com.parodison.shared.dto.map
import com.parodison.shared.resources.SatelliteResource
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.browser.localStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed class SatelliteListState {
    object Idle : SatelliteListState()
    object Loading : SatelliteListState()
    data class Success(val data: Page<Satellite>) : SatelliteListState()
    data class Error(val message: String) : SatelliteListState()
}

class SatelliteTrackerController(
    val scope: CoroutineScope,
    val client: HttpClient
) {
    private val trackingSatellitesStorage = "tracking_satellites"
    private val _satelliteListState = MutableStateFlow<SatelliteListState>(SatelliteListState.Idle)
    val satelliteListState = _satelliteListState.asStateFlow()

    private val _selectedSatellite = MutableStateFlow<Satellite?>(null)
    val selectedSatellite = _selectedSatellite.asStateFlow()

    private val _trackingSatellites = MutableStateFlow<List<Satellite>>(emptyList())
    val trackingSatellites = _trackingSatellites.asStateFlow()

    private val _trackingDelay = MutableStateFlow<Duration>(300.milliseconds)
    val trackingDelay = _trackingDelay.asStateFlow()

    init {
        restoreTrackedSatellites()
        trackingSatellites.onEach { satellites ->
            val noradIds = satellites.map { it.omm.noradCatId }
            localStorage.setItem(trackingSatellitesStorage, Json.encodeToString(noradIds))
        }
            .launchIn(scope)
    }

    private fun restoreTrackedSatellites() {
        val stored = localStorage.getItem(trackingSatellitesStorage) ?: return
        val noradIds = Json.decodeFromString<List<Long>>(stored)

        scope.launch {
            val satellites = coroutineScope {
                noradIds.map { noradId ->
                    async { fetchSatelliteByNoradId(noradId) }
                }.awaitAll()
            }.filterNotNull()
            _trackingSatellites.value = satellites
        }
    }

    suspend fun fetchSatelliteByNoradId(noradId: Long): Satellite? {
        val response = client.get(SatelliteResource.NoradId(noradId = noradId))
        return if (response.status.isSuccess()) {
            response.body<OrbitMeanElementsMessage>().toSatellite()
        } else null
    }

    fun trackSatellite(satellite: Satellite) {
        _trackingSatellites.value = (_trackingSatellites.value + satellite).distinctBy { it.omm.noradCatId }
    }

    fun untrackSatellite(satellite: Satellite) {
        _trackingSatellites.value = _trackingSatellites.value.filter { it != satellite }
    }

    fun handleSatelliteTrack(satellite: Satellite) {
        val currentList = _trackingSatellites.value
        _trackingSatellites.value = if (currentList.any { it.omm.noradCatId == satellite.omm.noradCatId }) {
            currentList.filter { it.omm.noradCatId != satellite.omm.noradCatId }
        } else {
            (currentList + satellite).distinctBy { it.omm.noradCatId }
        }
    }

    fun updateSelectedSatellite(data: Satellite) {
        _selectedSatellite.value = data
    }

    fun clearSelectedSatellite() {
        _selectedSatellite.value = null
    }

    fun findSatellitesBySearch(
        searchText: String? = null,
        page: Int = 1,
        pageSize: Int = 100,
        group: SatGroup? = null,
    ) {
        scope.launch {
            try {
                val response = client.get(SatelliteResource(
                    searchText = searchText,
                    page = page,
                    pageSize = pageSize,
                    group = group,
                )) {
                    accept(ContentType.Application.Cbor)
                }
                if (response.status.isSuccess()) {
                    val data = response.body<Page<OrbitMeanElementsMessage>>()
                    _satelliteListState.value = SatelliteListState.Success(data.map { it.toSatellite() })
                } else {
                    val error = response.bodyAsText()
                    println(error)
                    _satelliteListState.value = SatelliteListState.Error(
                        "Ha ocurrido un error con status code ${response.status.value}: $error"
                    )
                }

            } catch (e: ResponseException) {
                val errorResponse = e.response.bodyAsText()
                _satelliteListState.value = SatelliteListState.Error(errorResponse)
            } catch (e: Exception) {
                val error = e.message ?: "Ha ocurrido un error inesperado"
                e.printStackTrace()
                _satelliteListState.value = SatelliteListState.Error(error)
            }

        }
    }

    fun resetSatelliteList() {
        _satelliteListState.value = SatelliteListState.Idle
    }
    fun findSatelliteByNoradId(noradId: Long): Satellite? {
        val state = satelliteListState.value
        return if (state is SatelliteListState.Success) {
            state.data.content.find { it.omm.noradCatId == noradId }
        } else {
            null
        }
    }

    fun changeTrackedDelay(delay: Duration) {
        _trackingDelay.value = delay
    }
}