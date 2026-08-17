package com.parodison.orbital.system.components.maplibre

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import com.parodison.orbital.system.core.LayerSpecification
import com.parodison.orbital.system.core.MapOptions
import com.parodison.orbital.system.core.circleLayer
import com.parodison.orbital.system.core.fillLayer
import com.parodison.orbital.system.core.geoJsonLineString
import com.parodison.orbital.system.core.geoJsonMultiLineString
import com.parodison.orbital.system.core.geoJsonMultiPolygon
import com.parodison.orbital.system.core.geoJsonPoint
import com.parodison.orbital.system.core.geoJsonPolygon
import com.parodison.orbital.system.core.geoJsonSource
import com.parodison.orbital.system.core.lineLayer
import com.parodison.orbital.system.core.lngLat
import com.parodison.orbital.system.core.markerOptions
import com.parodison.orbital.system.core.maplibregl
import com.parodison.orbital.system.core.onLoad
import com.parodison.orbit.core.sgp4.model.GeodeticCoordinates
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.toAttrs
import kotlinx.browser.document
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLElement
import kotlin.math.abs

/**
 * Handle para controlar un [MapLibreMap] desde fuera de forma imperativa (la API de MapLibre
 * es imperativa por naturaleza: flyTo, addSource, addLayer, etc. no tienen sentido como props
 * declarativas que se re-aplican en cada recomposición).
 *
 * El mapa no existe hasta que el `Div` contenedor se monta en el DOM *y* termina de cargar su
 * style, así que cualquier acción pedida antes de eso ([withMap]) se encola y se ejecuta recién
 * cuando [attach] la asigna.
 */
class MapLibreMapState internal constructor() {
    var map: maplibregl.Map? by mutableStateOf(null)
        private set

    /** True una vez que el mapa subyacente ya existe (con su style cargado) y se puede operar. */
    val isReady: Boolean get() = map != null

    private val pendingActions = mutableListOf<(maplibregl.Map) -> Unit>()

    /** Ejecuta [action] ya mismo si el mapa está listo, o la encola hasta que lo esté. */
    fun withMap(action: (maplibregl.Map) -> Unit) {
        map?.let(action) ?: pendingActions.add(action)
    }

    internal fun attach(instance: maplibregl.Map) {
        map = instance
        val queued = pendingActions.toList()
        pendingActions.clear()
        queued.forEach { it(instance) }
    }

    internal fun detach() {
        map?.remove()
        map = null
    }
}

@Composable
fun rememberMapLibreState(): MapLibreMapState = remember { MapLibreMapState() }

/**
 * Scope expuesto dentro del `content` de [MapLibreMap]: da acceso al [maplibregl.Map] ya creado
 * (con style cargado) para que elementos declarativos ([Marker], [Line], [AnimatedLine]) puedan
 * añadirse/actualizarse/quitarse solos de forma imperativa según su propia vida en la
 * composición. Nuevos tipos de overlay (Popup, Circle, etc.) se agregan igual: extension
 * functions de este mismo scope, compartiendo [map] y el patrón DisposableEffect + LaunchedEffect.
 */
class MapLibreScope internal constructor(val map: maplibregl.Map)

/**
 * Puente declarativo sobre `maplibregl.Map`: monta un `Div` y crea el mapa una sola vez, cuando
 * ese `Div` entra al DOM. [options] solo se usa en ese momento inicial — MapLibre no soporta
 * "cambiar" container/style/center reactivamente reconstruyendo el objeto de opciones, así que
 * actualizaciones posteriores (zoom, center, sources, layers) van por [state] o por [content],
 * no por volver a pasar otro [options].
 *
 * [content] recién se compone cuando el mapa existe *y ya terminó de cargar su style* (evento
 * `load` de MapLibre) — sin eso, `addSource`/`addLayer` (los usa [Line]) tiran error si se
 * llaman antes de tiempo. Gatearlo acá evita que cada overlay declarativo tenga que manejar esa
 * carrera por su cuenta.
 */
@Composable
fun MapLibreMap(
    modifier: Modifier = Modifier,
    options: MapOptions,
    state: MapLibreMapState = rememberMapLibreState(),
    onMapReady: (maplibregl.Map) -> Unit = {},
    content: @Composable MapLibreScope.() -> Unit = {},
) {
    Div(
        attrs = modifier.toAttrs {
            ref { element ->
                options.container = element
                val instance = maplibregl.Map(options)
                instance.onLoad {
                    state.attach(instance)
                    onMapReady(instance)
                }

                onDispose {
                    state.detach()
                }
            }
        }
    )

    val map = state.map
    if (map != null) {
        val scope = remember(map) { MapLibreScope(map) }
        scope.content()
    }
}

