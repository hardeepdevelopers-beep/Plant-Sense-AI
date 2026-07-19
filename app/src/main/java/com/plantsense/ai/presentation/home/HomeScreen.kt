package com.plantsense.ai.presentation.home

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.plantsense.ai.R
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.presentation.navigation.BottomNavigationBar
import com.plantsense.ai.presentation.navigation.HomeKey
import androidx.navigation3.runtime.NavKey
import java.io.File
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToIdentify: (String) -> Unit,
    onNavigateToDisease: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val recentScans by viewModel.recentScans.collectAsState()
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showAnalysisDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showAnalysisDialog = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                currentKey = HomeKey,
                onNavigate = { key ->
                    when (key) {
                        HomeKey -> { /* Already on Home */ }
                        com.plantsense.ai.presentation.navigation.HistoryKey -> onNavigateToHistory()
                        com.plantsense.ai.presentation.navigation.ProfileKey -> onNavigateToProfile()
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Greeting Header
            Text(
                text = getGreeting(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large Scan a Plant card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B5E20) // Deep rich forest green background
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { onNavigateToCamera() },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color(0xFF81C784), // Premium mint green accent
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.scan_a_plant),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons (Capture & Upload)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Capture
                Button(
                    onClick = onNavigateToCamera,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.capture_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Upload
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.upload_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Recent Scans Header
            Text(
                text = stringResource(R.string.recent_scans_label),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // List of Recent Scans
            if (recentScans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_scans_found),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(recentScans, key = { it.id }) { item ->
                        RecentScanRowItem(
                            item = item,
                            onClick = {
                                if (item.type == "IDENTIFICATION") {
                                    onNavigateToIdentify(item.imageUrl)
                                } else {
                                    onNavigateToDisease(item.imageUrl)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Analysis Type Dialog for Picked Images
    if (showAnalysisDialog) {
        Dialog(onDismissRequest = { showAnalysisDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.select_analysis_type),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.choose_how_to_analyze),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Identify option
                    Button(
                        onClick = {
                            showAnalysisDialog = false
                            selectedImageUri?.let { uri ->
                                val file = copyUriToCache(context, uri)
                                file?.let { onNavigateToIdentify(it.absolutePath) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.identify_plant), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Diagnose Option
                    Button(
                        onClick = {
                            showAnalysisDialog = false
                            selectedImageUri?.let { uri ->
                                val file = copyUriToCache(context, uri)
                                file?.let { onNavigateToDisease(it.absolutePath) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(stringResource(R.string.diagnose_disease), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Cancel
                    TextButton(onClick = { showAnalysisDialog = false }) {
                        Text(
                            stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentScanRowItem(
    item: ScanHistoryItem,
    onClick: () -> Unit
) {
    val bitmap = remember(item.imageUrl) {
        try {
            BitmapFactory.decodeFile(item.imageUrl)
        } catch (e: Exception) {
            null
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // Rounded Thumbnail with fallbacks
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (item.type == "IDENTIFICATION") Color(0xFF1B5E20) else Color(0xFFD32F2F)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Scan Info Text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (item.type == "IDENTIFICATION") {
                        item.plantName ?: stringResource(R.string.unknown_plant)
                    } else {
                        item.diseaseName ?: stringResource(R.string.leaf_pathology)
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getRelativeTimeString(item.timestamp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

private fun getRelativeTimeString(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> if (days == 1L) "1 day ago" else "$days days ago"
        hours > 0 -> if (hours == 1L) "1 hour ago" else "$hours hours ago"
        minutes > 0 -> if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
        else -> "Just now"
    }
}

private fun copyUriToCache(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "picked_image_${System.currentTimeMillis()}.jpg")
        val outputStream = file.outputStream()
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        null
    }
}
