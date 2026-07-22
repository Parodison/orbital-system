package com.parodison.sgp4.propagation

/** Elementos medios ya actualizados a un instante t (minutos desde la época). */
internal data class SecularEffects(
    val semiMajorAxis: Double,
    val meanMotion: Double,
    val eccentricity: Double,
    val meanAnomaly: Double,
    val argPerigee: Double,
    val node: Double,
)
