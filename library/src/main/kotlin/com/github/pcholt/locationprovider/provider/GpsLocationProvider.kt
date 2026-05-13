package com.github.pcholt.locationprovider.provider

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import com.github.pcholt.locationprovider.internal.PermissionChecker
import com.github.pcholt.locationprovider.internal.ProviderAvailability
import com.github.pcholt.locationprovider.internal.isFresh
import com.github.pcholt.locationprovider.internal.toLocationResult
import com.github.pcholt.locationprovider.model.FailureReason
import com.github.pcholt.locationprovider.model.LocationResult
import com.github.pcholt.locationprovider.model.ProviderSource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class GpsLocationProvider(private val context: Context) : LocationStrategy {

    private val locationManager get() =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun isAvailable(): Boolean =
        PermissionChecker.hasFineLocation(context) &&
                ProviderAvailability.isGpsEnabled(context)

    @SuppressLint("MissingPermission")
    override suspend fun fetchLocation(): LocationResult = suspendCancellableCoroutine { cont ->
        val lm = locationManager

        val lastKnown = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (lastKnown != null && lastKnown.isFresh(30_000L)) {
            cont.resume(lastKnown.toLocationResult(ProviderSource.GPS))
            return@suspendCancellableCoroutine
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lm.removeUpdates(this)
                if (cont.isActive) cont.resume(location.toLocationResult(ProviderSource.GPS))
            }

            override fun onProviderDisabled(provider: String) {
                lm.removeUpdates(this)
                if (cont.isActive) cont.resume(LocationResult.Failure(FailureReason.PROVIDER_DISABLED))
            }
        }

        lm.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 0L, 0f, listener, Looper.getMainLooper()
        )
        cont.invokeOnCancellation { lm.removeUpdates(listener) }
    }

    @SuppressLint("MissingPermission")
    override fun updates(minIntervalMs: Long, minDistanceM: Float): Flow<LocationResult> =
        callbackFlow {
            val lm = locationManager
            val listener = LocationListener { location ->
                trySend(location.toLocationResult(ProviderSource.GPS))
            }
            lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, minIntervalMs, minDistanceM,
                listener, Looper.getMainLooper()
            )
            awaitClose { lm.removeUpdates(listener) }
        }

    override fun source() = ProviderSource.GPS
}
