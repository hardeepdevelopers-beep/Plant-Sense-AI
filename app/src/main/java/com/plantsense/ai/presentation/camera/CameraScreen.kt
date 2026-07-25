package com.plantsense.ai.presentation.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.plantsense.ai.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToIdentify: (String) -> Unit,
    onNavigateToDisease: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    
    val viewModel: CameraViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val hasApiKey by viewModel.hasApiKey.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.app_name),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .clickable { backPressedDispatcher?.onBackPressed() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.back_cd),
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                CameraViewfinder(
                    hasApiKey = hasApiKey,
                    onPhotoCaptured = { file, mode ->
                        if (mode == "IDENTIFY") {
                            onNavigateToIdentify(file.absolutePath)
                        } else {
                            onNavigateToDisease(file.absolutePath)
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.camera_permission_required),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.camera_permission_explanation),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(stringResource(R.string.grant_permission), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Floating API Key Warning Banner
            AnimatedVisibility(
                visible = !hasApiKey,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToProfile() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Warning,
                                contentDescription = stringResource(R.string.warning_cd),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.api_key_warning),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CameraViewfinder(
    hasApiKey: Boolean,
    onPhotoCaptured: (File, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Dynamic lens configuration and flash control hooks
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    
    // Switch dynamic mode tabs
    var activeMode by remember { mutableStateOf("IDENTIFY") } // "IDENTIFY" or "DIAGNOSE"

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Capture lifecycle provider binds on selector state changes
    LaunchedEffect(lensFacing) {
        val cameraProviderProvider = ProcessCameraProvider.getInstance(context)
        cameraProviderProvider.addListener({
            val cameraProvider = cameraProviderProvider.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                cameraControl = camera.cameraControl
                cameraControl?.enableTorch(isFlashOn)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    LaunchedEffect(isFlashOn) {
        cameraControl?.enableTorch(isFlashOn)
    }

    // Photo Gallery picker copy logic
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val destFile = copyUriToInternalStorage(context, it)
            if (destFile != null) {
                onPhotoCaptured(destFile, activeMode)
            } else {
                errorMessage = "Failed to copy picked image."
            }
        }
    }

    // Infinite transitions for scanning laser and breathing corner overlays
    val infiniteTransition = rememberInfiniteTransition(label = "hud_scan")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_offset"
    )
    val cornerPulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corner_pulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay vignette gradient for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // Rounded Scanning Cutout with Sweeping Laser Line & Corner Indicators
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 140.dp) // Offset above center to avoid shutter overlap
        ) {
            val frameSizePx = 250.dp.toPx()
            val cornerRadiusPx = 32.dp.toPx()
            
            val left = (size.width - frameSizePx) / 2
            val top = (size.height - frameSizePx) / 2
            
            val cutoutPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(
                            offset = Offset(left, top),
                            size = Size(frameSizePx, frameSizePx)
                        ),
                        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                    )
                )
            }
            
            // Draw background overlay tint except for the center viewfinder cutout
            clipPath(path = cutoutPath, clipOp = ClipOp.Difference) {
                drawRect(color = Color.Black.copy(alpha = 0.55f))
            }
            
            // Draw Animated Corner Guidelines
            val strokeWidth = 3.dp.toPx()
            val cornerLength = 24.dp.toPx()
            val cornerColor = Color.White
            
            val scaleOffset = (frameSizePx * (cornerPulse - 1.0f)) / 2
            val animLeft = left - scaleOffset
            val animTop = top - scaleOffset
            val animRight = left + frameSizePx + scaleOffset
            val animBottom = top + frameSizePx + scaleOffset
            
            // Top-Left
            drawPath(
                path = Path().apply {
                    moveTo(animLeft, animTop + cornerLength)
                    lineTo(animLeft, animTop)
                    lineTo(animLeft + cornerLength, animTop)
                },
                color = cornerColor,
                style = Stroke(width = strokeWidth)
            )
            
            // Top-Right
            drawPath(
                path = Path().apply {
                    moveTo(animRight - cornerLength, animTop)
                    lineTo(animRight, animTop)
                    lineTo(animRight, animTop + cornerLength)
                },
                color = cornerColor,
                style = Stroke(width = strokeWidth)
            )
            
            // Bottom-Left
            drawPath(
                path = Path().apply {
                    moveTo(animLeft, animBottom - cornerLength)
                    lineTo(animLeft, animBottom)
                    lineTo(animLeft + cornerLength, animBottom)
                },
                color = cornerColor,
                style = Stroke(width = strokeWidth)
            )
            
            // Bottom-Right
            drawPath(
                path = Path().apply {
                    moveTo(animRight - cornerLength, animBottom)
                    lineTo(animRight, animBottom)
                    lineTo(animRight, animBottom - cornerLength)
                },
                color = cornerColor,
                style = Stroke(width = strokeWidth)
            )
            
            // Sweeping Laser Line
            val laserY = animTop + (animBottom - animTop) * laserOffset
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0xFF2E7D32).copy(alpha = 0.8f),
                        Color(0xFF8BC34A),
                        Color(0xFF2E7D32).copy(alpha = 0.8f),
                        Color.Transparent
                    )
                ),
                start = Offset(animLeft + 6.dp.toPx(), laserY),
                end = Offset(animRight - 6.dp.toPx(), laserY),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Instructions overlay floating under scanning frame
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 180.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Text(
                    text = "Align the plant inside the frame.",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Error snackbar overlay
        errorMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .statusBarsPadding()
                    .padding(top = 64.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Snackbar(
                    action = {
                        TextButton(onClick = { errorMessage = null }) {
                            Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(msg)
                }
            }
        }

        // Pixel-style mode selection and shutter controller layout
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Select Tabs (IDENTIFY vs DIAGNOSE)
            Row(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeTab(
                    label = "IDENTIFY",
                    isSelected = activeMode == "IDENTIFY",
                    onClick = { activeMode = "IDENTIFY" }
                )
                ModeTab(
                    label = "DIAGNOSE",
                    isSelected = activeMode == "DIAGNOSE",
                    onClick = { activeMode = "DIAGNOSE" }
                )
            }

            // Glassmorphic Floating Controller Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(36.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(36.dp))
                    .padding(vertical = 12.dp, horizontal = 20.dp)
            ) {
                // Left Controls: Gallery & Flash
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery Upload Shortcut
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = "Upload from Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Flash / Torch control
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isFlashOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
                            .clickable { isFlashOn = !isFlashOn },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFlashOn) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                            contentDescription = "Toggle Flash",
                            tint = if (isFlashOn) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Center Capture Shutter Button
                val shutterScale by animateFloatAsState(
                    targetValue = if (isCapturing) 0.85f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "shutter_scale"
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable(enabled = hasApiKey && !isCapturing) {
                            isCapturing = true
                            errorMessage = null
                            capturePhoto(
                                context = context,
                                imageCapture = imageCapture,
                                executor = cameraExecutor,
                                onPhotoCaptured = { file ->
                                    isCapturing = false
                                    onPhotoCaptured(file, activeMode)
                                },
                                onCaptureError = {
                                    isCapturing = false
                                    errorMessage = "Capture failed, try again."
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White,
                            radius = (size.minDimension / 2) - 2.dp.toPx(),
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawCircle(
                            color = if (hasApiKey) Color.White else Color.White.copy(alpha = 0.4f),
                            radius = ((size.minDimension / 2) - 9.dp.toPx()) * shutterScale
                        )
                    }
                }

             /*   // Right Control: Front/Back Camera Lens Switch
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Cameraswitch,
                        contentDescription = "Switch Camera Selector",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }*/
            }
        }
    }
}

@Composable
private fun ModeTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
            letterSpacing = 0.5.sp
        )
    }
}

// Local helper to copy URI content into internal cache file structure
private fun copyUriToInternalStorage(context: Context, uri: Uri): File? {
    val scansDir = File(context.filesDir, "scans").apply { mkdirs() }
    val destFile = File(scansDir, "picked-${System.currentTimeMillis()}.jpg")
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        destFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onPhotoCaptured: (File) -> Unit,
    onCaptureError: (ImageCaptureException) -> Unit
) {
    val scansDir = File(context.filesDir, "scans").apply { mkdirs() }
    val photoFile = File(
        scansDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                ContextCompat.getMainExecutor(context).execute {
                    onPhotoCaptured(photoFile)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                ContextCompat.getMainExecutor(context).execute {
                    onCaptureError(exception)
                }
            }
        }
    )
}
