package com.parodison.orbital.system

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.cbor.cbor
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.time.Instant

/**
 * El EPOCH de Celestrak viene sin offset de zona horaria (ej. "2026-07-17T13:44:50.076384"),
 * pero siempre es UTC. `Instant.parse` de kotlin.time exige el offset, así que se lo agregamos
 * si falta antes de parsear.
 */
object CelestrakEpochSerializer : KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("CelestrakEpoch", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val raw = decoder.decodeString()
        val hasOffset = raw.endsWith("Z") || Regex("[+-]\\d{2}:?\\d{2}$").containsMatchIn(raw)
        return Instant.parse(if (hasOffset) raw else "${raw}Z")
    }

    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
}

private val celestrakClient = HttpClient(CIO) {
    install(ClientContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@OptIn(ExperimentalSerializationApi::class)
fun Application.module() {
    install(CORS) {
        allowHost("localhost:8080")
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })

        cbor(Cbor {
            ignoreUnknownKeys = true
        })
    }

    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }

        get("/api/satellites") {
            val group = call.request.queryParameters["GROUP"] ?: "stations"
            val format = call.request.queryParameters["FORMAT"] ?: "json"

            try {
                val response = celestrakClient.get("https://celestrak.org/NORAD/elements/gp.php") {
                    parameter("GROUP", group)
                    parameter("FORMAT", format)
                }
                if (response.status.isSuccess()) {
                    val data = response.body<List<OrbitData>>()
                    call.respond(data)
                } else {
                    call.respond(
                        status = response.status,
                        response.bodyAsText()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                call.respondText(e.message ?: "Error desconocido")
            }
        }
    }
}

@Serializable
data class OrbitData(
    @SerialName("OBJECT_NAME")
    val objectName: String,
    @SerialName("OBJECT_ID")
    val objectId: String,
    @SerialName("EPOCH")
    @Serializable(with = CelestrakEpochSerializer::class)
    val epoch: Instant,
    @SerialName("MEAN_MOTION")
    val meanMotion: Double,
    @SerialName("ECCENTRICITY")
    val eccentricity: Double,
    @SerialName("INCLINATION")
    val inclination: Double,
    @SerialName("RA_OF_ASC_NODE")
    val raOfAscNode: Double,
    @SerialName("ARG_OF_PERICENTER")
    val argOfPericenter: Double,
    @SerialName("MEAN_ANOMALY")
    val meanAnomaly: Double,
    @SerialName("EPHEMERIS_TYPE")
    val ephemerisType: Long,
    @SerialName("CLASSIFICATION_TYPE")
    val classificationType: String,
    @SerialName("NORAD_CAT_ID")
    val noradCatId: Long,
    @SerialName("ELEMENT_SET_NO")
    val elementSetNo: Long,
    @SerialName("REV_AT_EPOCH")
    val revAtEpoch: Long,
    @SerialName("BSTAR")
    val bstar: Double,
    @SerialName("MEAN_MOTION_DOT")
    val meanMotionDot: Double,
    @SerialName("MEAN_MOTION_DDOT")
    val meanMotionDdot: Double,
)