/**
 * Marcador declarativo: renderiza [content] (Compose normal, transpilado a HTML) en un `<div>`
 * desconectado del árbol principal vía [renderComposable] — el mismo mecanismo que usa Kobweb
 * para montar la app entera — y ese `<div>` se lo pasa a `maplibregl.Marker` como su elemento.
 *
 * El marcador y su composición interna se crean una sola vez (mientras [MapLibreScope.map] no
 * cambie) y sobreviven a cambios de [lng]/[lat]: esos solo disparan un `setLngLat` imperativo,
 * no recrean el marcador ni pierden el estado interno de [content]. Todo se limpia (`marker.remove()`
 * + `composition.dispose()`) cuando este composable sale de composición.
 */
@Composable
fun MapLibreScope.Marker(
    lng: Double,
    lat: Double,
    anchor: String? = null,
    content: @Composable () -> Unit = {},
) {

    val latestContent = rememberUpdatedState(content)
    val markerHost = remember(map) { document.createElement("div") as HTMLElement }
    var markerInstance by remember(map) { mutableStateOf<maplibregl.Marker?>(null) }

    DisposableEffect(map, markerHost) {
        val composition = renderComposable(root = markerHost) {
            latestContent.value()
        }
        val marker = maplibregl.Marker(markerOptions(element = markerHost, anchor = anchor))
            .setLngLat(lngLat(lng, lat))
            .addTo(map)
        markerInstance = marker

        onDispose {
            marker.remove()
            composition.dispose()
            markerInstance = null
        }
    }

    LaunchedEffect(lng, lat) {
        markerInstance?.setLngLat(lngLat(lng, lat))
    }
}

private var nextOverlayId = 0
private fun uniqueOverlayId(prefix: String): String = "$prefix-${nextOverlayId++}"

/**
 * Primitivo genérico: agrega cualquier fuente GeoJSON + layer de estilo a [MapLibreScope.map] y
 * maneja su ciclo de vida completo (crear una sola vez, actualizar con `setData` cuando cambia
 * [key], limpiar `removeLayer`+`removeSource` al salir de composición). [Line] (y por lo tanto
 * [AnimatedLine]), [Polygon], [MultiPolygon] y [CirclePoint] son wrappers finos y tipados sobre
 * este mismo primitivo — cualquier geometría GeoJSON nueva que haga falta en el futuro
 * (GeometryCollection, etc.) se agrega igual, sin reimplementar el ciclo de vida de source/layer
 * cada vez.
 *
 * @param key identidad de los datos actuales: [data] es `dynamic`/JS y su igualdad estructural
 *   no es confiable, así que [LaunchedEffect] reacciona sobre este valor Kotlin normal en su lugar
 *   (una `List<GeodeticCoordinates>`, un `Pair<Double, Double>`, etc.).
 * @param data arma el GeoJSON (Feature) a partir de [key].
 * @param buildLayer arma el [LayerSpecification] a partir de los ids de source/layer ya
 *   generados; se llama una sola vez al crear el layer — el estilo (color, opacidad, etc.) no es
 *   reactivo hoy, solo la geometría vía [key]/[data].
 */
@Composable
fun <T> MapLibreScope.GeoJsonLayer(
    key: T,
    data: (T) -> dynamic,
    buildLayer: (sourceId: String, layerId: String) -> LayerSpecification,
) {
    val sourceId = remember(map) { uniqueOverlayId("geojson-source") }
    val layerId = remember(map) { uniqueOverlayId("geojson-layer") }

    DisposableEffect(map, sourceId) {
        map.addSource(sourceId, geoJsonSource(data = data(key)))
        map.addLayer(buildLayer(sourceId, layerId))

        onDispose {
            map.removeLayer(layerId)
            map.removeSource(sourceId)
        }
    }

    LaunchedEffect(key) {
        map.getSource(sourceId).setData(data(key))
    }
}

private fun List<GeodeticCoordinates>.toLineCoordinates(): Array<Array<Double>> =
    map { arrayOf(it.longitudeDeg, it.latitudeDeg) }.toTypedArray()

