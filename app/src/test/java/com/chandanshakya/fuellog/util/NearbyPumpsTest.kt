package com.chandanshakya.fuellog.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyPumpsTest {

    @Test
    fun distance_samePoint_isZero() {
        assertEquals(0.0, GeoMath.distanceMeters(27.7, 85.3, 27.7, 85.3), 0.5)
    }

    @Test
    fun distance_knownPair_isReasonable() {
        // ~1 degree latitude ≈ 111 km
        val d = GeoMath.distanceMeters(27.0, 85.0, 28.0, 85.0)
        assertTrue(d in 100_000.0..120_000.0)
    }

    @Test
    fun formatDistance_metersAndKm() {
        assertEquals("250 m", GeoMath.formatDistance(250.0))
        assertEquals("1.5 km", GeoMath.formatDistance(1500.0))
    }

    @Test
    fun parse_overpassJson_sortsByDistance() {
        val json = """
            {"elements":[
              {"type":"node","id":2,"lat":27.71,"lon":85.32,"tags":{"amenity":"fuel","name":"Far"}},
              {"type":"node","id":1,"lat":27.701,"lon":85.301,"tags":{"amenity":"fuel","name":"Near","brand":"Shell"}},
              {"type":"node","id":3,"lat":27.70,"lon":85.30,"tags":{"amenity":"restaurant","name":"Not a pump"}}
            ]}
        """.trimIndent()
        val pumps = OverpassFuelPumps.parse(27.70, 85.30, json, limit = 10)
        assertEquals(2, pumps.size)
        assertEquals("Near", pumps[0].name)
        assertEquals("Shell", pumps[0].brand)
        assertTrue(pumps[0].distanceMeters < pumps[1].distanceMeters)
    }
}
