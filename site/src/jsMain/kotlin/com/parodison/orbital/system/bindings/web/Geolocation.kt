package com.parodison.orbital.system.bindings.web

external interface Geolocation {
    fun getCurrentPosition(
        successCallback: (Position) -> Unit,
        errorCallback: ((PositionError) -> Unit)? = definedExternally,
        options: PositionOptions? = definedExternally,
    )
    fun watchPosition(
        successCallback: (Position) -> Unit,
        errorCallback: ((PositionError) -> Unit)? = definedExternally,
        options: PositionOptions? = definedExternally,
    ): Int
    fun clearWatch(watchId: Int)
}

external interface Position {
    val coords: Coordinates
    val timestamp: Double
}

external interface Coordinates {
    val latitude: Double
    val longitude: Double
    val altitude: Double?
    val altitudeAccuracy: Double?
    val accuracy: Double
}

external interface PositionError {
    val code: Int
    val message: String
}

external interface PositionOptions {
    var enableHighAccuracy: Boolean
    var timeout: Int
    var maximumAge: Int
}

val org.w3c.dom.Navigator.geolocation: Geolocation
    get() = asDynamic().geolocation as Geolocation