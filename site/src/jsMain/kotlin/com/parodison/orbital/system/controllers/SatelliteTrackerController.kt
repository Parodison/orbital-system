package com.parodison.orbital.system.controllers

import com.parodison.orbital.system.models.OrbitData
import com.parodison.sgp4.Satellite
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

sealed class SatelliteListState {
    object Idle : SatelliteListState()
    object Loading : SatelliteListState()
    data class Success(val data: List<OrbitData>) : SatelliteListState()
    data class Error(val message: String) : SatelliteListState()
}

class SatelliteTrackerController(
    val scope: CoroutineScope,
    val client: HttpClient
) {
    private val _satelliteListState = MutableStateFlow<SatelliteListState>(SatelliteListState.Idle)
    val satelliteListState = _satelliteListState.asStateFlow()

    private val _selectedSatellite = MutableStateFlow<Satellite?>(null)
    val selectedSatellite = _selectedSatellite.asStateFlow()

    init {
        """if (_satelliteListState.value is SatelliteListState.Idle) {
            loadSatelliteList()
        }"""
        _satelliteListState.value = SatelliteListState.Success(
            data = Json.decodeFromString<List<OrbitData>>(satelliteJsonList)
        )
    }

    fun updateSelectedSatellite(data: Satellite) {
        _selectedSatellite.value = data
    }

    fun clearSelectedSatellite() {
        _selectedSatellite.value = null
    }

    fun loadSatelliteList() {
        _satelliteListState.value = SatelliteListState.Loading
        scope.launch {
            try {
                val response = client.get("http://localhost:8000/api/satellites") {
                    parameter("GROUP", "stations")
                    parameter("FORMAT", "json")
                    accept(ContentType.Application.Cbor)
                }
                if (response.status.isSuccess()) {
                    val data = response.body<List<OrbitData>>()
                    _satelliteListState.value = SatelliteListState.Success(data)
                } else {
                    val error = response.bodyAsText()
                    println(error)
                    _satelliteListState.value = SatelliteListState.Error(
                        "Ha ocurrido un error con status code ${response.status.value}: $error"
                    )
                }

            } catch (e: ResponseException) {
                val errorResponse = e.response.bodyAsText()
                _satelliteListState.value = SatelliteListState.Error(errorResponse)
            } catch (e: Exception) {
                val error = e.message ?: "Ha ocurrido un error inesperado"
                e.printStackTrace()
                _satelliteListState.value = SatelliteListState.Error(error)
            }

        }
    }

    fun resetSatelliteList() {
        _satelliteListState.value = SatelliteListState.Idle
    }
    fun findOrbitDataByNoradId(noradId: Long): OrbitData? {
        val state = satelliteListState.value
        return if (state is SatelliteListState.Success) {
            state.data.find { it.noradCatId == noradId }
        } else {
            null
        }
    }
}