/**
 * Reemplaza los saltos de -180°/180° por longitudes continuas (ej. 175°, 178°, 181°, 184°... en
 * vez de 175°, 178°, -179°, -176°), sumando/restando 360° acumulado cada vez que detecta un
 * salto >180° entre puntos consecutivos.
 *
 * Es la alternativa a [splitAtAntimeridian] para un anillo *cerrado*: un `Polygon` GeoJSON exige
 * que cada anillo cierre en el mismo punto, así que no se puede partir en segmentos como con una
 * línea abierta sin dejar de ser un anillo válido. Con longitudes continuas, en cambio, el
 * anillo entero se puede seguir dibujando de una sola pieza sin el salto visual espurio.
 */
private fun Array<Array<Double>>.unwrapLongitudes(): List<Array<Double>> {
    if (isEmpty()) return emptyList()

    var offset = 0.0
    var previousLng = this[0][0]
    return map { point ->
        val lng = point[0]
        val delta = lng - previousLng
        when {
            delta > 180.0 -> offset -= 360.0
            delta < -180.0 -> offset += 360.0
        }
        previousLng = lng
        arrayOf(lng + offset, point[1])
    }
}

/** Cierra el anillo si no vino cerrado (primer punto == último), como exige un `Polygon` GeoJSON. */
private fun List<GeodeticCoordinates>.toRingCoordinates(): Array<Array<Double>> {
    val ring = toLineCoordinates().unwrapLongitudes()
    val closedRing = if (ring.isEmpty() || ring.first().contentEquals(ring.last())) {
        ring
    } else {
        ring + listOf(ring.first())
    }
    return closedRing.toTypedArray()
}

private const val ANTIMERIDIAN_JUMP_THRESHOLD_DEG = 180.0

/** Corta en segmentos nuevos cada vez que salta más de 180° en longitud (cruce del antimeridiano). */
private fun List<GeodeticCoordinates>.splitAtAntimeridian(): List<List<GeodeticCoordinates>> {
    if (isEmpty()) return emptyList()

    val segments = mutableListOf(mutableListOf(first()))
    for (i in 1 until size) {
        val jumpDeg = abs(this[i].longitudeDeg - this[i - 1].longitudeDeg)
        if (jumpDeg > ANTIMERIDIAN_JUMP_THRESHOLD_DEG) segments.add(mutableListOf())
        segments.last().add(this[i])
    }
    return segments
}

/**
 * GeoJSON de [this] como `LineString` si no cruza el antimeridiano, o `MultiLineString` (un
 * segmento por cada cruce) si sí — un `LineString` continuo dibujaría una línea recta espuria
 * atravesando el mapa entero en cada salto de +180° a -180° (o viceversa).
 */
private fun List<GeodeticCoordinates>.toLineGeoJson(): dynamic {
    val segments = splitAtAntimeridian()
    return if (segments.size <= 1) {
        geoJsonLineString(toLineCoordinates())
    } else {
        geoJsonMultiLineString(segments.map { it.toLineCoordinates() }.toTypedArray())
    }
}

/**
 * Línea declarativa (GeoJSON `LineString`/`MultiLineString` + layer `line`), la versión
 * "estática": [points] controla todo desde afuera, como [lng]/[lat] en [Marker]. Wrapper fino
 * sobre [GeoJsonLayer]: cambios en [points] no recrean nada, solo actualizan el source vía
 * `setData`, igual que el ejemplo oficial "Animate a line" de MapLibre.
 */
@Composable
fun MapLibreScope.Line(
    points: List<GeodeticCoordinates>,
    color: Color.Rgb,
    width: Double = 3.0,
    opacity: Double = 0.8,
) {
    GeoJsonLayer(
        key = points,
        data = { it.toLineGeoJson() },
        buildLayer = { sourceId, layerId ->
            lineLayer(
                id = layerId,
                source = sourceId,
                lineColor = color.toString(),
                lineWidth = width,
                lineOpacity = opacity,
                lineCap = "round",
                lineJoin = "round",
            )
        },
    )
}

/**
 * Versión animada de [Line]: revela [fullPath] progresivamente a lo largo de [durationMs], con
 * `withFrameMillis` (el equivalente Compose del `requestAnimationFrame` que usa el ejemplo
 * oficial de MapLibre). No reimplementa nada imperativo — es pura composición sobre [Line]: la
 * "versión original" (sin animar) es simplemente llamar a [Line] con el [fullPath] completo.
 */
