package com.github.pcholt.locationprovider

import com.github.pcholt.locationprovider.http.HttpClient
import com.github.pcholt.locationprovider.model.FailureReason
import com.github.pcholt.locationprovider.model.LocationResult
import com.github.pcholt.locationprovider.model.ProviderSource
import com.github.pcholt.locationprovider.provider.IpLocationProvider
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class IpLocationProviderTest {

    private val validBdcResponse = """
        {
          "status": 200,
          "location": {
            "latitude": 51.5074,
            "longitude": -0.1278,
            "accuracyRadius": 20
          }
        }
    """.trimIndent()

    @Before
    fun setUp() {
        mockkObject(HttpClient)
    }

    @After
    fun tearDown() {
        unmockkObject(HttpClient)
    }

    @Test
    fun `fetchLocation returns Success with correct coordinates on valid response`() = runTest {
        every { HttpClient.get(any(), any()) } returns validBdcResponse

        val provider = IpLocationProvider(LocationConfig())
        val result = provider.fetchLocation()

        assertTrue(result is LocationResult.Success)
        result as LocationResult.Success
        assertEquals(51.5074, result.latitude, 0.0001)
        assertEquals(-0.1278, result.longitude, 0.0001)
        assertEquals(ProviderSource.IP, result.source)
        assertEquals(20_000f, result.accuracyMeters, 0.1f)
    }

    @Test
    fun `fetchLocation returns Failure when JSON is unparseable`() = runTest {
        every { HttpClient.get(any(), any()) } returns """{"status": 200}"""

        val provider = IpLocationProvider(LocationConfig())
        val result = provider.fetchLocation()

        assertTrue(result is LocationResult.Failure)
        assertEquals(FailureReason.NETWORK_ERROR, (result as LocationResult.Failure).reason)
    }

    @Test
    fun `fetchLocation returns Failure on network exception`() = runTest {
        every { HttpClient.get(any(), any()) } throws java.io.IOException("timeout")

        val provider = IpLocationProvider(LocationConfig())
        val result = provider.fetchLocation()

        assertTrue(result is LocationResult.Failure)
        assertEquals(FailureReason.NETWORK_ERROR, (result as LocationResult.Failure).reason)
    }

    @Test
    fun `isAvailable always returns true`() {
        assertTrue(IpLocationProvider(LocationConfig()).isAvailable())
    }

    @Test
    fun `API key is appended to URL`() {
        var capturedUrl = ""
        every { HttpClient.get(any(), any()) } answers {
            capturedUrl = firstArg()
            validBdcResponse
        }

        runTest {
            IpLocationProvider(LocationConfig(bigDataCloudApiKey = "mykey123")).fetchLocation()
        }

        assertTrue(capturedUrl.contains("key=mykey123"))
    }
}
