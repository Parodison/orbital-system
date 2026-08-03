package com.parodison.shared.client

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.cbor.cbor
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json

object HttpClientFactory {
    @OptIn(ExperimentalSerializationApi::class)
    fun create(configure: HttpClientConfig<*>.() -> Unit = {}): HttpClient = HttpClient {
        install(ContentNegotiation) {
            val jsonSerializer = Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            }

            json(jsonSerializer)
            json(jsonSerializer, contentType = ContentType.Text.Plain)

            cbor(Cbor {
                ignoreUnknownKeys = true
            })
        }
        configure()
    }
}
