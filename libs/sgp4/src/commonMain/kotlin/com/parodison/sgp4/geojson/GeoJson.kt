package com.parodison.sgp4.geojson

import com.parodison.sgp4.GeodeticCoordinates
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Subconjunto de GeoJSON (RFC 7946) con lo que necesita [com.parodison.sgp4.Satellite] para
 * alimentar un mapa: punto (posición subsatelital), líneas (ground track) y polígono
 * (footprint de cobertura). Viven en `libs/sgp4` — son portátiles a todos los targets, a
 * diferencia de los tipos GeoJSON internos de maplibre-compose, que solo están en el
 * classpath del target JVM.
 *
 * El discriminador "type" de cada variante sale gratis del polimorfismo sellado de
 * kotlinx.serialization (su clave por defecto ya es "type"); alcanza con fijar el valor
 * con @SerialName en cada subtipo para que coincida con el spec.
 */
@Serializable
sealed class Geometry

@Serializable
@SerialName("Point")
data class Point(val coordinates: List<Double>) : Geometry()

@Serializable
@SerialName("LineString")
data class LineString(val coordinates: List<List<Double>>) : Geometry()

@Serializable
@SerialName("MultiLineString")
data class MultiLineString(val coordinates: List<List<List<Double>>>) : Geometry()

@Serializable
@SerialName("Polygon")
data class Polygon(val coordinates: List<List<List<Double>>>) : Geometry()

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class Feature(
    val geometry: Geometry,
    val properties: Map<String, JsonElement> = emptyMap(),
) {
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val type: String = "Feature"
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class FeatureCollection(
    val features: List<Feature>,
) {
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    val type: String = "FeatureCollection"
}

/** [longitud, latitud, altura en metros] — GeoJSON fija el orden lon/lat; la altura es en metros, no km. */
private fun GeodeticCoordinates.toPosition(): List<Double> =
    listOf(longitudeDeg, latitudeDeg, altitudeKm * 1000.0)

fun GeodeticCoordinates.toPointGeometry(): Point = Point(coordinates = toPosition())

fun List<GeodeticCoordinates>.toLineStringGeometry(): LineString =
    LineString(coordinates = map { it.toPosition() })

fun List<List<GeodeticCoordinates>>.toMultiLineStringGeometry(): MultiLineString =
    MultiLineString(coordinates = map { segment -> segment.map { it.toPosition() } })

/** Cierra el anillo (repite el primer punto al final) si el caller no lo hizo ya. */
fun List<GeodeticCoordinates>.toPolygonGeometry(): Polygon {
    val ring = if (isNotEmpty() && first() != last()) this + first() else this
    return Polygon(coordinates = listOf(ring.map { it.toPosition() }))
}

private val geoJsonFormat = Json { encodeDefaults = true }

/** Serializa a texto GeoJSON — útil para pasarlo tal cual a un `map.addSource(...)` en JS. */
fun Feature.toGeoJsonString(): String = geoJsonFormat.encodeToString(this)

fun FeatureCollection.toGeoJsonString(): String = geoJsonFormat.encodeToString(this)
