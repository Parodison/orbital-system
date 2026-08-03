package com.parodison.orbital.system.controllers

import com.parodison.orbital.system.bindings.web.Position
import com.parodison.orbital.system.bindings.web.geolocation
import com.parodison.orbital.system.bindings.web.positionOptions
import com.parodison.orbit.core.satellite.ObserverCoordinates
import kotlinx.browser.window
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface GeolocationState {
    object Idle : GeolocationState
    data class Tracking(
        val position: Position
    ) : GeolocationState
    data class Denied(val cause: String) : GeolocationState
}

/** Convierte la posición trackeada (si la hay) a [ObserverCoordinates], para look angles/pasadas. */
fun GeolocationState.toObserverOrNull(): ObserverCoordinates? {
    val coords = (this as? GeolocationState.Tracking)?.position?.coords ?: return null
    return ObserverCoordinates(
        latitudeDeg = coords.latitude,
        longitudeDeg = coords.longitude,
        altitudeKm = (coords.altitude ?: 0.0) / 1000.0,
    )
}

class GeolocationController {
    private val _geolocationState = MutableStateFlow<GeolocationState>(GeolocationState.Idle)
    val geolocationState = _geolocationState.asStateFlow()

    private var watchId: Int? = null

    init {
        startGeolocationTracking()
    }

    fun startGeolocationTracking() {

        watchId = window.navigator.geolocation.watchPosition(
            successCallback = { _geolocationState.value = GeolocationState.Tracking(it) },
            errorCallback = { _geolocationState.value = GeolocationState.Denied(cause = it.message) },
            options = positionOptions(
                enableHighAccuracy = true,
                timeout = 300,
                maximumAge = 0
            )
        )
    }

    fun stopGeolocationTracking() {
        watchId?.let { window.navigator.geolocation.clearWatch(it) }
        watchId = null
        _geolocationState.value = GeolocationState.Idle
    }
}