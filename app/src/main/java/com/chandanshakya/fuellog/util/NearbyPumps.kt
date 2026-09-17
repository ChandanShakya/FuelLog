package com.chandanshakya.fuellog.util

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class NearbyPump(
    val osmId: Long,
    val name: String,
    val brand: String?,
    val operator: String?,
    val lat: Double,
    val lon: Double,
    val distanceMeters: Double
)

object GeoMath {
    /** Great-circle distance in meters (haversine). */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    fun formatDistance(meters: Double): String =
        if (meters < 1000) "${meters.toInt()} m"
        else "%.1f km".format(meters / 1000.0)
}

/**
 * Nearby fuel stations from OpenStreetMap Overpass API.
 * No map SDK — HTTP + JSON only (keeps APK small).
 */
object OverpassFuelPumps {
    private const val ENDPOINT = "https://overpass-api.de/api/interpreter"
    private const val USER_AGENT = "FuelLog/1.2 (Android; nearby pumps)"

    /** Search radius for nearby stations (7 km). */
    const val DEFAULT_RADIUS_METERS = 7000

    @Throws(Exception::class)
    fun fetchNearby(lat: Double, lon: Double, radiusMeters: Int = DEFAULT_RADIUS_METERS, limit: Int = 40): List<NearbyPump> {
        val query = """
            [out:json][timeout:25];
            (
              node["amenity"="fuel"](around:$radiusMeters,$lat,$lon);
              way["amenity"="fuel"](around:$radiusMeters,$lat,$lon);
            );
            out center tags;
        """.trimIndent()

        val body = "data=" + URLEncoder.encode(query, "UTF-8")
        val conn = URL(ENDPOINT).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15_000
            conn.readTimeout = 25_000
            conn.doOutput = true
            conn.setRequestProperty("User-Agent", USER_AGENT)
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("Overpass HTTP $code")
            }
            val text = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            return parse(lat, lon, text, limit)
        } finally {
            conn.disconnect()
        }
    }

    internal fun parse(originLat: Double, originLon: Double, json: String, limit: Int): List<NearbyPump> {
        val root = JSONObject(json)
        val elements = root.optJSONArray("elements") ?: return emptyList()
        val pumps = ArrayList<NearbyPump>(elements.length())
        for (i in 0 until elements.length()) {
            val el = elements.getJSONObject(i)
            val tags = el.optJSONObject("tags") ?: continue
            if (tags.optString("amenity") != "fuel") continue

            val lat = when {
                el.has("lat") -> el.getDouble("lat")
                el.has("center") -> el.getJSONObject("center").getDouble("lat")
                else -> continue
            }
            val lon = when {
                el.has("lon") -> el.getDouble("lon")
                el.has("center") -> el.getJSONObject("center").getDouble("lon")
                else -> continue
            }
            val name = tags.optString("name").ifBlank {
                tags.optString("brand").ifBlank { tags.optString("operator").ifBlank { "Fuel station" } }
            }
            pumps.add(
                NearbyPump(
                    osmId = el.optLong("id"),
                    name = name,
                    brand = tags.optString("brand").ifBlank { null },
                    operator = tags.optString("operator").ifBlank { null },
                    lat = lat,
                    lon = lon,
                    distanceMeters = GeoMath.distanceMeters(originLat, originLon, lat, lon)
                )
            )
        }
        return pumps.sortedBy { it.distanceMeters }.take(limit)
    }
}
