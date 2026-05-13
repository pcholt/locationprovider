package com.github.pcholt.locationprovider.provider

import com.github.pcholt.locationprovider.LocationConfig
import com.github.pcholt.locationprovider.http.HttpClient
import com.github.pcholt.locationprovider.http.JsonParser
import com.github.pcholt.locationprovider.model.FailureReason
import com.github.pcholt.locationprovider.model.LocationResult
import com.github.pcholt.locationprovider.model.ProviderSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

internal class IpLocationProvider(private val config: LocationConfig) : LocationStrategy {

    override fun isAvailable(): Boolean = true

    override suspend fun fetchLocation(): LocationResult = withContext(Dispatchers.IO) {
        try {
            val key = config.bigDataCloudApiKey
            val url = "https://api.bigdatacloud.net/data/ip-geolocation-with-confidence" +
                    "?localityLanguage=en&key=$key"
            val json = HttpClient.get(url, config.ipTimeoutMs.toInt())
            val parsed = JsonParser.parseBigDataCloud(json)
                ?: return@withContext LocationResult.Failure(FailureReason.NETWORK_ERROR)
            LocationResult.Success(
                latitude = parsed.first,
                longitude = parsed.second,
                accuracyMeters = parsed.third,
                source = ProviderSource.IP
            )
        } catch (e: Exception) {
            LocationResult.Failure(FailureReason.NETWORK_ERROR, e)
        }
    }

    override fun updates(minIntervalMs: Long, minDistanceM: Float): Flow<LocationResult> = flow {
        // IP geolocation provides a single point-in-time fix; emit once per collection
        emit(fetchLocation())
    }

    override fun source() = ProviderSource.IP
}
