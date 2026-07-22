package com.parodison.sgp4.model

/** Posición (km) y velocidad (km/s) en el sistema TEME (True Equator, Mean Equinox). */
data class PositionVelocity(
    val position: Vector3,
    val velocity: Vector3,
)
