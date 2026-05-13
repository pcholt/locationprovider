package com.github.pcholt.locationprovider.internal

import android.location.Location
import com.github.pcholt.locationprovider.model.LocationResult
import com.github.pcholt.locationprovider.model.ProviderSource

internal fun Location.toLocationResult(source: ProviderSource): LocationResult.Success =
    LocationResult.Success(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = if (hasAccuracy()) accuracy else Float.MAX_VALUE,
        source = source,
        timestampMs = time
    )

internal fun Location.isFresh(maxAgeMs: Long): Boolean =
    System.currentTimeMillis() - time < maxAgeMs
