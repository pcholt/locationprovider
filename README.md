# locationprovider

[![](https://jitpack.io/v/pcholt/locationprovider.svg)](https://jitpack.io/#pcholt/locationprovider)

An Android location library with **no Google Play Services dependency**. Tracks device location using a priority fallback chain and checks whether the device is inside a geofence.

**Fallback chain:** GPS → Cell/Wi-Fi network triangulation → IP geolocation (BigDataCloud)

---

## Setup

Add JitPack to your repositories in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add the dependency:

```kotlin
implementation("com.github.pcholt:locationprovider:1.0.0")
```

---

## Permissions

The library declares these permissions in its manifest (merged automatically):

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
```

`ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` are dangerous permissions — your app must request them at runtime before calling any location methods.

---

## Usage

### One-shot location fetch

```kotlin
val provider = LocationProvider(context)

lifecycleScope.launch {
    when (val result = provider.getLocation()) {
        is LocationResult.Success -> {
            println("${result.source}: ${result.latitude}, ${result.longitude}")
            println("Accuracy: ${result.accuracyMeters}m")
        }
        is LocationResult.Failure -> println("Failed: ${result.reason}")
        LocationResult.Unavailable -> println("No provider available")
    }
}
```

### Continuous updates (Flow)

```kotlin
lifecycleScope.launch {
    provider.locationUpdates()
        .collect { result ->
            if (result is LocationResult.Success) {
                println("Updated: ${result.latitude}, ${result.longitude} via ${result.source}")
            }
        }
}
```

Cancel the coroutine to stop updates — the underlying `LocationListener` is removed automatically.

### Geofence check

```kotlin
// Circle geofence
val trafalgarSquare = GeofenceShape.Circle(
    centerLatitude = 51.5080,
    centerLongitude = -0.1281,
    radiusMeters = 500.0
)

lifecycleScope.launch {
    when (val result = provider.checkGeofence(trafalgarSquare)) {
        is GeofenceResult.Inside  -> println("Inside the geofence")
        is GeofenceResult.Outside -> println("Outside the geofence")
        is GeofenceResult.LocationFailed -> println("Could not determine location")
    }
}

// Polygon geofence
val polygon = GeofenceShape.Polygon(
    vertices = listOf(
        LatLng(51.51, -0.14),
        LatLng(51.51, -0.10),
        LatLng(51.49, -0.10),
        LatLng(51.49, -0.14)
    )
)
```

You can also check synchronously if you already have a location fix:

```kotlin
val isInside = provider.isInsideGeofence(location, trafalgarSquare)
```

---

## Configuration

```kotlin
val provider = LocationProvider(
    context = context,
    config = LocationConfig(
        gpsTimeoutMs = 15_000L,          // wait up to 15s for a GPS fix
        networkTimeoutMs = 10_000L,       // wait up to 10s for network fix
        ipTimeoutMs = 8_000L,             // HTTP timeout for IP geolocation
        minUpdateIntervalMs = 5_000L,     // minimum interval between flow emissions
        minUpdateDistanceMeters = 10f,    // minimum distance to trigger a flow emission
        bigDataCloudApiKey = "your-key"   // optional; free tier works without a key
    )
)
```

---

## How the fallback chain works

1. **GPS** (`LocationManager.GPS_PROVIDER`) — most accurate, requires `ACCESS_FINE_LOCATION`. Skipped if the provider is disabled or the permission is missing. Falls back after `gpsTimeoutMs` if no fix is obtained.
2. **Network** (`LocationManager.NETWORK_PROVIDER`) — uses cell towers and Wi-Fi access points. Requires `ACCESS_COARSE_LOCATION`. Falls back after `networkTimeoutMs`.
3. **IP geolocation** (BigDataCloud HTTPS API) — city-level accuracy (~1–50 km). Always available when the device has internet. Uses the free endpoint by default; pass a `bigDataCloudApiKey` for higher rate limits.

---

## Known limitations

- Polygon geofences that cross the antimeridian (±180° longitude) are not supported.
- IP geolocation accuracy is city-level and should not be used for precise geofencing.
- Background location updates on API 29+ require `ACCESS_BACKGROUND_LOCATION`, which the consumer app must request separately.
