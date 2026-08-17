package com.parodison.orbital.system.components.groundstation

import androidx.compose.runtime.*
import com.parodison.orbit.core.groundstation.dto.GroundStation
import com.parodison.orbit.core.groundstation.dto.GroundStationStatus
import com.parodison.orbit.core.satellite.ObserverCoordinates
import com.parodison.orbit.core.satellite.model.OrbitMeanElementsMessage
import com.parodison.orbit.core.satellite.model.toSatellite
import com.parodison.orbital.system.core.AppColors
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Text
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Composable
fun GroundStationStatusContainer(station: GroundStation) {
    Box(
        Modifier
            .fillMaxWidth()
            .border(1.px, LineStyle.Solid, AppColors.OutlineGray)
            .borderRadius(8.px)
            .padding(16.px),
    ) {
        when (val status = station.status) {
            is GroundStationStatus.Connected.PreparingForPass ->
                PreparingForPassContent(status, station.coordinates!!)
            else -> Unit
        }
    }
}

@Composable
private fun PreparingForPassContent(
    status: GroundStationStatus.Connected.PreparingForPass,
    observer: ObserverCoordinates,
) {
    var now by remember { mutableStateOf(Clock.System.now()) }
    var losTime by remember { mutableStateOf<Instant?>(null) }

    LaunchedEffect(status) {
        val satellite = status.omm.toSatellite()
        val pass = satellite.nextPassesFrom(
            observer = observer,
            from = status.aosTime,
            searchWindow = 1.hours,
        ).firstOrNull()
        losTime = pass?.los
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000.milliseconds)
            now = Clock.System.now()
        }
    }

    val timeUntilAos = status.aosTime - now
    val passDuration = losTime?.let { it - status.aosTime }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            InfoPanel(
                modifier = Modifier.weight(1f),
                label = "ESTADO ACTUAL",
                content = { Text("PREPARANDO PASE") },
            )
            InfoPanel(
                modifier = Modifier.weight(1f),
                label = "Satélite preparado",
                content = { Text("${status.omm.objectName} (NORAD ${status.omm.noradCatId})") },
            )
        }

        InfoPanel(
            label = "AOS",
            content = {
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) { Text("hola") }
                    Column(Modifier.weight(1f)) { Text("en ${formatDuration(timeUntilAos)}") }
                }
            },
        )

        InfoPanel(
            label = "Duración estimada del pase",
            content = { Text(passDuration?.let { formatDuration(it) } ?: "calculando...") },
        )
    }
}

@Composable
private fun InfoPanel(
    modifier: Modifier = Modifier,
    label: String,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .backgroundColor(AppColors.DarkBlueSecondary)
            .borderRadius(6.px)
            .padding(12.px)
            .margin(bottom = 8.px),
    ) {
        Column {
            Text(label)
            content()
        }
    }
}

private fun formatDuration(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds.coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
}

private fun Long.pad(): String = toString().padStart(2, '0')

// --- Datos mock, para probar el componente sin backend real ---

fun mockOrbitMeanElementsMessage(): OrbitMeanElementsMessage = OrbitMeanElementsMessage(
    objectName = "ARAPY I",
    objectId = "2024-001A",
    epoch = Clock.System.now(),
    meanMotion = 15.5,
    eccentricity = 0.0001,
    inclination = 51.6,
    raOfAscNode = 120.0,
    argOfPericenter = 90.0,
    meanAnomaly = 0.0,
    ephemerisType = 0,
    classificationType = "U",
    noradCatId = 56347,
    elementSetNo = 999,
    revAtEpoch = 1,
    bstar = 0.0001,
    meanMotionDot = 0.0,
    meanMotionDdot = 0.0,
)

/** ID fijo de la estación mock, para poder identificarla dentro de la lista de estaciones. */
val MockTestGroundStationId: Uuid = Uuid.parse("00000000-0000-0000-0000-000000000099")

/**
 * Un ejemplo de cada tipo de [GroundStationStatus], para poder alternar entre ellos al mockear.
 * El primero es "preparing_for_pass" para que la estación mock se vea activa y con datos al abrirla.
 */
fun mockGroundStationStatuses(): List<GroundStationStatus> {
    val omm = mockOrbitMeanElementsMessage()
    return listOf(
        GroundStationStatus.Connected.PreparingForPass(
            omm = omm,
            aosAzimuth = 145.2,
            aosTime = Clock.System.now() + 12.minutes + 48.seconds,
        ),
        GroundStationStatus.Connected.Tracking(
            omm = omm,
            azimuth = 210.4,
            elevation = 47.8,
        ),
        GroundStationStatus.Connected.Ready,
        GroundStationStatus.Connected.PassComplete,
        GroundStationStatus.Connected.Error(
            message = "Pérdida de señal con el rotor",
            recoverable = true,
        ),
        GroundStationStatus.Idle,
    )
}

fun mockGroundStation(status: GroundStationStatus = mockGroundStationStatuses().first()): GroundStation = GroundStation(
    id = MockTestGroundStationId,
    name = "Estación prueba",
    coordinates = ObserverCoordinates(
        latitudeDeg = -25.2637,
        longitudeDeg = -57.5759,
        altitudeKm = 0.1,
    ),
    status = status,
)

fun GroundStationStatus.mockLabel(): String = when (this) {
    is GroundStationStatus.Idle -> "Idle"
    is GroundStationStatus.Connected.Ready -> "Ready"
    is GroundStationStatus.Connected.PreparingForPass -> "Preparando pase"
    is GroundStationStatus.Connected.Tracking -> "Tracking"
    is GroundStationStatus.Connected.PassComplete -> "Pase completo"
    is GroundStationStatus.Connected.Error -> "Error"
}

@Composable
fun GroundStationStatusContainerPreview() {
    GroundStationStatusContainer(mockGroundStation())
}