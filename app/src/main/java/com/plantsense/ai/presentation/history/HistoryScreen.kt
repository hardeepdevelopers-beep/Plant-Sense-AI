package com.plantsense.ai.presentation.history

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.plantsense.ai.R
import com.plantsense.ai.domain.model.ScanHistoryItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.plantsense.ai.presentation.navigation.BottomNavigationBar
import com.plantsense.ai.presentation.navigation.HomeKey
import com.plantsense.ai.presentation.navigation.HistoryKey
import com.plantsense.ai.presentation.navigation.ProfileKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val scanHistory by viewModel.scanHistory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var selectedItem by remember { mutableStateOf<ScanHistoryItem?>(null) }
    var itemToDelete by remember { mutableStateOf<ScanHistoryItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_history_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_cd))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentKey = HistoryKey,
                onNavigate = { key ->
                    when (key) {
                        HomeKey -> onNavigateToHome()
                        HistoryKey -> {}
                        ProfileKey -> onNavigateToProfile()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (scanHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            stringResource(R.string.no_scans_found),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.no_scans_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    items(scanHistory, key = { it.id }) { item ->
                        HistoryCard(
                            item = item,
                            onClick = { selectedItem = item },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }
    }

    // Detail dialog when clicking a history card
    selectedItem?.let { item ->
        HistoryDetailDialog(
            item = item,
            onDismiss = { selectedItem = null }
        )
    }

    // Delete History Item Confirmation
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.delete_history_title)) },
            text = { Text(stringResource(R.string.delete_history_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun HistoryCard(
    item: ScanHistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
    val bitmap = remember(item.imageUrl) {
        try {
            BitmapFactory.decodeFile(item.imageUrl)
        } catch (e: Exception) {
            null
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail image
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            ) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Type tag
                val tagColor = if (item.type == "IDENTIFICATION") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                val label = if (item.type == "IDENTIFICATION") stringResource(R.string.identify_tag) else stringResource(R.string.pathology_tag)
                
                Box(
                    modifier = Modifier
                        .background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = label,
                        color = tagColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val displayName = if (item.type == "IDENTIFICATION") {
                    item.plantName ?: stringResource(R.string.unknown_plant)
                } else {
                    item.diseaseName ?: stringResource(R.string.healthy)
                }

                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Delete action
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_from_history_cd),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun HistoryDetailDialog(
    item: ScanHistoryItem,
    onDismiss: () -> Unit
) {
    val bitmap = remember(item.imageUrl) {
        try {
            BitmapFactory.decodeFile(item.imageUrl)
        } catch (e: Exception) {
            null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.type == "IDENTIFICATION") stringResource(R.string.scan_archive_title) else stringResource(R.string.diagnosis_archive_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(R.string.close_cd))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Image
                    item {
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }

                    // Content
                    if (item.type == "IDENTIFICATION") {
                        item {
                            Text(
                                text = item.plantName ?: stringResource(R.string.unknown_plant),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            item.botanicalName?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        item {
                            HorizontalDivider()
                            Text(
                                stringResource(R.string.overview),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                item.description ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        item {
                            HorizontalDivider()
                            Text(
                                stringResource(R.string.sunlight),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(item.careLight ?: "", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                stringResource(R.string.watering),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(item.careWater ?: "", style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                stringResource(R.string.temperature),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(item.careTemp ?: "", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        // Disease
                        item {
                            Text(
                                text = item.diseaseName ?: stringResource(R.string.healthy),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            item.diseaseSeverity?.let {
                                Text(
                                    text = stringResource(R.string.severity_label, it),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        item {
                            HorizontalDivider()
                            Text(
                                stringResource(R.string.cause),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(item.diseaseCause ?: stringResource(R.string.healthy_plant_default), style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                stringResource(R.string.observed_symptoms),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(item.diseaseSymptoms ?: stringResource(R.string.healthy_plant_default), style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                stringResource(R.string.treatment_recommendations),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(item.diseaseTreatment ?: stringResource(R.string.healthy_plant_default), style = MaterialTheme.typography.bodySmall)

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                stringResource(R.string.prevention_strategy),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(item.diseasePrevention ?: stringResource(R.string.healthy_plant_default), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
