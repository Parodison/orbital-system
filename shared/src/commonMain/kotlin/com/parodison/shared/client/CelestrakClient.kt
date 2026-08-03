package com.parodison.shared.client

import co.touchlab.kermit.Logger
import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.shared.dto.SatGroup
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

/** Celestrak returns this when a group is rate-limited (no new GP data since the last successful pull). */
class CelestrakForbiddenException(group: SatGroup) :
    Exception("Celestrak devolvió 403 (Forbidden) para el grupo ${group.displayName} — probablemente rate-limited")

class CelestrakClient(private val httpClient: HttpClient) {
    private val logger = Logger.withTag("CelestrakClient")

    suspend fun getGroup(group: SatGroup): List<OrbitMeanElementsMessage> {
        val response = httpClient.get {
            parameter("GROUP", group.groupValue)
            parameter("FORMAT", "json")
            contentType(ContentType.Application.Json)
        }
        if (response.status == HttpStatusCode.Forbidden) {
            throw CelestrakForbiddenException(group)
        }
        return response.body()
    }

    suspend fun getAllGroups(): Map<SatGroup, List<OrbitMeanElementsMessage>> = coroutineScope {
        val semaphore = Semaphore(3)
        SatGroup.entries
            .map { group ->
                async {
                    logger.d { "Obteniendo datos satelitales para el grupo: ${group.displayName}" }
                    val elements = runCatching { semaphore.withPermit { getGroup(group) } }
                        .onSuccess { logger.i { "Se obtuvieron ${it.size} elementos para el grupo ${group.displayName}" } }
                        .onFailure { error ->
                            when (error) {
                                is CelestrakForbiddenException -> logger.w { error.message.orEmpty() }
                                else -> logger.e(error) { "Fallo inesperado obteniendo el grupo ${group.displayName}" }
                            }
                        }
                        .getOrNull()
                    group to elements
                }
            }
            .awaitAll()
            .mapNotNull { (group, elements) -> elements?.let { group to it } }
            .toMap()
    }
}

private val celestrakHttpClient = HttpClientFactory.create {
    defaultRequest { url("https://celestrak.org/NORAD/elements/gp.php") }
}

val celestrakClient = CelestrakClient(celestrakHttpClient)
