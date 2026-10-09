package com.example.ui.components

import android.content.Context
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.model.Complaint
import com.example.model.Severity
import com.example.ui.theme.CivicBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusRepaired
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Pinned GPS Location preview on Google Maps for citizens when taking a photo.
 */
@Composable
fun CitizenPinnedGpsMapView(
    latitude: Double,
    longitude: Double,
    accuracyMeters: Float,
    formattedAddress: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycle, mapView) {
        mapView.onCreate(Bundle())
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Slate200, RoundedCornerShape(14.dp))
            .testTag("citizen_pinned_gps_map")
    ) {
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { googleMap ->
                        try {
                            googleMap.uiSettings.isZoomControlsEnabled = false
                            googleMap.uiSettings.isMapToolbarEnabled = false
                            val pos = LatLng(latitude, longitude)
                            googleMap.clear()
                            googleMap.addMarker(
                                MarkerOptions()
                                    .position(pos)
                                    .title("Pinned Defect Location")
                                    .snippet(formattedAddress)
                            )
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 16.5f))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { googleMap ->
                    try {
                        val pos = LatLng(latitude, longitude)
                        googleMap.clear()
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title("Pinned Defect Location")
                                .snippet(formattedAddress)
                        )
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 16.5f))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )

        // Floating Precision Badge at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Slate900.copy(alpha = 0.90f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GpsFixed,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = String.format("%.5f° N, %.5f° W", kotlin.math.abs(latitude), kotlin.math.abs(longitude)),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GPS Lock • ±${accuracyMeters.toInt()}m accuracy",
                    color = StatusRepaired,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Embedded Google Map for the Admin Heatmap view, plotting all complaints as markers.
 */
@Composable
fun AdminGoogleMapHeatmapView(
    complaints: List<Complaint>,
    selectedComplaint: Complaint?,
    onSelectComplaint: (Complaint) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context) }

    DisposableEffect(lifecycle, mapView) {
        mapView.onCreate(Bundle())
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag("admin_google_map_view")
    ) {
        AndroidView(
            factory = {
                mapView.apply {
                    getMapAsync { googleMap ->
                        try {
                            googleMap.uiSettings.isZoomControlsEnabled = true
                            googleMap.uiSettings.isCompassEnabled = true

                            val centerPos = LatLng(37.77492, -122.41941)
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPos, 13.5f))

                            updateMapMarkers(googleMap, complaints, onSelectComplaint)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { googleMap ->
                    try {
                        updateMapMarkers(googleMap, complaints, onSelectComplaint)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )

        // Coordinates Legend Badge
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Slate900.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF38BDF8)))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Google Maps SDK • ${complaints.size} Geocoded Pins",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun updateMapMarkers(
    googleMap: com.google.android.gms.maps.GoogleMap,
    complaints: List<Complaint>,
    onSelectComplaint: (Complaint) -> Unit
) {
    googleMap.clear()
    val markerMap = mutableMapOf<String, Complaint>()

    complaints.forEach { c ->
        val pos = LatLng(c.latitude, c.longitude)
        val hue = when (c.severity) {
            Severity.CRITICAL -> BitmapDescriptorFactory.HUE_RED
            Severity.HIGH -> BitmapDescriptorFactory.HUE_ORANGE
            Severity.MEDIUM -> BitmapDescriptorFactory.HUE_YELLOW
            Severity.LOW -> BitmapDescriptorFactory.HUE_GREEN
        }

        val marker = googleMap.addMarker(
            MarkerOptions()
                .position(pos)
                .title("${c.id}: ${c.title}")
                .snippet("Lat: ${c.latitude}, Lon: ${c.longitude} • ${c.severity.label}")
                .icon(BitmapDescriptorFactory.defaultMarker(hue))
        )

        marker?.let { m ->
            markerMap[m.id] = c
        }
    }

    googleMap.setOnMarkerClickListener { marker ->
        markerMap[marker.id]?.let { comp ->
            onSelectComplaint(comp)
        }
        false
    }
}
