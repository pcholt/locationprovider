package com.github.pcholt.locationprovider

import com.github.pcholt.locationprovider.http.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JsonParserTest {

    @Test
    fun `parseBigDataCloud returns coordinates from valid response`() {
        val json = """
            {
              "status": 200,
              "location": {
                "latitude": 51.5074,
                "longitude": -0.1278,
                "accuracyRadius": 10
              }
            }
        """.trimIndent()
        val result = JsonParser.parseBigDataCloud(json)
        assertEquals(51.5074, result!!.first, 0.0001)
        assertEquals(-0.1278, result.second, 0.0001)
        assertEquals(10_000f, result.third, 0.1f)
    }

    @Test
    fun `parseBigDataCloud returns null when location object is missing`() {
        val json = """{"status": 200}"""
        assertNull(JsonParser.parseBigDataCloud(json))
    }

    @Test
    fun `parseBigDataCloud returns null when coordinates are missing`() {
        val json = """{"location": {"accuracyRadius": 5}}"""
        assertNull(JsonParser.parseBigDataCloud(json))
    }

    @Test
    fun `parseBigDataCloud returns null for malformed JSON`() {
        assertNull(JsonParser.parseBigDataCloud("not json"))
    }

    @Test
    fun `parseBigDataCloud defaults accuracyRadius to 50km when absent`() {
        val json = """
            {
              "location": {
                "latitude": 40.7128,
                "longitude": -74.0060
              }
            }
        """.trimIndent()
        val result = JsonParser.parseBigDataCloud(json)
        assertEquals(50_000f, result!!.third, 0.1f)
    }
}