@Composable
fun MapLibreScope.AnimatedLine(
    fullPath: List<GeodeticCoordinates>,
    color: Color.Rgb,
    durationMs: Int = 3000,
    width: Double = 3.0,
    opacity: Double = 0.8,
) {
    var revealedCount by remember(fullPath) { mutableStateOf(fullPath.size.coerceAtMost(1)) }

    LaunchedEffect(fullPath, durationMs) {
        if (fullPath.size < 2) return@LaunchedEffect
        val start = withFrameMillis { it }
        while (revealedCount < fullPath.size) {
            val elapsed = withFrameMillis { it } - start
            val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)
            revealedCount = (progress * fullPath.size).toInt().coerceIn(1, fullPath.size)
        }
    }

    Line(points = fullPath.take(revealedCount), color = color, width = width, opacity = opacity)
}

/**
 * Polígono relleno declarativo (GeoJSON `Polygon` + layer `fill`) — por ejemplo para el círculo
 * de cobertura de [com.parodison.orbit.core.satellite.Satellite.footprintPolygon]. [points] es el anillo
 * exterior; se cierra solo si no viene ya cerrado. Wrapper fino sobre [GeoJsonLayer].
 */
@Composable
fun MapLibreScope.Polygon(
    points: List<GeodeticCoordinates>,
    fillColor: Color.Rgb,
    fillOpacity: Double = 0.2,
    outlineColor: Color.Rgb? = null,
) {
    GeoJsonLayer(
        key = points,
        data = { geoJsonPolygon(arrayOf(it.toRingCoordinates())) },
        buildLayer = { sourceId, layerId ->
            fillLayer(
                id = layerId,
                source = sourceId,
                fillColor = fillColor.toString(),
                fillOpacity = fillOpacity,
                fillOutlineColor = outlineColor?.toString(),
            )
        },
    )
}

/**
 * Varios polígonos rellenos independientes a la vez (GeoJSON `MultiPolygon` + layer `fill`) —
 * por ejemplo, los footprints de cobertura de varios satélites en una sola capa. Cada elemento
 * de [polygons] es un anillo exterior, igual que [points] en [Polygon]. Wrapper fino sobre
 * [GeoJsonLayer].
 */
@Composable
fun MapLibreScope.MultiPolygon(
    polygons: List<List<GeodeticCoordinates>>,
    fillColor: Color.Rgb,
    fillOpacity: Double = 0.2,
    outlineColor: Color.Rgb? = null,
) {
    GeoJsonLayer(
        key = polygons,
        data = { list -> geoJsonMultiPolygon(list.map { arrayOf(it.toRingCoordinates()) }.toTypedArray()) },
        buildLayer = { sourceId, layerId ->
            fillLayer(
                id = layerId,
                source = sourceId,
                fillColor = fillColor.toString(),
                fillOpacity = fillOpacity,
                fillOutlineColor = outlineColor?.toString(),
            )
        },
    )
}

/**
 * Punto renderizado nativamente por MapLibre (GeoJSON `Point` + layer `circle`) — alternativa
 * liviana a [Marker] para cuando no hace falta contenido Compose adentro (solo un punto de
 * color): un `Marker` crea un `<div>` + una composición Compose HTML aparte por cada instancia,
 * mientras que esto es una única capa renderizada por GPU, más apropiada si necesitás muchos
 * puntos a la vez. Wrapper fino sobre [GeoJsonLayer].
 */
@Composable
fun MapLibreScope.CirclePoint(
    lng: Double,
    lat: Double,
    color: Color.Rgb,
    radiusPx: Double = 6.0,
    strokeColor: Color.Rgb? = null,
    strokeWidthPx: Double = 0.0,
) {
    GeoJsonLayer(
        key = lng to lat,
        data = { (pointLng, pointLat) -> geoJsonPoint(arrayOf(pointLng, pointLat)) },
        buildLayer = { sourceId, layerId ->
            circleLayer(
                id = layerId,
                source = sourceId,
                circleRadius = radiusPx,
                circleColor = color.toString(),
                circleStrokeColor = strokeColor?.toString(),
                circleStrokeWidth = strokeWidthPx,
            )
        },
    )
}
