package com.parodison.shared.client

import com.parodison.shared.dto.Transmitter
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class SatnogsDbClient(private val httpClient: HttpClient) {
    suspend fun getTransmitters(noradCatId: Long): List<Transmitter> =
        httpClient.get {
            parameter("norad_cat_id", noradCatId)
            parameter("format", "json")
        }.body()
}

private val satnogsDbHttpClient = HttpClientFactory.create {
    defaultRequest { url("https://db.satnogs.org/api/transmitters/") }
}

val satnogsdbClient = SatnogsDbClient(satnogsDbHttpClient)
