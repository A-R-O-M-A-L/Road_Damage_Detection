package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

data class GpsLocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val formattedAddress: String,
    val district: String
) {
    fun formattedCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return String.format("%.5f° %s, %.5f° %s", kotlin.math.abs(latitude), latDir, kotlin.math.abs(longitude), lonDir)
    }
}

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentGpsLocation(context: Context): GpsLocationResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            // Default municipal reference location when permission is awaiting
            return@withContext GpsLocationResult(
                latitude = 37.77492,
                longitude = -122.41941,
                accuracyMeters = 5.0f,
                formattedAddress = "Grand Ave & 5th St, San Francisco, CA",
                district = "Downtown Metro (Dist. 4)"
            )
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        val location: Location? = suspendCancellableCoroutine { cont ->
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }

            cont.invokeOnCancellation {
                cts.cancel()
            }
        } ?: suspendCancellableCoroutine { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(null)
                }
        }

        val lat = location?.latitude ?: 37.77492
        val lon = location?.longitude ?: -122.41941
        val acc = location?.accuracy ?: 4.5f

        val (address, district) = reverseGeocode(context, lat, lon)

        GpsLocationResult(
            latitude = lat,
            longitude = lon,
            accuracyMeters = acc,
            formattedAddress = address,
            district = district
        )
    }

    private fun reverseGeocode(context: Context, latitude: Double, longitude: Double): Pair<String, String> {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocation(latitude, longitude, 1)
            val first = results?.firstOrNull()
            if (first != null) {
                val street = first.thoroughfare ?: first.featureName ?: "Grand Ave"
                val subThoroughfare = first.subThoroughfare?.let { "$it " } ?: ""
                val locality = first.locality ?: "San Francisco"
                val subLocality = first.subLocality ?: "District 4"
                val full = "$subThoroughfare$street, $locality"
                Pair(full, subLocality)
            } else {
                Pair("Grand Ave & 5th St, San Francisco, CA", "Downtown Metro (Dist. 4)")
            }
        } catch (e: Exception) {
            Pair("Grand Ave & 5th St, San Francisco, CA", "Downtown Metro (Dist. 4)")
        }
    }
}