val satelliteJsonList = """
    [
      {
        "OBJECT_NAME": "ISS (ZARYA)",
        "OBJECT_ID": "1998-067A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 25544,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57646,
        "BSTAR": 0.000072724,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "POISK",
        "OBJECT_ID": "2009-060A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 36086,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57553,
        "BSTAR": 0.000072724,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "CSS (TIANHE)",
        "OBJECT_ID": "2021-035A",
        "EPOCH": "2026-07-17T12:32:16.126368",
        "MEAN_MOTION": 15.58143309,
        "ECCENTRICITY": 0.0001921,
        "INCLINATION": 41.4691,
        "RA_OF_ASC_NODE": 137.7269,
        "ARG_OF_PERICENTER": 301.5742,
        "MEAN_ANOMALY": 58.4908,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 48274,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 29790,
        "BSTAR": 0.000018813,
        "MEAN_MOTION_DOT": 0.00001129,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "ISS (NAUKA)",
        "OBJECT_ID": "2021-066A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 49044,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57557,
        "BSTAR": 0.000072724,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "FREGAT DEB",
        "OBJECT_ID": "2011-037PF",
        "EPOCH": "2026-07-17T05:15:19.083168",
        "MEAN_MOTION": 12.42812951,
        "ECCENTRICITY": 0.09004692,
        "INCLINATION": 51.6189,
        "RA_OF_ASC_NODE": 271.604,
        "ARG_OF_PERICENTER": 347.8715,
        "MEAN_ANOMALY": 10.1695,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 49271,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 22878,
        "BSTAR": 0.022629119,
        "MEAN_MOTION_DOT": 0.00013403,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "CSS (WENTIAN)",
        "OBJECT_ID": "2022-085A",
        "EPOCH": "2026-07-17T12:32:16.126368",
        "MEAN_MOTION": 15.58143309,
        "ECCENTRICITY": 0.00019213,
        "INCLINATION": 41.4691,
        "RA_OF_ASC_NODE": 137.7269,
        "ARG_OF_PERICENTER": 301.5742,
        "MEAN_ANOMALY": 58.4908,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 53239,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 28823,
        "BSTAR": 0.000018813159,
        "MEAN_MOTION_DOT": 0.00001129,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "CSS (MENGTIAN)",
        "OBJECT_ID": "2022-143A",
        "EPOCH": "2026-07-17T12:32:16.126368",
        "MEAN_MOTION": 15.58143309,
        "ECCENTRICITY": 0.0001921,
        "INCLINATION": 41.4691,
        "RA_OF_ASC_NODE": 137.7269,
        "ARG_OF_PERICENTER": 301.5742,
        "MEAN_ANOMALY": 58.4908,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 54216,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 29759,
        "BSTAR": 0.000018813,
        "MEAN_MOTION_DOT": 0.00001129,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "HRC MONOBLOCK CAMERA",
        "OBJECT_ID": "1998-067XR",
        "EPOCH": "2026-07-17T04:31:08.211936",
        "MEAN_MOTION": 15.78618998,
        "ECCENTRICITY": 0.00011887,
        "INCLINATION": 51.6204,
        "RA_OF_ASC_NODE": 124.5362,
        "ARG_OF_PERICENTER": 65.4974,
        "MEAN_ANOMALY": 294.615,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 66052,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 4273,
        "BSTAR": 0.00037352429,
        "MEAN_MOTION_DOT": 0.00066482,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "SZ-21 MODULE",
        "OBJECT_ID": "2025-246C",
        "EPOCH": "2026-07-16T23:12:05.817600",
        "MEAN_MOTION": 15.78939225,
        "ECCENTRICITY": 0.00086469,
        "INCLINATION": 41.4717,
        "RA_OF_ASC_NODE": 121.4946,
        "ARG_OF_PERICENTER": 232.6437,
        "MEAN_ANOMALY": 127.3622,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 66515,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 3844,
        "BSTAR": 0.00019672523,
        "MEAN_MOTION_DOT": 0.00035792,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "SOYUZ-MS 28",
        "OBJECT_ID": "2025-275A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 66664,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57565,
        "BSTAR": 0.000072724,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "DUPLEX",
        "OBJECT_ID": "1998-067XS",
        "EPOCH": "2026-07-17T12:53:13.026912",
        "MEAN_MOTION": 15.64693632,
        "ECCENTRICITY": 0.0002448,
        "INCLINATION": 51.626,
        "RA_OF_ASC_NODE": 135.9565,
        "ARG_OF_PERICENTER": 341.3134,
        "MEAN_ANOMALY": 18.777,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 66906,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 3540,
        "BSTAR": 0.00025497,
        "MEAN_MOTION_DOT": 0.00024764,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "KNACKSAT-2",
        "OBJECT_ID": "1998-067XZ",
        "EPOCH": "2026-07-17T05:47:37.449888",
        "MEAN_MOTION": 15.62548905,
        "ECCENTRICITY": 0.0007952,
        "INCLINATION": 51.6276,
        "RA_OF_ASC_NODE": 143.0009,
        "ARG_OF_PERICENTER": 335.1166,
        "MEAN_ANOMALY": 24.9444,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 67683,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 2497,
        "BSTAR": 0.00037883,
        "MEAN_MOTION_DOT": 0.00034112,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "CORAL",
        "OBJECT_ID": "1998-067YA",
        "EPOCH": "2026-07-17T05:32:57.765696",
        "MEAN_MOTION": 15.92998545,
        "ECCENTRICITY": 0.0001613,
        "INCLINATION": 51.6162,
        "RA_OF_ASC_NODE": 131.5425,
        "ARG_OF_PERICENTER": 349.5884,
        "MEAN_ANOMALY": 10.5089,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 67684,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 2512,
        "BSTAR": 0.0010391,
        "MEAN_MOTION_DOT": 0.00369418,
        "MEAN_MOTION_DDOT": 0.000063694
      },
      {
        "OBJECT_NAME": "GXIBA-1",
        "OBJECT_ID": "1998-067YB",
        "EPOCH": "2026-07-17T01:54:45.001728",
        "MEAN_MOTION": 15.64694508,
        "ECCENTRICITY": 0.0008538,
        "INCLINATION": 51.6259,
        "RA_OF_ASC_NODE": 142.6595,
        "ARG_OF_PERICENTER": 332.343,
        "MEAN_ANOMALY": 27.7109,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 67685,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 2496,
        "BSTAR": 0.00041936,
        "MEAN_MOTION_DOT": 0.00041234,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "UITMSAT-2",
        "OBJECT_ID": "1998-067YC",
        "EPOCH": "2026-07-17T06:30:25.727040",
        "MEAN_MOTION": 15.69371078,
        "ECCENTRICITY": 0.000362,
        "INCLINATION": 51.6252,
        "RA_OF_ASC_NODE": 139.4279,
        "ARG_OF_PERICENTER": 328.7912,
        "MEAN_ANOMALY": 31.287,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 67686,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 2502,
        "BSTAR": 0.00051251,
        "MEAN_MOTION_DOT": 0.00061202,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "LEOPARD",
        "OBJECT_ID": "1998-067YD",
        "EPOCH": "2026-07-17T01:19:49.052640",
        "MEAN_MOTION": 15.63900279,
        "ECCENTRICITY": 0.000479,
        "INCLINATION": 51.6281,
        "RA_OF_ASC_NODE": 143.2705,
        "ARG_OF_PERICENTER": 315.638,
        "MEAN_ANOMALY": 44.4231,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 67687,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 2487,
        "BSTAR": 0.00046805,
        "MEAN_MOTION_DOT": 0.00044613,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "HMU-SAT2",
        "OBJECT_ID": "1998-067YE",
        "EPOCH": "2026-07-17T05:36:00.453312",
        "MEAN_MOTION": 15.67103083,
        "ECCENTRICITY": 0.0004034,
        "INCLINATION": 51.6269,
        "RA_OF_ASC_NODE": 140.6937,
        "ARG_OF_PERICENTER": 321.1903,
        "MEAN_ANOMALY": 38.8803,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 67688,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 2492,
        "BSTAR": 0.00047621,
        "MEAN_MOTION_DOT": 0.00051694,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "CREW DRAGON 12",
        "OBJECT_ID": "2026-031A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 67796,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57561,
        "BSTAR": 0.000072724,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "PROGRESS-MS 33",
        "OBJECT_ID": "2026-058A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 68319,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57569,
        "BSTAR": 0.000072724,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "CYGNUS NG-24",
        "OBJECT_ID": "2026-079A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 68689,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57573,
        "BSTAR": 0.000072723697,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "PROGRESS-MS 34",
        "OBJECT_ID": "2026-093A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 68837,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57577,
        "BSTAR": 0.000072723697,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "TIANZHOU-10",
        "OBJECT_ID": "2026-102A",
        "EPOCH": "2026-07-17T12:32:16.126368",
        "MEAN_MOTION": 15.58143309,
        "ECCENTRICITY": 0.00019213,
        "INCLINATION": 41.4691,
        "RA_OF_ASC_NODE": 137.7269,
        "ARG_OF_PERICENTER": 301.5742,
        "MEAN_ANOMALY": 58.4908,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 69049,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 29750,
        "BSTAR": 0.000018813159,
        "MEAN_MOTION_DOT": 0.00001129,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "SHENZHOU-23 (SZ-23)",
        "OBJECT_ID": "2026-113A",
        "EPOCH": "2026-07-17T12:32:16.126368",
        "MEAN_MOTION": 15.58143309,
        "ECCENTRICITY": 0.00019213,
        "INCLINATION": 41.4691,
        "RA_OF_ASC_NODE": 137.7269,
        "ARG_OF_PERICENTER": 301.5742,
        "MEAN_ANOMALY": 58.4908,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 69180,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 29737,
        "BSTAR": 0.000018813159,
        "MEAN_MOTION_DOT": 0.00001129,
        "MEAN_MOTION_DDOT": 0
      },
      {
        "OBJECT_NAME": "SOYUZ-MS 29",
        "OBJECT_ID": "2026-162A",
        "EPOCH": "2026-07-17T13:44:50.076384",
        "MEAN_MOTION": 15.49033131,
        "ECCENTRICITY": 0.0006795,
        "INCLINATION": 51.6316,
        "RA_OF_ASC_NODE": 149.7201,
        "ARG_OF_PERICENTER": 306.9083,
        "MEAN_ANOMALY": 53.1282,
        "EPHEMERIS_TYPE": 0,
        "CLASSIFICATION_TYPE": "U",
        "NORAD_CAT_ID": 100057,
        "ELEMENT_SET_NO": 999,
        "REV_AT_EPOCH": 57558,
        "BSTAR": 0.000072723697,
        "MEAN_MOTION_DOT": 0.00003563,
        "MEAN_MOTION_DDOT": 0
      }
    ]
""".trimIndent()