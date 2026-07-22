package com.parodison.orbital.system.core

import kotlin.js.json
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Node

// ---------------------------------------------------------------------------------------------
// Internal helper: builds a plain JS object from pairs, skipping nulls, without ever using js("")
// ---------------------------------------------------------------------------------------------

internal fun jsObject(vararg pairs: Pair<String, Any?>): dynamic {
    val filtered = pairs.filter { it.second != null }
    return json(*filtered.toTypedArray())
}

// ---------------------------------------------------------------------------------------------
// Root module binding
// ---------------------------------------------------------------------------------------------

@JsModule("maplibre-gl")
@JsNonModule
external object maplibregl {

    open class Map(options: MapOptions) {
        fun addControl(control: dynamic, position: String = definedExternally): Map
        fun removeControl(control: dynamic): Map
        fun hasControl(control: dynamic): Boolean

        fun resize(eventData: dynamic = definedExternally): Map

        fun getBounds(): LngLatBounds
        fun setMaxBounds(bounds: dynamic = definedExternally): Map
        fun getMaxBounds(): LngLatBounds?

        fun setMinZoom(minZoom: Double): Map
        fun getMinZoom(): Double
        fun setMaxZoom(maxZoom: Double): Map
        fun getMaxZoom(): Double
        fun setMinPitch(minPitch: Double): Map
        fun getMinPitch(): Double
        fun setMaxPitch(maxPitch: Double): Map
        fun getMaxPitch(): Double

        fun getRenderWorldCopies(): Boolean
        fun setRenderWorldCopies(renderWorldCopies: Boolean = definedExternally): Map

        fun project(lngLat: dynamic): Point
        fun unproject(point: dynamic): LngLat

        fun isMoving(): Boolean
        fun isZooming(): Boolean
        fun isRotating(): Boolean

        fun on(type: String, listener: (dynamic) -> Unit): Map
        fun on(type: String, layerId: String, listener: (dynamic) -> Unit): Map
        fun off(type: String, listener: (dynamic) -> Unit): Map
        fun off(type: String, layerId: String, listener: (dynamic) -> Unit): Map
        fun once(type: String, listener: (dynamic) -> Unit): Map
        fun fire(type: String, properties: dynamic = definedExternally): Map
        fun listens(type: String): Boolean

        fun getCenter(): LngLat
        fun setCenter(center: dynamic, eventData: dynamic = definedExternally): Map
        fun panBy(offset: dynamic, options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map
        fun panTo(lngLat: dynamic, options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map

        fun getZoom(): Double
        fun setZoom(zoom: Double, eventData: dynamic = definedExternally): Map
        fun zoomTo(zoom: Double, options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map
        fun zoomIn(options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map
        fun zoomOut(options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map

        fun getBearing(): Double
        fun setBearing(bearing: Double, eventData: dynamic = definedExternally): Map
        fun rotateTo(bearing: Double, options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map
        fun resetNorth(options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map
        fun resetNorthPitch(options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map
        fun snapToNorth(options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map

        fun getPitch(): Double
        fun setPitch(pitch: Double, eventData: dynamic = definedExternally): Map

        fun getPadding(): dynamic
        fun setPadding(padding: dynamic, eventData: dynamic = definedExternally): Map

        fun cameraForBounds(bounds: dynamic, options: dynamic = definedExternally): dynamic
        fun fitBounds(bounds: dynamic, options: dynamic = definedExternally, eventData: dynamic = definedExternally): Map
        fun fitScreenCoordinates(
            p0: dynamic,
            p1: dynamic,
            bearing: Double,
            options: dynamic = definedExternally,
            eventData: dynamic = definedExternally,
        ): Map

        fun jumpTo(options: dynamic, eventData: dynamic = definedExternally): Map
        fun easeTo(options: dynamic, eventData: dynamic = definedExternally): Map
        fun flyTo(options: dynamic, eventData: dynamic = definedExternally): Map
        fun isEasing(): Boolean
        fun stop(): Map

        fun addSource(id: String, source: dynamic): Map
        fun isSourceLoaded(id: String): Boolean
        fun areTilesLoaded(): Boolean
        fun removeSource(id: String): Map
        fun getSource(id: String): dynamic

        fun addImage(id: String, image: dynamic, options: dynamic = definedExternally): Map
        fun updateImage(id: String, image: dynamic): Map
        fun removeImage(id: String): Map
        fun loadImage(url: String, callback: (dynamic, dynamic) -> Unit): Unit
        fun hasImage(id: String): Boolean
        fun listImages(): Array<String>

        fun addLayer(layer: dynamic, beforeId: String = definedExternally): Map
        fun moveLayer(id: String, beforeId: String = definedExternally): Map
        fun removeLayer(id: String): Map
        fun getLayer(id: String): dynamic
        fun getLayersOrder(): Array<String>
        fun setLayerZoomRange(layerId: String, minzoom: Double, maxzoom: Double): Map
        fun setFilter(layerId: String, filter: dynamic = definedExternally, options: dynamic = definedExternally): Map
        fun getFilter(layerId: String): dynamic

        fun setPaintProperty(layerId: String, name: String, value: dynamic, options: dynamic = definedExternally): Map
        fun getPaintProperty(layerId: String, name: String): dynamic
        fun setLayoutProperty(layerId: String, name: String, value: dynamic, options: dynamic = definedExternally): Map
        fun getLayoutProperty(layerId: String, name: String): dynamic

        fun setLight(light: dynamic, options: dynamic = definedExternally): Map
        fun getLight(): dynamic

        fun setTerrain(terrain: dynamic = definedExternally): Map
        fun getTerrain(): dynamic

        fun setSky(sky: dynamic = definedExternally): Map
        fun getSky(): dynamic

        fun setFeatureState(feature: dynamic, state: dynamic): Unit
        fun removeFeatureState(target: dynamic, key: String = definedExternally): Unit
        fun getFeatureState(feature: dynamic): dynamic

        fun setStyle(style: dynamic, options: dynamic = definedExternally): Map
        fun getStyle(): dynamic
        fun isStyleLoaded(): Boolean

        fun queryRenderedFeatures(geometryOrOptions: dynamic = definedExternally, options: dynamic = definedExternally): Array<dynamic>
        fun querySourceFeatures(sourceId: String, parameters: dynamic = definedExternally): Array<dynamic>

        fun getContainer(): HTMLElement
        fun getCanvasContainer(): HTMLElement
        fun getCanvas(): HTMLCanvasElement

        fun loaded(): Boolean
        fun remove(): Unit
        fun triggerRepaint(): Unit

        var showTileBoundaries: Boolean
        var showCollisionBoxes: Boolean
        var showPadding: Boolean
        var repaint: Boolean
    }

    open class Marker(options: MarkerOptions = definedExternally) {
        fun addTo(map: Map): Marker
        fun remove(): Marker
        fun getLngLat(): LngLat
        fun setLngLat(lngLat: dynamic): Marker
        fun getElement(): HTMLElement
        fun setPopup(popup: Popup = definedExternally): Marker
        fun getPopup(): Popup?
        fun togglePopup(): Marker
        fun getOffset(): Point
        fun setOffset(offset: dynamic): Marker
        fun setDraggable(shouldBeDraggable: Boolean): Marker
        fun isDraggable(): Boolean
        fun getRotation(): Double
        fun setRotation(rotation: Double = definedExternally): Marker
        fun getRotationAlignment(): String
        fun setRotationAlignment(alignment: String = definedExternally): Marker
        fun getPitchAlignment(): String
        fun setPitchAlignment(alignment: String = definedExternally): Marker
        fun on(type: String, listener: (dynamic) -> Unit): Marker
        fun off(type: String, listener: (dynamic) -> Unit): Marker
    }

    open class Popup(options: PopupOptions = definedExternally) {
        fun addTo(map: Map): Popup
        fun isOpen(): Boolean
        fun remove(): Popup
        fun getLngLat(): LngLat
        fun setLngLat(lngLat: dynamic): Popup
        fun trackPointer(): Popup
        fun getElement(): HTMLElement
        fun setText(text: String): Popup
        fun setHTML(html: String): Popup
        fun setDOMContent(htmlNode: Node): Popup
        fun getMaxWidth(): String
        fun setMaxWidth(maxWidth: String): Popup
        fun on(type: String, listener: (dynamic) -> Unit): Popup
        fun off(type: String, listener: (dynamic) -> Unit): Popup
    }

    open class NavigationControl(options: NavigationControlOptions = definedExternally)

    open class GeolocateControl(options: GeolocateControlOptions = definedExternally) {
        fun trigger(): Boolean
        fun on(type: String, listener: (dynamic) -> Unit): GeolocateControl
    }

    open class ScaleControl(options: ScaleControlOptions = definedExternally) {
        fun setUnit(unit: String): Unit
    }

    open class FullscreenControl(options: FullscreenControlOptions = definedExternally)

    open class AttributionControl(options: AttributionControlOptions = definedExternally)

    open class LogoControl

    open class LngLat(lng: Double, lat: Double) {
        var lng: Double
        var lat: Double
        fun wrap(): LngLat
        fun toArray(): Array<Double>
        override fun toString(): String
        fun distanceTo(other: LngLat): Double
        fun toBounds(radius: Double = definedExternally): LngLatBounds

        companion object {
            fun convert(input: dynamic): LngLat
        }
    }

    open class LngLatBounds(sw: dynamic = definedExternally, ne: dynamic = definedExternally) {
        fun setNorthEast(ne: dynamic): LngLatBounds
        fun setSouthWest(sw: dynamic): LngLatBounds
        fun extend(obj: dynamic): LngLatBounds
        fun getCenter(): LngLat
        fun getSouthWest(): LngLat
        fun getNorthEast(): LngLat
        fun getNorthWest(): LngLat
        fun getSouthEast(): LngLat
        fun getWest(): Double
        fun getNorth(): Double
        fun getSouth(): Double
        fun getEast(): Double
        fun contains(lngLat: dynamic): Boolean
        fun toArray(): Array<Array<Double>>
        override fun toString(): String
        fun isEmpty(): Boolean

        companion object {
            fun convert(input: dynamic): LngLatBounds
        }
    }

    open class Point(x: Double, y: Double) {
        var x: Double
        var y: Double
        fun clone(): Point
        fun add(other: Point): Point
        fun sub(other: Point): Point
        fun mult(scale: Double): Point
        fun div(scale: Double): Point
        fun rotate(angle: Double): Point
        fun round(): Point
        fun mag(): Double
        fun equals(other: Point): Boolean
        fun dist(other: Point): Double
        fun distSqr(other: Point): Double
        fun angle(): Double
        fun angleTo(other: Point): Double

        companion object {
            fun convert(input: dynamic): Point
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Map options
// ---------------------------------------------------------------------------------------------

external interface MapOptions {
    var container: dynamic // String (element id) or HTMLElement
    var style: dynamic // String (URL) or a style spec object
    var center: dynamic // [lng, lat] or LngLat or {lng, lat}
    var zoom: Double
    var bearing: Double
    var pitch: Double
    var minZoom: Double
    var maxZoom: Double
    var minPitch: Double
    var maxPitch: Double
    var interactive: Boolean
    var bearingSnap: Double
    var pitchWithRotate: Boolean
    var clickTolerance: Double
    var attributionControl: dynamic // Boolean or AttributionControlOptions
    var customAttribution: dynamic // String or Array<String>
    var logoPosition: String
    var failIfMajorPerformanceCaveat: Boolean
    var preserveDrawingBuffer: Boolean
    var antialias: Boolean
    var refreshExpiredTiles: Boolean
    var maxBounds: dynamic
    var scrollZoom: Boolean
    var boxZoom: Boolean
    var dragRotate: Boolean
    var dragPan: Boolean
    var keyboard: Boolean
    var doubleClickZoom: Boolean
    var touchZoomRotate: Boolean
    var touchPitch: Boolean
    var trackResize: Boolean
    var renderWorldCopies: Boolean
    var maxTileCacheSize: Int
    var localIdeographFontFamily: String
    var collectResourceTiming: Boolean
    var fadeDuration: Double
    var crossSourceCollisions: Boolean
    var locale: dynamic
    var cooperativeGestures: Boolean
    var hash: dynamic // Boolean or String
    var transformRequest: dynamic // (url: String, resourceType: String) -> dynamic
    var canvasContextAttributes: dynamic
}

fun mapOptions(
    container: dynamic = "map",
    style: dynamic = "/orbital_monitor_map.json",
    center: dynamic = null,
    zoom: Double? = null,
    bearing: Double? = null,
    pitch: Double? = null,
    minZoom: Double? = null,
    maxZoom: Double? = null,
    minPitch: Double? = null,
    maxPitch: Double? = null,
    interactive: Boolean? = null,
    bearingSnap: Double? = null,
    pitchWithRotate: Boolean? = null,
    clickTolerance: Double? = null,
    attributionControl: dynamic = null,
    customAttribution: dynamic = null,
    failIfMajorPerformanceCaveat: Boolean? = null,
    preserveDrawingBuffer: Boolean? = null,
    antialias: Boolean? = null,
    refreshExpiredTiles: Boolean? = null,
    maxBounds: dynamic = null,
    scrollZoom: Boolean? = null,
    boxZoom: Boolean? = null,
    dragRotate: Boolean? = null,
    dragPan: Boolean? = null,
    keyboard: Boolean? = null,
    doubleClickZoom: Boolean? = null,
    touchZoomRotate: Boolean? = null,
    touchPitch: Boolean? = null,
    trackResize: Boolean? = null,
    renderWorldCopies: Boolean? = null,
    maxTileCacheSize: Int? = null,
    localIdeographFontFamily: String? = null,
    collectResourceTiming: Boolean? = null,
    fadeDuration: Double? = null,
    crossSourceCollisions: Boolean? = null,
    locale: dynamic = null,
    cooperativeGestures: Boolean? = null,
    hash: dynamic = null,
    transformRequest: dynamic = null,
): MapOptions = jsObject(
    "container" to container,
    "style" to style,
    "center" to center,
    "zoom" to zoom,
    "bearing" to bearing,
    "pitch" to pitch,
    "minZoom" to minZoom,
    "maxZoom" to maxZoom,
    "minPitch" to minPitch,
    "maxPitch" to maxPitch,
    "interactive" to interactive,
    "bearingSnap" to bearingSnap,
    "pitchWithRotate" to pitchWithRotate,
    "clickTolerance" to clickTolerance,
    "attributionControl" to attributionControl,
    "customAttribution" to customAttribution,
    "failIfMajorPerformanceCaveat" to failIfMajorPerformanceCaveat,
    "preserveDrawingBuffer" to preserveDrawingBuffer,
    "antialias" to antialias,
    "refreshExpiredTiles" to refreshExpiredTiles,
    "maxBounds" to maxBounds,
    "scrollZoom" to scrollZoom,
    "boxZoom" to boxZoom,
    "dragRotate" to dragRotate,
    "dragPan" to dragPan,
    "keyboard" to keyboard,
    "doubleClickZoom" to doubleClickZoom,
    "touchZoomRotate" to touchZoomRotate,
    "touchPitch" to touchPitch,
    "trackResize" to trackResize,
    "renderWorldCopies" to renderWorldCopies,
    "maxTileCacheSize" to maxTileCacheSize,
    "localIdeographFontFamily" to localIdeographFontFamily,
    "collectResourceTiming" to collectResourceTiming,
    "fadeDuration" to fadeDuration,
    "crossSourceCollisions" to crossSourceCollisions,
    "locale" to locale,
    "cooperativeGestures" to cooperativeGestures,
    "hash" to hash,
    "transformRequest" to transformRequest,
).unsafeCast<MapOptions>()

fun lngLat(lng: Double, lat: Double): dynamic = jsObject("lng" to lng, "lat" to lat)

fun lngLatArray(lng: Double, lat: Double): Array<Double> = arrayOf(lng, lat)

// ---------------------------------------------------------------------------------------------
// Marker / Popup options
// ---------------------------------------------------------------------------------------------

external interface MarkerOptions {
    var element: HTMLElement
    var offset: dynamic
    var anchor: String // "center" | "top" | "bottom" | "left" | "right" | "top-left" | ...
    var color: String
    var scale: Double
    var draggable: Boolean
    var clickTolerance: Double
    var rotation: Double
    var rotationAlignment: String // "map" | "viewport" | "auto"
    var pitchAlignment: String // "map" | "viewport" | "auto"
    var occludedOpacity: Double
}

fun markerOptions(
    element: HTMLElement? = null,
    offset: dynamic = null,
    anchor: String? = null,
    color: String? = null,
    scale: Double? = null,
    draggable: Boolean? = null,
    clickTolerance: Double? = null,
    rotation: Double? = null,
    rotationAlignment: String? = null,
    pitchAlignment: String? = null,
    occludedOpacity: Double? = null,
): MarkerOptions = jsObject(
    "element" to element,
    "offset" to offset,
    "anchor" to anchor,
    "color" to color,
    "scale" to scale,
    "draggable" to draggable,
    "clickTolerance" to clickTolerance,
    "rotation" to rotation,
    "rotationAlignment" to rotationAlignment,
    "pitchAlignment" to pitchAlignment,
    "occludedOpacity" to occludedOpacity,
).unsafeCast<MarkerOptions>()

external interface PopupOptions {
    var closeButton: Boolean
    var closeOnClick: Boolean
    var closeOnMove: Boolean
    var focusAfterOpen: Boolean
    var anchor: String
    var offset: dynamic
    var className: String
    var maxWidth: String
    var subpixelPositioning: Boolean
}

fun popupOptions(
    closeButton: Boolean? = null,
    closeOnClick: Boolean? = null,
    closeOnMove: Boolean? = null,
    focusAfterOpen: Boolean? = null,
    anchor: String? = null,
    offset: dynamic = null,
    className: String? = null,
    maxWidth: String? = null,
    subpixelPositioning: Boolean? = null,
): PopupOptions = jsObject(
    "closeButton" to closeButton,
    "closeOnClick" to closeOnClick,
    "closeOnMove" to closeOnMove,
    "focusAfterOpen" to focusAfterOpen,
    "anchor" to anchor,
    "offset" to offset,
    "className" to className,
    "maxWidth" to maxWidth,
    "subpixelPositioning" to subpixelPositioning,
).unsafeCast<PopupOptions>()

// ---------------------------------------------------------------------------------------------
// Control options
// ---------------------------------------------------------------------------------------------

external interface NavigationControlOptions {
    var showCompass: Boolean
    var showZoom: Boolean
    var visualizePitch: Boolean
}

fun navigationControlOptions(
    showCompass: Boolean? = null,
    showZoom: Boolean? = null,
    visualizePitch: Boolean? = null,
): NavigationControlOptions = jsObject(
    "showCompass" to showCompass,
    "showZoom" to showZoom,
    "visualizePitch" to visualizePitch,
).unsafeCast<NavigationControlOptions>()

external interface GeolocateControlOptions {
    var positionOptions: dynamic
    var fitBoundsOptions: dynamic
    var trackUserLocation: Boolean
    var showAccuracyCircle: Boolean
    var showUserLocation: Boolean
    var showUserHeading: Boolean
}

fun geolocateControlOptions(
    positionOptions: dynamic = null,
    fitBoundsOptions: dynamic = null,
    trackUserLocation: Boolean? = null,
    showAccuracyCircle: Boolean? = null,
    showUserLocation: Boolean? = null,
    showUserHeading: Boolean? = null,
): GeolocateControlOptions = jsObject(
    "positionOptions" to positionOptions,
    "fitBoundsOptions" to fitBoundsOptions,
    "trackUserLocation" to trackUserLocation,
    "showAccuracyCircle" to showAccuracyCircle,
    "showUserLocation" to showUserLocation,
    "showUserHeading" to showUserHeading,
).unsafeCast<GeolocateControlOptions>()

external interface ScaleControlOptions {
    var maxWidth: Double
    var unit: String // "imperial" | "metric" | "nautical"
}

fun scaleControlOptions(
    maxWidth: Double? = null,
    unit: String? = null,
): ScaleControlOptions = jsObject(
    "maxWidth" to maxWidth,
    "unit" to unit,
).unsafeCast<ScaleControlOptions>()

external interface FullscreenControlOptions {
    var container: HTMLElement
}

fun fullscreenControlOptions(container: HTMLElement? = null): FullscreenControlOptions =
    jsObject("container" to container).unsafeCast<FullscreenControlOptions>()

external interface AttributionControlOptions {
    var compact: Boolean
    var customAttribution: dynamic // String or Array<String>
}

fun attributionControlOptions(
    compact: Boolean? = null,
    customAttribution: dynamic = null,
): AttributionControlOptions = jsObject(
    "compact" to compact,
    "customAttribution" to customAttribution,
).unsafeCast<AttributionControlOptions>()

// ---------------------------------------------------------------------------------------------
// Events
// ---------------------------------------------------------------------------------------------

external interface MapMouseEvent {
    val type: String
    val target: maplibregl.Map
    val originalEvent: dynamic
    val point: maplibregl.Point
    val lngLat: maplibregl.LngLat
    val features: Array<dynamic>?
}

external interface MapLibreEvent {
    val type: String
    val target: maplibregl.Map
}

external interface MapDataEvent {
    val type: String
    val target: maplibregl.Map
    val dataType: String // "source" | "style"
    val sourceId: String?
    val isSourceLoaded: Boolean?
}

external interface ErrorEvent {
    val type: String
    val target: maplibregl.Map
    val error: dynamic
}

object MapEvent {
    const val Load = "load"
    const val Idle = "idle"
    const val Render = "render"
    const val Resize = "resize"
    const val Remove = "remove"
    const val Error = "error"
    const val Data = "data"
    const val Style = "style"
    const val SourceData = "sourcedata"
    const val StyleData = "styledata"
    const val StyleImageMissing = "styleimagemissing"
    const val DataLoading = "dataloading"
    const val SourceDataLoading = "sourcedataloading"

    const val Click = "click"
    const val DblClick = "dblclick"
    const val MouseDown = "mousedown"
    const val MouseUp = "mouseup"
    const val MouseMove = "mousemove"
    const val MouseEnter = "mouseenter"
    const val MouseLeave = "mouseleave"
    const val MouseOver = "mouseover"
    const val MouseOut = "mouseout"
    const val ContextMenu = "contextmenu"

    const val TouchStart = "touchstart"
    const val TouchEnd = "touchend"
    const val TouchMove = "touchmove"
    const val TouchCancel = "touchcancel"
    const val Wheel = "wheel"

    const val MoveStart = "movestart"
    const val Move = "move"
    const val MoveEnd = "moveend"
    const val DragStart = "dragstart"
    const val Drag = "drag"
    const val DragEnd = "dragend"
    const val ZoomStart = "zoomstart"
    const val Zoom = "zoom"
    const val ZoomEnd = "zoomend"
    const val RotateStart = "rotatestart"
    const val Rotate = "rotate"
    const val RotateEnd = "rotateend"
    const val PitchStart = "pitchstart"
    const val Pitch = "pitch"
    const val PitchEnd = "pitchend"
    const val BoxZoomStart = "boxzoomstart"
    const val BoxZoomEnd = "boxzoomend"
    const val BoxZoomCancel = "boxzoomcancel"
    const val WebGlContextLost = "webglcontextlost"
    const val WebGlContextRestored = "webglcontextrestored"
}

fun maplibregl.Map.onLoad(listener: (MapLibreEvent) -> Unit): maplibregl.Map =
    on(MapEvent.Load) { e -> listener(e.unsafeCast<MapLibreEvent>()) }

fun maplibregl.Map.onClick(listener: (MapMouseEvent) -> Unit): maplibregl.Map =
    on(MapEvent.Click) { e -> listener(e.unsafeCast<MapMouseEvent>()) }

fun maplibregl.Map.onClick(layerId: String, listener: (MapMouseEvent) -> Unit): maplibregl.Map =
    on(MapEvent.Click, layerId) { e -> listener(e.unsafeCast<MapMouseEvent>()) }

fun maplibregl.Map.onMouseEnter(layerId: String, listener: (MapMouseEvent) -> Unit): maplibregl.Map =
    on(MapEvent.MouseEnter, layerId) { e -> listener(e.unsafeCast<MapMouseEvent>()) }

fun maplibregl.Map.onMouseLeave(layerId: String, listener: (MapMouseEvent) -> Unit): maplibregl.Map =
    on(MapEvent.MouseLeave, layerId) { e -> listener(e.unsafeCast<MapMouseEvent>()) }

fun maplibregl.Map.onMove(listener: (MapLibreEvent) -> Unit): maplibregl.Map =
    on(MapEvent.Move) { e -> listener(e.unsafeCast<MapLibreEvent>()) }

fun maplibregl.Map.onMoveEnd(listener: (MapLibreEvent) -> Unit): maplibregl.Map =
    on(MapEvent.MoveEnd) { e -> listener(e.unsafeCast<MapLibreEvent>()) }

fun maplibregl.Map.onZoomEnd(listener: (MapLibreEvent) -> Unit): maplibregl.Map =
    on(MapEvent.ZoomEnd) { e -> listener(e.unsafeCast<MapLibreEvent>()) }

fun maplibregl.Map.onError(listener: (ErrorEvent) -> Unit): maplibregl.Map =
    on(MapEvent.Error) { e -> listener(e.unsafeCast<ErrorEvent>()) }

// ---------------------------------------------------------------------------------------------
// Source / Layer type constants
// ---------------------------------------------------------------------------------------------

object SourceType {
    const val GeoJson = "geojson"
    const val Vector = "vector"
    const val Raster = "raster"
    const val RasterDem = "raster-dem"
    const val Image = "image"
    const val Video = "video"
    const val Canvas = "canvas"
}

object LayerType {
    const val Background = "background"
    const val Fill = "fill"
    const val Line = "line"
    const val Symbol = "symbol"
    const val Circle = "circle"
    const val Heatmap = "heatmap"
    const val FillExtrusion = "fill-extrusion"
    const val Raster = "raster"
    const val Hillshade = "hillshade"
    const val Sky = "sky"
}

// ---------------------------------------------------------------------------------------------
// GeoJSON helpers
// ---------------------------------------------------------------------------------------------

fun geoJsonPoint(coordinates: Array<Double>, properties: dynamic = null): dynamic = jsObject(
    "type" to "Feature",
    "geometry" to jsObject("type" to "Point", "coordinates" to coordinates),
    "properties" to (properties ?: jsObject()),
)

fun geoJsonLineString(coordinates: Array<Array<Double>>, properties: dynamic = null): dynamic = jsObject(
    "type" to "Feature",
    "geometry" to jsObject("type" to "LineString", "coordinates" to coordinates),
    "properties" to (properties ?: jsObject()),
)

fun geoJsonPolygon(coordinates: Array<Array<Array<Double>>>, properties: dynamic = null): dynamic = jsObject(
    "type" to "Feature",
    "geometry" to jsObject("type" to "Polygon", "coordinates" to coordinates),
    "properties" to (properties ?: jsObject()),
)

fun geoJsonMultiLineString(coordinates: Array<Array<Array<Double>>>, properties: dynamic = null): dynamic = jsObject(
    "type" to "Feature",
    "geometry" to jsObject("type" to "MultiLineString", "coordinates" to coordinates),
    "properties" to (properties ?: jsObject()),
)

fun geoJsonMultiPolygon(coordinates: Array<Array<Array<Array<Double>>>>, properties: dynamic = null): dynamic = jsObject(
    "type" to "Feature",
    "geometry" to jsObject("type" to "MultiPolygon", "coordinates" to coordinates),
    "properties" to (properties ?: jsObject()),
)

fun geoJsonFeatureCollection(features: Array<dynamic>): dynamic = jsObject(
    "type" to "FeatureCollection",
    "features" to features,
)

// ---------------------------------------------------------------------------------------------
// Source specs
// ---------------------------------------------------------------------------------------------

external interface GeoJSONSourceSpecification {
    var type: String // "geojson"
    var data: dynamic // URL string or a GeoJSON object
    var maxzoom: Double
    var attribution: String
    var buffer: Double
    var tolerance: Double
    var cluster: Boolean
    var clusterRadius: Double
    var clusterMaxZoom: Double
    var clusterMinPoints: Double
    var clusterProperties: dynamic
    var lineMetrics: Boolean
    var generateId: Boolean
    var promoteId: dynamic
    var filter: dynamic
}

fun geoJsonSource(
    data: dynamic,
    maxzoom: Double? = null,
    attribution: String? = null,
    buffer: Double? = null,
    tolerance: Double? = null,
    cluster: Boolean? = null,
    clusterRadius: Double? = null,
    clusterMaxZoom: Double? = null,
    clusterMinPoints: Double? = null,
    clusterProperties: dynamic = null,
    lineMetrics: Boolean? = null,
    generateId: Boolean? = null,
    promoteId: dynamic = null,
): GeoJSONSourceSpecification = jsObject(
    "type" to SourceType.GeoJson,
    "data" to data,
    "maxzoom" to maxzoom,
    "attribution" to attribution,
    "buffer" to buffer,
    "tolerance" to tolerance,
    "cluster" to cluster,
    "clusterRadius" to clusterRadius,
    "clusterMaxZoom" to clusterMaxZoom,
    "clusterMinPoints" to clusterMinPoints,
    "clusterProperties" to clusterProperties,
    "lineMetrics" to lineMetrics,
    "generateId" to generateId,
    "promoteId" to promoteId,
).unsafeCast<GeoJSONSourceSpecification>()

external interface VectorSourceSpecification {
    var type: String // "vector"
    var url: String
    var tiles: Array<String>
    var bounds: Array<Double>
    var scheme: String // "xyz" | "tms"
    var minzoom: Double
    var maxzoom: Double
    var attribution: String
    var promoteId: dynamic
    var volatile: Boolean
}

fun vectorSource(
    url: String? = null,
    tiles: Array<String>? = null,
    bounds: Array<Double>? = null,
    scheme: String? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    attribution: String? = null,
    promoteId: dynamic = null,
    volatile: Boolean? = null,
): VectorSourceSpecification = jsObject(
    "type" to SourceType.Vector,
    "url" to url,
    "tiles" to tiles,
    "bounds" to bounds,
    "scheme" to scheme,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "attribution" to attribution,
    "promoteId" to promoteId,
    "volatile" to volatile,
).unsafeCast<VectorSourceSpecification>()

external interface RasterSourceSpecification {
    var type: String // "raster"
    var url: String
    var tiles: Array<String>
    var bounds: Array<Double>
    var minzoom: Double
    var maxzoom: Double
    var tileSize: Double
    var scheme: String
    var attribution: String
    var volatile: Boolean
}

fun rasterSource(
    url: String? = null,
    tiles: Array<String>? = null,
    bounds: Array<Double>? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    tileSize: Double? = null,
    scheme: String? = null,
    attribution: String? = null,
    volatile: Boolean? = null,
): RasterSourceSpecification = jsObject(
    "type" to SourceType.Raster,
    "url" to url,
    "tiles" to tiles,
    "bounds" to bounds,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "tileSize" to tileSize,
    "scheme" to scheme,
    "attribution" to attribution,
    "volatile" to volatile,
).unsafeCast<RasterSourceSpecification>()

external interface RasterDEMSourceSpecification {
    var type: String // "raster-dem"
    var url: String
    var tiles: Array<String>
    var bounds: Array<Double>
    var minzoom: Double
    var maxzoom: Double
    var tileSize: Double
    var attribution: String
    var encoding: String // "terrarium" | "mapbox"
}

fun rasterDemSource(
    url: String? = null,
    tiles: Array<String>? = null,
    bounds: Array<Double>? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    tileSize: Double? = null,
    attribution: String? = null,
    encoding: String? = null,
): RasterDEMSourceSpecification = jsObject(
    "type" to SourceType.RasterDem,
    "url" to url,
    "tiles" to tiles,
    "bounds" to bounds,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "tileSize" to tileSize,
    "attribution" to attribution,
    "encoding" to encoding,
).unsafeCast<RasterDEMSourceSpecification>()

external interface ImageSourceSpecification {
    var type: String // "image"
    var url: String
    var coordinates: Array<Array<Double>> // 4 corners: TL, TR, BR, BL
}

fun imageSource(url: String, coordinates: Array<Array<Double>>): ImageSourceSpecification = jsObject(
    "type" to SourceType.Image,
    "url" to url,
    "coordinates" to coordinates,
).unsafeCast<ImageSourceSpecification>()

// ---------------------------------------------------------------------------------------------
// Layer specs
// ---------------------------------------------------------------------------------------------

external interface LayerSpecification {
    var id: String
    var type: String
    var source: dynamic
    var minzoom: Double
    var maxzoom: Double
    var filter: dynamic
    var layout: dynamic
    var paint: dynamic
    var metadata: dynamic
}

fun backgroundLayer(
    id: String,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    backgroundColor: dynamic = null,
    backgroundOpacity: dynamic = null,
    backgroundPattern: dynamic = null,
    visibility: String? = null,
): LayerSpecification = jsObject(
    "id" to id,
    "type" to LayerType.Background,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "paint" to jsObject(
        "background-color" to backgroundColor,
        "background-opacity" to backgroundOpacity,
        "background-pattern" to backgroundPattern,
    ),
    "layout" to jsObject("visibility" to visibility),
).unsafeCast<LayerSpecification>()

fun fillLayer(
    id: String,
    source: String,
    sourceLayer: String? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    filter: dynamic = null,
    fillColor: dynamic = null,
    fillOpacity: dynamic = null,
    fillOutlineColor: dynamic = null,
    fillAntialias: Boolean? = null,
    fillTranslate: dynamic = null,
    fillTranslateAnchor: String? = null,
    fillPattern: dynamic = null,
    visibility: String? = null,
): LayerSpecification = jsObject(
    "id" to id,
    "type" to LayerType.Fill,
    "source" to source,
    "source-layer" to sourceLayer,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "filter" to filter,
    "paint" to jsObject(
        "fill-color" to fillColor,
        "fill-opacity" to fillOpacity,
        "fill-outline-color" to fillOutlineColor,
        "fill-antialias" to fillAntialias,
        "fill-translate" to fillTranslate,
        "fill-translate-anchor" to fillTranslateAnchor,
        "fill-pattern" to fillPattern,
    ),
    "layout" to jsObject("visibility" to visibility),
).unsafeCast<LayerSpecification>()

fun lineLayer(
    id: String,
    source: String,
    sourceLayer: String? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    filter: dynamic = null,
    lineColor: dynamic = null,
    lineOpacity: dynamic = null,
    lineWidth: dynamic = null,
    lineGapWidth: dynamic = null,
    lineOffset: dynamic = null,
    lineBlur: dynamic = null,
    lineDasharray: dynamic = null,
    linePattern: dynamic = null,
    lineTranslate: dynamic = null,
    lineTranslateAnchor: String? = null,
    lineCap: String? = null,
    lineJoin: String? = null,
    lineMiterLimit: Double? = null,
    lineRoundLimit: Double? = null,
    lineSortKey: dynamic = null,
    visibility: String? = null,
): LayerSpecification = jsObject(
    "id" to id,
    "type" to LayerType.Line,
    "source" to source,
    "source-layer" to sourceLayer,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "filter" to filter,
    "paint" to jsObject(
        "line-color" to lineColor,
        "line-opacity" to lineOpacity,
        "line-width" to lineWidth,
        "line-gap-width" to lineGapWidth,
        "line-offset" to lineOffset,
        "line-blur" to lineBlur,
        "line-dasharray" to lineDasharray,
        "line-pattern" to linePattern,
        "line-translate" to lineTranslate,
        "line-translate-anchor" to lineTranslateAnchor,
    ),
    "layout" to jsObject(
        "line-cap" to lineCap,
        "line-join" to lineJoin,
        "line-miter-limit" to lineMiterLimit,
        "line-round-limit" to lineRoundLimit,
        "line-sort-key" to lineSortKey,
        "visibility" to visibility,
    ),
).unsafeCast<LayerSpecification>()

fun circleLayer(
    id: String,
    source: String,
    sourceLayer: String? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    filter: dynamic = null,
    circleRadius: dynamic = null,
    circleColor: dynamic = null,
    circleBlur: dynamic = null,
    circleOpacity: dynamic = null,
    circleTranslate: dynamic = null,
    circleTranslateAnchor: String? = null,
    circlePitchScale: String? = null,
    circlePitchAlignment: String? = null,
    circleStrokeWidth: dynamic = null,
    circleStrokeColor: dynamic = null,
    circleStrokeOpacity: dynamic = null,
    circleSortKey: dynamic = null,
    visibility: String? = null,
): LayerSpecification = jsObject(
    "id" to id,
    "type" to LayerType.Circle,
    "source" to source,
    "source-layer" to sourceLayer,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "filter" to filter,
    "paint" to jsObject(
        "circle-radius" to circleRadius,
        "circle-color" to circleColor,
        "circle-blur" to circleBlur,
        "circle-opacity" to circleOpacity,
        "circle-translate" to circleTranslate,
        "circle-translate-anchor" to circleTranslateAnchor,
        "circle-pitch-scale" to circlePitchScale,
        "circle-pitch-alignment" to circlePitchAlignment,
        "circle-stroke-width" to circleStrokeWidth,
        "circle-stroke-color" to circleStrokeColor,
        "circle-stroke-opacity" to circleStrokeOpacity,
    ),
    "layout" to jsObject(
        "circle-sort-key" to circleSortKey,
        "visibility" to visibility,
    ),
).unsafeCast<LayerSpecification>()

fun symbolLayer(
    id: String,
    source: String,
    sourceLayer: String? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    filter: dynamic = null,
    iconImage: dynamic = null,
    iconSize: dynamic = null,
    iconColor: dynamic = null,
    iconOpacity: dynamic = null,
    iconHaloColor: dynamic = null,
    iconHaloWidth: dynamic = null,
    iconAllowOverlap: dynamic = null,
    iconIgnorePlacement: dynamic = null,
    iconAnchor: dynamic = null,
    iconOffset: dynamic = null,
    iconRotate: dynamic = null,
    textField: dynamic = null,
    textFont: dynamic = null,
    textSize: dynamic = null,
    textColor: dynamic = null,
    textOpacity: dynamic = null,
    textHaloColor: dynamic = null,
    textHaloWidth: dynamic = null,
    textAnchor: dynamic = null,
    textOffset: dynamic = null,
    textJustify: dynamic = null,
    textAllowOverlap: dynamic = null,
    textIgnorePlacement: dynamic = null,
    symbolPlacement: String? = null,
    symbolSortKey: dynamic = null,
    visibility: String? = null,
): LayerSpecification = jsObject(
    "id" to id,
    "type" to LayerType.Symbol,
    "source" to source,
    "source-layer" to sourceLayer,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "filter" to filter,
    "paint" to jsObject(
        "icon-color" to iconColor,
        "icon-opacity" to iconOpacity,
        "icon-halo-color" to iconHaloColor,
        "icon-halo-width" to iconHaloWidth,
        "text-color" to textColor,
        "text-opacity" to textOpacity,
        "text-halo-color" to textHaloColor,
        "text-halo-width" to textHaloWidth,
    ),
    "layout" to jsObject(
        "icon-image" to iconImage,
        "icon-size" to iconSize,
        "icon-allow-overlap" to iconAllowOverlap,
        "icon-ignore-placement" to iconIgnorePlacement,
        "icon-anchor" to iconAnchor,
        "icon-offset" to iconOffset,
        "icon-rotate" to iconRotate,
        "text-field" to textField,
        "text-font" to textFont,
        "text-size" to textSize,
        "text-anchor" to textAnchor,
        "text-offset" to textOffset,
        "text-justify" to textJustify,
        "text-allow-overlap" to textAllowOverlap,
        "text-ignore-placement" to textIgnorePlacement,
        "symbol-placement" to symbolPlacement,
        "symbol-sort-key" to symbolSortKey,
        "visibility" to visibility,
    ),
).unsafeCast<LayerSpecification>()

fun rasterLayer(
    id: String,
    source: String,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    rasterOpacity: dynamic = null,
    rasterHueRotate: dynamic = null,
    rasterBrightnessMin: dynamic = null,
    rasterBrightnessMax: dynamic = null,
    rasterSaturation: dynamic = null,
    rasterContrast: dynamic = null,
    rasterResampling: String? = null,
    visibility: String? = null,
): LayerSpecification = jsObject(
    "id" to id,
    "type" to LayerType.Raster,
    "source" to source,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "paint" to jsObject(
        "raster-opacity" to rasterOpacity,
        "raster-hue-rotate" to rasterHueRotate,
        "raster-brightness-min" to rasterBrightnessMin,
        "raster-brightness-max" to rasterBrightnessMax,
        "raster-saturation" to rasterSaturation,
        "raster-contrast" to rasterContrast,
        "raster-resampling" to rasterResampling,
    ),
    "layout" to jsObject("visibility" to visibility),
).unsafeCast<LayerSpecification>()

fun fillExtrusionLayer(
    id: String,
    source: String,
    sourceLayer: String? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    filter: dynamic = null,
    fillExtrusionColor: dynamic = null,
    fillExtrusionHeight: dynamic = null,
    fillExtrusionBase: dynamic = null,
    fillExtrusionOpacity: dynamic = null,
    fillExtrusionTranslate: dynamic = null,
    fillExtrusionTranslateAnchor: String? = null,
    fillExtrusionPattern: dynamic = null,
    fillExtrusionVerticalGradient: Boolean? = null,
    visibility: String? = null,
): LayerSpecification = jsObject(
    "id" to id,
    "type" to LayerType.FillExtrusion,
    "source" to source,
    "source-layer" to sourceLayer,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "filter" to filter,
    "paint" to jsObject(
        "fill-extrusion-color" to fillExtrusionColor,
        "fill-extrusion-height" to fillExtrusionHeight,
        "fill-extrusion-base" to fillExtrusionBase,
        "fill-extrusion-opacity" to fillExtrusionOpacity,
        "fill-extrusion-translate" to fillExtrusionTranslate,
        "fill-extrusion-translate-anchor" to fillExtrusionTranslateAnchor,
        "fill-extrusion-pattern" to fillExtrusionPattern,
        "fill-extrusion-vertical-gradient" to fillExtrusionVerticalGradient,
    ),
    "layout" to jsObject("visibility" to visibility),
).unsafeCast<LayerSpecification>()

fun heatmapLayer(
    id: String,
    source: String,
    sourceLayer: String? = null,
    minzoom: Double? = null,
    maxzoom: Double? = null,
    filter: dynamic = null,
    heatmapRadius: dynamic = null,
    heatmapWeight: dynamic = null,
    heatmapIntensity: dynamic = null,
    heatmapColor: dynamic = null,
    heatmapOpacity: dynamic = null,
    visibility: String? = null,
): LayerSpecification = jsObject(
    "id" to id,
    "type" to LayerType.Heatmap,
    "source" to source,
    "source-layer" to sourceLayer,
    "minzoom" to minzoom,
    "maxzoom" to maxzoom,
    "filter" to filter,
    "paint" to jsObject(
        "heatmap-radius" to heatmapRadius,
        "heatmap-weight" to heatmapWeight,
        "heatmap-intensity" to heatmapIntensity,
        "heatmap-color" to heatmapColor,
        "heatmap-opacity" to heatmapOpacity,
    ),
    "layout" to jsObject("visibility" to visibility),
).unsafeCast<LayerSpecification>()

// ---------------------------------------------------------------------------------------------
// Expression helpers (style-spec expressions are plain JSON arrays)
// ---------------------------------------------------------------------------------------------

object Expr {
    fun get(property: String): Array<Any?> = arrayOf("get", property)

    fun literal(value: dynamic): Array<Any?> = arrayOf("literal", value)

    fun interpolateLinear(input: dynamic, vararg stops: Pair<Double, Any?>): Array<Any?> {
        val flat = mutableListOf<Any?>("interpolate", arrayOf("linear"), input)
        stops.forEach { (stop, value) -> flat.add(stop); flat.add(value) }
        return flat.toTypedArray()
    }

    fun step(input: dynamic, default: Any?, vararg stops: Pair<Double, Any?>): Array<Any?> {
        val flat = mutableListOf<Any?>("step", input, default)
        stops.forEach { (stop, value) -> flat.add(stop); flat.add(value) }
        return flat.toTypedArray()
    }

    fun match(input: dynamic, default: Any?, vararg cases: Pair<Any?, Any?>): Array<Any?> {
        val flat = mutableListOf<Any?>("match", input)
        cases.forEach { (matchValue, output) -> flat.add(matchValue); flat.add(output) }
        flat.add(default)
        return flat.toTypedArray()
    }

    fun case(vararg branches: Pair<Array<Any?>, Any?>, default: Any?): Array<Any?> {
        val flat = mutableListOf<Any?>("case")
        branches.forEach { (condition, output) -> flat.add(condition); flat.add(output) }
        flat.add(default)
        return flat.toTypedArray()
    }

    fun eq(left: Any?, right: Any?): Array<Any?> = arrayOf("==", left, right)
    fun gt(left: Any?, right: Any?): Array<Any?> = arrayOf(">", left, right)
    fun gte(left: Any?, right: Any?): Array<Any?> = arrayOf(">=", left, right)
    fun lt(left: Any?, right: Any?): Array<Any?> = arrayOf("<", left, right)
    fun lte(left: Any?, right: Any?): Array<Any?> = arrayOf("<=", left, right)
    fun all(vararg conditions: Array<Any?>): Array<Any?> = arrayOf("all", *conditions)
    fun any(vararg conditions: Array<Any?>): Array<Any?> = arrayOf("any", *conditions)
}
