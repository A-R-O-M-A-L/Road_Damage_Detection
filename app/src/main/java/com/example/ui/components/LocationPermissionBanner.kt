package com.example.ui.components

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CivicBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Accompanist-powered Location Permission component to handle runtime permission requests
 * for high-accuracy GPS coordinates geotagging on road defect reports.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationPermissionState(
    onPermissionsGranted: () -> Unit = {}
): MultiplePermissionsState {
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(permissionState.allPermissionsGranted) {
        if (permissionState.allPermissionsGranted) {
            onPermissionsGranted()
        }
    }

    return permissionState
}

/**
 * Visual rationale / permission helper banner explaining why GPS is needed
 * and triggering the Accompanist permission request.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionBanner(
    permissionState: MultiplePermissionsState,
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit = { permissionState.launchMultiplePermissionRequest() }
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = !permissionState.allPermissionsGranted,
        modifier = modifier
    ) {
        val shouldShowRationale = permissionState.shouldShowRationale

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (shouldShowRationale) Color(0xFFFEF3C7) else Color(0xFFEFF6FF)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (shouldShowRationale) Color(0xFFF59E0B) else CivicBlue.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp)
                )
                .testTag("location_permission_banner")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (shouldShowRationale) Icons.Default.Info else Icons.Default.LocationSearching,
                        contentDescription = null,
                        tint = if (shouldShowRationale) Color(0xFFD97706) else CivicBlue,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (shouldShowRationale) "Precise GPS Permission Required" else "Automatic GPS Geotagging",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (shouldShowRationale) {
                                "Location access was declined earlier. Public Works repair crews require exact GPS coordinates to dispatch maintenance trucks to this exact hazard location."
                            } else {
                                "Grant location permission so CivicFix can auto-tag this road defect with sub-meter GPS coordinates on the municipal dispatch map."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate700,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (shouldShowRationale) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("button_open_app_settings")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open App Settings", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = CivicBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("button_grant_location_permission")
                        ) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Enable GPS Access", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
