package com.plantsense.ai.presentation.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.plantsense.ai.R
import com.plantsense.ai.domain.model.ScanHistoryItem
import com.plantsense.ai.domain.model.ScanType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToIdentify: (String, Int) -> Unit,
    onNavigateToDisease: (String, Int) -> Unit,
    onBack: () -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val scanHistory by viewModel.scanHistory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    var itemToDelete by remember { mutableStateOf<ScanHistoryItem?>(null) }
    
    // UI Filters and local Favorites list mapping
    var selectedFilter by remember { mutableStateOf("All") }
    val favorites = remember { mutableStateListOf<Int>() }
    
    // Pull to refresh simulation
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val filteredHistory = remember(scanHistory, selectedFilter, favorites.size) {
        scanHistory.filter { item ->
            when (selectedFilter) {
                "All" -> true
                "Healthy" -> {
                    item.type == ScanType.DISEASE && (item.diseaseName.isNullOrEmpty() || item.diseaseName == "Healthy")
                }
                "Diseased" -> {
                    item.type == ScanType.DISEASE && !(item.diseaseName.isNullOrEmpty() || item.diseaseName == "Healthy")
                }
                "Favorites" -> {
                    favorites.contains(item.id)
                }
                else -> true
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.scan_history_title), fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack, 
                            contentDescription = stringResource(R.string.back_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = bottomBar,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Premium Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text(stringResource(R.string.search_placeholder), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)) },
                leadingIcon = { Icon(imageVector = Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(imageVector = Icons.Rounded.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            )

            // Horizontal Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                val chipList = listOf("All", "Healthy", "Diseased", "Favorites")
                items(chipList.size) { index ->
                    val filterName = chipList[index]
                    val isSelected = selectedFilter == filterName
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filterName },
                        label = { Text(text = filterName, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                            selectedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Pull to Refresh Box wrapping content
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    coroutineScope.launch {
                        isRefreshing = true
                        delay(1200)
                        isRefreshing = false
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (filteredHistory.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedFilter == "Favorites") Icons.Rounded.Favorite else Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = if (selectedFilter == "Favorites") "No favorites saved" else stringResource(R.string.no_scans_found),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (selectedFilter == "Favorites") {
                                    "Heart scans in your history to filter them here."
                                } else {
                                    "Start scanning plants to compile and save your botanical vigor reports."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                modifier = Modifier.width(280.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = onNavigateToHome,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Start Scanning",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Lazy List with Cascading Animation Wrapper
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(filteredHistory, key = { _, item -> item.id }) { index, item ->
                            AnimatedHistoryCard(index = index) {
                                HistoryCard(
                                    item = item,
                                    isFavorited = favorites.contains(item.id),
                                    onFavoriteToggle = {
                                        if (favorites.contains(item.id)) {
                                            favorites.remove(item.id)
                                        } else {
                                            favorites.add(item.id)
                                        }
                                    },
                                    onClick = {
                                        if (item.type == ScanType.IDENTIFICATION) {
                                            onNavigateToIdentify(item.imageUrl, item.id)
                                        } else {
                                            onNavigateToDisease(item.imageUrl, item.id)
                                        }
                                    },
                                    onDelete = { itemToDelete = item }
                                )
                            }
                        }
                        
                        // Bottom spacer padding for the bottom bar pill overlay
                        item {
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text(stringResource(R.string.delete_history_title), fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.delete_history_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(item.id)
                        favorites.remove(item.id)
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.delete_label), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text(stringResource(R.string.cancel), fontWeight = FontWeight.SemiBold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// Sequential cascading slide + fade animations
@Composable
private fun AnimatedHistoryCard(
    index: Int,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Sequenced entry cascade delays
        delay(index * 60L)
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 400)) + 
                slideInVertically(
                    animationSpec = tween(durationMillis = 400, easing = EaseOutBack),
                    initialOffsetY = { it / 3 }
                ),
        exit = fadeOut(animationSpec = tween(durationMillis = 200)),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun HistoryCard(
    item: ScanHistoryItem,
    isFavorited: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • hh:mm a", Locale.getDefault()) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large Crop Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            ) {
                AsyncImage(
                    model = File(item.imageUrl),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Information Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Header tags row (Type badge + Vigor/Confidence indicators)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tagColor = if (item.type == ScanType.IDENTIFICATION) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                    val label = if (item.type == ScanType.IDENTIFICATION) "Species" else "Pathology"
                    
                    // Type Tag
                    Box(
                        modifier = Modifier
                            .background(tagColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = label.uppercase(Locale.getDefault()),
                            color = tagColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // Confidence / Healthy Badges
                    if (item.type == ScanType.IDENTIFICATION) {
                        val percent = ((item.confidence ?: 0.85) * 100).toInt()
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$percent% MATCH",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    } else {
                        val isHealthy = item.diseaseName.isNullOrEmpty() || item.diseaseName == "Healthy"
                        val isDark = isSystemInDarkTheme()
                        val badgeBg = if (isHealthy) {
                            Color(0xFF2E7D32).copy(alpha = 0.1f)
                        } else {
                            if (isDark) Color(0xFFFFB300).copy(alpha = 0.15f) else Color(0xFFE65100).copy(alpha = 0.1f)
                        }
                        val badgeTint = if (isHealthy) {
                            if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                        } else {
                            if (isDark) Color(0xFFFFD54F) else Color(0xFFD84315)
                        }
                        val badgeLabel = if (isHealthy) "HEALTHY VIGOR" else "DISEASE ALERT"
                        val badgeIcon = if (isHealthy) Icons.Rounded.Spa else Icons.Rounded.Warning
                        
                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = badgeIcon,
                                    contentDescription = null,
                                    tint = badgeTint,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = badgeLabel,
                                    color = badgeTint,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Plant / Disease Title
                val displayName = if (item.type == ScanType.IDENTIFICATION) {
                    item.plantName ?: stringResource(R.string.unknown_plant)
                } else {
                    item.diseaseName ?: stringResource(R.string.healthy)
                }

                Text(
                    text = displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Formatted Date
                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Heart Toggle + Delete Icons + Navigation Chevron (highly compact)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Favorites heart toggle
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onFavoriteToggle() }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite scan",
                        tint = if (isFavorited) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete icon
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onDelete() }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.delete_from_history_cd),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Nav Chevron
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
