package com.parodison.sgp4.time

import kotlin.time.Instant

/** Fecha juliana correspondiente a este instante (UT1 ~ UTC para estos propósitos). */
internal fun Instant.toJulianDate(): Double {
    val secondsSinceUnixEpoch = epochSeconds + nanosecondsOfSecond / 1_000_000_000.0
    return secondsSinceUnixEpoch / 86400.0 + 2440587.5
}
