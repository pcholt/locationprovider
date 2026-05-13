package com.github.pcholt.locationprovider.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.pcholt.locationprovider.LocationConfig
import com.github.pcholt.locationprovider.LocationProvider
import com.github.pcholt.locationprovider.sample.databinding.ActivityMainBinding
import com.github.pcholt.locationprovider.model.GeofenceShape
import com.github.pcholt.locationprovider.model.GeofenceResult
import com.github.pcholt.locationprovider.model.LocationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationProvider: LocationProvider
    private var updatesJob: Job? = null

    // Trafalgar Square, London — 500m circle geofence
    private val trafalgarSquare = GeofenceShape.Circle(
        centerLatitude = 51.5080,
        centerLongitude = -0.1281,
        radiusMeters = 500.0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationProvider = LocationProvider(this, LocationConfig(gpsTimeoutMs = 10_000))

        requestPermissionsIfNeeded()

        binding.btnGetLocation.setOnClickListener {
            lifecycleScope.launch {
                binding.tvResult.text = "Fetching location…"
                val result = locationProvider.getLocation()
                binding.tvResult.text = formatResult(result)
            }
        }

        binding.btnToggleUpdates.setOnClickListener {
            if (updatesJob?.isActive == true) {
                updatesJob?.cancel()
                updatesJob = null
                binding.btnToggleUpdates.text = "Start Location Updates"
                binding.tvResult.text = "Updates stopped."
            } else {
                binding.btnToggleUpdates.text = "Stop Location Updates"
                updatesJob = lifecycleScope.launch {
                    locationProvider.locationUpdates()
                        .catch { e -> binding.tvResult.text = "Error: ${e.message}" }
                        .collect { result -> binding.tvResult.text = formatResult(result) }
                }
            }
        }

        binding.btnCheckGeofence.setOnClickListener {
            lifecycleScope.launch {
                binding.tvResult.text = "Checking geofence…"
                val result = locationProvider.checkGeofence(trafalgarSquare)
                binding.tvResult.text = when (result) {
                    is GeofenceResult.Inside  -> "INSIDE Trafalgar Square 500m\n${formatResult(result.location)}"
                    is GeofenceResult.Outside -> "OUTSIDE Trafalgar Square 500m\n${formatResult(result.location)}"
                    is GeofenceResult.LocationFailed -> "Location failed: ${result.result}"
                }
            }
        }
    }

    private fun formatResult(result: LocationResult): String = when (result) {
        is LocationResult.Success ->
            "Source: ${result.source}\n" +
            "Lat: ${result.latitude}\n" +
            "Lng: ${result.longitude}\n" +
            "Accuracy: ${result.accuracyMeters}m"
        is LocationResult.Failure -> "Failed: ${result.reason} — ${result.cause?.message}"
        LocationResult.Unavailable -> "All providers unavailable"
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1)
        }
    }
}
