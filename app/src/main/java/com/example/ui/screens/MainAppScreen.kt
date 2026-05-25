package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.JarvisState
import com.example.ui.JarvisViewModel
import com.example.ui.components.JarvisOrb
import com.example.ui.theme.JarvisAccentPurple
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisCardBg
import com.example.ui.theme.JarvisPrimaryCyan
import com.example.ui.theme.JarvisSuccessGreen
import com.example.ui.theme.JarvisTextHigh
import com.example.ui.theme.JarvisTextLow
import com.example.ui.theme.JarvisBorderColor
import com.example.ui.theme.JarvisActiveCyan
import com.example.ui.theme.JarvisGlassBg
import com.example.data.database.ReminderEntity
import com.example.util.SystemCommandHelper
import com.example.util.JarvisThemeEngine
import com.example.util.ThemingMode
import androidx.compose.foundation.Canvas
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun MainAppScreen(viewModel: JarvisViewModel) {
    var bootComplete by remember { mutableStateOf(false) }

    Crossfade(targetState = bootComplete, label = "boot_crossfade") { complete ->
        if (!complete) {
            JarvisBootScreen(onBootFinished = { bootComplete = true })
        } else {
            JarvisNavigationScaffold(viewModel = viewModel)
        }
    }
}

// 1. AI BOOT SEQUENCE SCREEN
@Composable
fun JarvisBootScreen(onBootFinished: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }
    val bootLogs = remember { mutableStateListOf<String>() }

    val diagnostics = listOf(
        "CORE ARCR: ONLINE [100% POWER]",
        "SYSTEM UPLINK: BROADCAST SYNC",
        "COGNITIVE MATRIX: INTERCONNECTING",
        "INTENT ENGN: MAPPED [OFFLINE CMD]",
        "NEURAL INTEGRITY: SCAN ACTIVE",
        "SENSORY ARRAY STATUS: LEVEL 5",
        "J.A.R.V.I.S. FIRMWARE V3.5 SYNC"
    )

    LaunchedEffect(Unit) {
        // Play animated terminal logs sequentially
        diagnostics.forEachIndexed { index, log ->
            kotlinx.coroutines.delay(350L)
            bootLogs.add(log)
            progress = (index + 1).toFloat() / diagnostics.size
        }
        kotlinx.coroutines.delay(500)
        onBootFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Neon JARVIS Title
            Text(
                text = "J.A.R.V.I.S.",
                color = JarvisPrimaryCyan,
                fontSize = 42.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "INTELLIGENT SYSTEM COGNITION UPLINK",
                color = JarvisAccentPurple,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Animated Terminal Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.3f)), RoundedCornerShape(8.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(bootLogs) { log ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "► ",
                                color = JarvisPrimaryCyan,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = log,
                                color = JarvisTextHigh,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Progress Indicators
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(CircleShape),
                color = JarvisPrimaryCyan,
                trackColor = JarvisAccentPurple.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "BOOT LOAD STATUS: ${(progress * 100).toInt()}%",
                color = JarvisPrimaryCyan,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// 2. SYSTEM STATUS HEADER PORTAL (HIGH DENSITY HUD)
@Composable
fun JarvisSystemHeader(viewModel: JarvisViewModel) {
    val isLiteMode by viewModel.isLiteMode.collectAsState()

    // Dynamic incrementing seconds representing Uptime statistics
    var uptimeCounter by remember { mutableStateOf(50575) } // Starting point around 14 hours
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            uptimeCounter++
        }
    }

    val formattedUptime = remember(uptimeCounter) {
        val hrs = (uptimeCounter / 3600) % 24
        val mins = (uptimeCounter / 60) % 60
        val secs = uptimeCounter % 60
        String.format(Locale.US, "%02d:%02d:%02d", hrs, mins, secs)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(JarvisBackground)
            .padding(start = 16.coordsPadding(), top = 12.coordsPadding(), end = 16.coordsPadding(), bottom = 6.coordsPadding())
    ) {
        // High density protocol title & stats top row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "SYSTEM PROTOCOL",
                    color = JarvisPrimaryCyan.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "J.A.R.V.I.S.",
                        color = JarvisTextHigh,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-0.5).sp
                    )
                    Box(
                        modifier = Modifier
                            .background(JarvisPrimaryCyan.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.4f)), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isLiteMode) "V5.0-LITE" else "V5.0-HIGH",
                            color = JarvisPrimaryCyan,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // High Density system progress loads (RAM / CPU meters)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // CPU load bar (HTML themed simulation)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CPU LOAD",
                        color = JarvisTextHigh.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(5.dp)
                            .background(Color(0xFF0F172A), CircleShape)
                            .border(BorderStroke(0.5.dp, JarvisPrimaryCyan.copy(alpha = 0.2f)), CircleShape)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.12f)
                                .background(JarvisPrimaryCyan)
                        )
                    }
                }

                // RAM load bar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RAM 1.2G",
                        color = JarvisTextHigh.copy(alpha = 0.5f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .width(50.dp)
                            .height(5.dp)
                            .background(Color(0xFF0F172A), CircleShape)
                            .border(BorderStroke(0.5.dp, JarvisPrimaryCyan.copy(alpha = 0.15f)), CircleShape)
                            .clip(CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.64f)
                                .background(JarvisPrimaryCyan)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Cybernetic Telemetry Overlay line
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "LAT: 40.7128° N | LON: 74.0060° W | ALT: 12.4M",
                color = JarvisPrimaryCyan.copy(alpha = 0.35f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "UPTIME: $formattedUptime | PING: 14MS",
                color = JarvisPrimaryCyan.copy(alpha = 0.35f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Futuristic neon gradient separator divider row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            JarvisPrimaryCyan.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

// Extension inline helper to verify proper insets/padding
private fun Int.coordsPadding(): androidx.compose.ui.unit.Dp = this.dp

// 2. MAIN NAV SCAFFOLD
@Composable
fun JarvisNavigationScaffold(viewModel: JarvisViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val isLiteMode by viewModel.isLiteMode.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JarvisBackground,
        topBar = {
            JarvisSystemHeader(viewModel = viewModel)
        },
        bottomBar = {
            NavigationBar(
                containerColor = JarvisCardBg,
                tonalElevation = 8.dp,
                modifier = Modifier.border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.15f)), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                val items = listOf(
                    Triple("hud", "HUD", Icons.Default.Terminal),
                    Triple("chat", "CHAT", Icons.Default.Chat),
                    Triple("vision", "VISION", Icons.Default.Videocam),
                    Triple("tasks", "TASKS", Icons.Default.Task),
                    Triple("settings", "CORE", Icons.Default.Settings)
                )
                items.forEach { (tab, label, icon) ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // High Density exact HTML-style miniature glowing dots indicators above selected core tab
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) JarvisPrimaryCyan else Color.Transparent)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
                            }
                        },
                        label = { Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JarvisPrimaryCyan,
                            unselectedIconColor = JarvisTextLow,
                            selectedTextColor = JarvisPrimaryCyan,
                            unselectedTextColor = JarvisTextLow,
                            indicatorColor = JarvisActiveCyan
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // High fidelity ambient glowing dots background (Only if NOT in Lite Mode)
            if (!isLiteMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(80.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(JarvisAccentPurple.copy(alpha = 0.08f), Color.Transparent),
                                center = Offset(200f, 200f),
                                radius = 600f
                            )
                        )
                )
            }

            // Route Display
            when (selectedTab) {
                "hud" -> JarvisHudScreen(viewModel)
                "chat" -> JarvisChatScreen(viewModel)
                "vision" -> JarvisVisionScreen(viewModel)
                "tasks" -> JarvisTasksScreen(viewModel)
                "settings" -> JarvisSettingsScreen(viewModel)
            }
        }
    }
}

// 3. HUD SCREEN (CENTRAL CONSOLE)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun JarvisHudScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val jarvisState by viewModel.jarvisState.collectAsState()
    val rawSpeechText by viewModel.speechText.collectAsState()
    val isLiteMode by viewModel.isLiteMode.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

    val lastCommand = remember(conversations) {
        conversations.lastOrNull { it.sender == "user" }?.message ?: "Open my morning briefing and check weather"
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val width = maxWidth
        val isTablet = width > 600.dp

        if (isTablet) {
            // Grid-breaking Tablet Side-By-Side Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    JarvisOrb(
                        state = jarvisState,
                        isLiteMode = isLiteMode,
                        modifier = Modifier.size(260.dp),
                        onClick = {
                            if (recordAudioPermissionState.status.isGranted) {
                                if (jarvisState == JarvisState.LISTENING) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    viewModel.startVoiceListening()
                                }
                            } else {
                                recordAudioPermissionState.launchPermissionRequest()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HudFeedbackLabel(jarvisState)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    TelemetryDashboard(context)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Tactical Last Directive Indicator
                    LastDirectiveBar(
                        lastCommand = lastCommand,
                        jarvisState = jarvisState,
                        recordAudioPermissionState = recordAudioPermissionState,
                        viewModel = viewModel
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HudInputControls(
                        textInput = textInput,
                        onTextInputChanged = { textInput = it },
                        onSendProgress = {
                            viewModel.processTextPrompt(textInput)
                            textInput = ""
                        },
                        onMicTrigger = {
                            if (recordAudioPermissionState.status.isGranted) {
                                viewModel.startVoiceListening()
                            } else {
                                recordAudioPermissionState.launchPermissionRequest()
                            }
                        }
                    )
                }
            }
        } else {
            // Unified Mobile Vertical Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top telemetry dashboard parameters (High Density Grid blocks)
                TelemetryDashboard(context)

                // Central Active ORB
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    JarvisOrb(
                        state = jarvisState,
                        isLiteMode = isLiteMode,
                        modifier = Modifier.size(200.dp),
                        onClick = {
                            if (recordAudioPermissionState.status.isGranted) {
                                if (jarvisState == JarvisState.LISTENING) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    viewModel.startVoiceListening()
                                }
                            } else {
                                recordAudioPermissionState.launchPermissionRequest()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HudFeedbackLabel(jarvisState)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Tactical Last Directive Indicator
                    LastDirectiveBar(
                        lastCommand = lastCommand,
                        jarvisState = jarvisState,
                        recordAudioPermissionState = recordAudioPermissionState,
                        viewModel = viewModel
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive HUD Command Controls
                    HudInputControls(
                        textInput = textInput,
                        onTextInputChanged = { textInput = it },
                        onSendProgress = {
                            viewModel.processTextPrompt(textInput)
                            textInput = ""
                        },
                        onMicTrigger = {
                            if (recordAudioPermissionState.status.isGranted) {
                                if (jarvisState == JarvisState.LISTENING) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    viewModel.startVoiceListening()
                                }
                            } else {
                                recordAudioPermissionState.launchPermissionRequest()
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LastDirectiveBar(
    lastCommand: String,
    jarvisState: JarvisState,
    recordAudioPermissionState: com.google.accompanist.permissions.PermissionState,
    viewModel: JarvisViewModel
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x990F172A), RoundedCornerShape(24.dp)) // Slate border frame background
            .border(BorderStroke(1.dp, Color(0x13FFFFFF)), RoundedCornerShape(24.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "LAST COGNITIVE DIRECTIVE",
                    color = JarvisPrimaryCyan.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "\"$lastCommand\"",
                    color = JarvisTextHigh,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(JarvisPrimaryCyan.copy(alpha = 0.08f), CircleShape)
                    .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.3f)), CircleShape)
                    .clickable {
                        if (recordAudioPermissionState.status.isGranted) {
                            if (jarvisState == JarvisState.LISTENING) {
                                viewModel.stopVoiceListening()
                            } else {
                                viewModel.startVoiceListening()
                            }
                        } else {
                            recordAudioPermissionState.launchPermissionRequest()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (jarvisState == JarvisState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Trigger Speech Sync",
                    tint = JarvisPrimaryCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HudFeedbackLabel(state: JarvisState) {
    val message = when (state) {
        JarvisState.IDLE -> "CORE STATUS READY. CLICK ORB TO STREAM SPEAK"
        JarvisState.LISTENING -> "LISTENING TO SIR... SPEAK NOW"
        JarvisState.PROCESSING -> "UPLINKING DIALOG DIAGNOSTICS WITH COGNITIVE CORE..."
        JarvisState.SPEAKING -> "J.A.R.V.I.S TRANSCRIPTION RESPONDING..."
    }
    val textColor = if (state == JarvisState.PROCESSING) JarvisAccentPurple else JarvisPrimaryCyan

    Text(
        text = message,
        color = textColor,
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun TelemetryDashboard(context: Context) {
    // Read hardware levels
    val batteryPct = remember(context) { SystemCommandHelper.getBatteryDiagnostics(context) }
    val storageSpace = remember { SystemCommandHelper.getStorageDiagnostics() }
    val memoryUsage = remember(context) { SystemCommandHelper.getSystemMetrics(context) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Double Cell Grid to match "Home Control" and "AI Vision" beautiful grids of high-density template
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reactor Cell
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(JarvisGlassBg, RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, JarvisBorderColor), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(JarvisPrimaryCyan, CircleShape)
                        )
                        Text(
                            text = "REACTOR CORE",
                            color = JarvisPrimaryCyan,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = batteryPct.replace("Sir, ", "").trim(),
                        color = JarvisTextHigh.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            // Data Header
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(JarvisGlassBg, RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, JarvisBorderColor), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(JarvisAccentPurple, CircleShape)
                        )
                        Text(
                            text = "AI VISION SCAN",
                            color = JarvisAccentPurple,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = storageSpace.replace("Sir, ", "").trim(),
                        color = JarvisTextHigh.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Full Width Cognitive metrics cell
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(JarvisGlassBg, RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, JarvisBorderColor), RoundedCornerShape(16.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(JarvisSuccessGreen, CircleShape)
                    )
                    Text(
                        text = "COGNITIVE SPEED MATRIX LOAD",
                        color = JarvisSuccessGreen,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = memoryUsage.replace("Sir, ", "").trim(),
                    color = JarvisTextHigh,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}


@Composable
fun HudInputControls(
    textInput: String,
    onTextInputChanged: (String) -> Unit,
    onSendProgress: () -> Unit,
    onMicTrigger: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = textInput,
            onValueChange = onTextInputChanged,
            placeholder = { Text("Synthesize text commands...", color = JarvisTextLow, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextHigh, fontFamily = FontFamily.Monospace),
            modifier = Modifier
                .weight(1f)
                .testTag("command_input"),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = JarvisTextHigh,
                unfocusedTextColor = JarvisTextLow,
                focusedBorderColor = JarvisPrimaryCyan,
                unfocusedBorderColor = JarvisPrimaryCyan.copy(alpha = 0.4f),
                cursorColor = JarvisPrimaryCyan,
                focusedContainerColor = JarvisCardBg,
                unfocusedContainerColor = JarvisCardBg.copy(alpha = 0.4f)
            ),
            trailingIcon = {
                if (textInput.isNotEmpty()) {
                    IconButton(onClick = onSendProgress, modifier = Modifier.testTag("send_button")) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = JarvisPrimaryCyan)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Futuristic floating microphone toggle button
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(colors = listOf(JarvisAccentPurple.copy(alpha = 0.8f), JarvisAccentPurple)))
                .clickable { onMicTrigger() }
                .testTag("mic_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Mic, contentDescription = "Mic Trigger", tint = Color.White)
        }
    }
}

// 4. CHAT INTERFACE
@Composable
fun JarvisChatScreen(viewModel: JarvisViewModel) {
    val history by viewModel.conversations.collectAsState()
    var promptInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(key1 = history.size) {
        if (history.isNotEmpty()) {
            lazyListState.animateScrollToItem(history.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "SECURE COMMUNICATION ARCHIVE",
            color = JarvisPrimaryCyan,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Conversation list
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.15f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg.copy(alpha = 0.5f))
        ) {
            if (history.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, contentDescription = "Empty Log", tint = JarvisTextLow, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("SIR, LOGS ARE CURRENTLY EMPTY.", color = JarvisTextLow, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text("Synthesize a request to establish cognitive logic.", color = JarvisTextLow.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(history) { chat ->
                        val isUser = chat.sender == "user"
                        ChatBubble(sender = chat.sender, message = chat.message, isUser = isUser)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Typing input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Provide prompt logs...", color = JarvisTextLow, fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextHigh, fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisPrimaryCyan,
                    unfocusedBorderColor = JarvisPrimaryCyan.copy(alpha = 0.4f),
                    focusedContainerColor = JarvisCardBg
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (promptInput.trim().isNotEmpty()) {
                        viewModel.processTextPrompt(promptInput)
                        promptInput = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(JarvisPrimaryCyan, CircleShape)
                    .testTag("chat_send_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = "Query Link", tint = JarvisBackground)
            }
        }
    }
}

@Composable
fun ChatBubble(sender: String, message: String, isUser: Boolean) {
    val alignFloat = if (isUser) Alignment.End else Alignment.Start
    val cardColor = if (isUser) JarvisAccentPurple.copy(alpha = 0.35f) else JarvisPrimaryCyan.copy(alpha = 0.15f)
    val txtHeader = if (isUser) "SIR" else if (sender == "jarvis_vision") "J.A.R.V.I.S [VISION CORE]" else "J.A.R.V.I.S."

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .focusable(),
        horizontalAlignment = alignFloat
    ) {
        Text(
            text = txtHeader,
            color = if (isUser) JarvisAccentPurple else JarvisPrimaryCyan,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 3.dp)
        )
        Card(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 12.dp
            ),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Text(
                text = message,
                color = JarvisTextHigh,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

// 5. VISION SCANNER (CAMERA) SCREEN
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun JarvisVisionScreen(viewModel: JarvisViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val scanResult by viewModel.visionScanResult.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.runVisionScan(bitmap, "Sir requests diagnostic analysis for this file upload. Scan text, values and objects.")
                }
            } catch (e: Exception) {
                Log.e("JarvisVision", "Error loading pic", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "OPTICAL RETINAL DECODER SCANNER",
            color = JarvisPrimaryCyan,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Outer Scanner Feed block
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxWidth()
                .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.25f)), RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (cameraPermissionState.status.isGranted) {
                // Living Camera Feed View
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val imageCapture = ImageCapture.Builder().build()
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                            } catch (e: Exception) {
                                Log.e("JarvisVision", "Use case binding failure", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // High fidelity glowing laser sweep line
                val infiniteTransition = rememberInfiniteTransition(label = "laser_sweep")
                val sweepY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 3000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sweep_y"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lineY = this.size.height * sweepY
                    drawLine(
                        color = JarvisPrimaryCyan,
                        start = Offset(0f, lineY),
                        end = Offset(this.size.width, lineY),
                        strokeWidth = 3f
                    )
                    // Halo Glow blur around laser sweep
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(JarvisPrimaryCyan.copy(alpha = 0.2f), Color.Transparent),
                            startY = lineY - 30f,
                            endY = lineY + 5f
                        ),
                        topLeft = Offset(0f, lineY - 30f),
                        size = androidx.compose.ui.geometry.Size(this.size.width, 35f)
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Videocam, contentDescription = "Camera Required", tint = JarvisTextLow, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { cameraPermissionState.launchPermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisPrimaryCyan),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("GRANT OPTICAL CLEARANCE", color = JarvisBackground, fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scanner readouts diagnostic cards
        Card(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxWidth()
                .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.15f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg.copy(alpha = 0.5f))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                item {
                    Column(modifier = Modifier.focusable()) {
                        Text(
                            text = "SCANNER READOUT STREAM",
                            color = JarvisAccentPurple,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = scanResult ?: "SIR, PRESS SCAN PROTOCOL BELOW OR TRANSFER CHIP CORES FOR HIGH-DENSITY SCAN ANALYSIS.",
                            color = JarvisTextHigh,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Command Scanning Actions keys
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Upload picker key
            Button(
                onClick = { filePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp)
                    .testTag("upload_image_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCardBg),
                border = BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Upload", tint = JarvisPrimaryCyan)
                Spacer(modifier = Modifier.width(6.dp))
                Text("IMPORT CORE", color = JarvisTextHigh, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            // Simulate/Analyze scanner button
            Button(
                onClick = {
                    // Simulate optical capture by analyzing a static HUD template internally
                    val simulatedBitmap = Bitmap.createBitmap(160, 160, Bitmap.Config.ARGB_8888)
                    viewModel.runVisionScan(simulatedBitmap, "Simulate scan diagnosis of dynamic structural outlines.")
                } ,
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("scan_optics_button"),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisPrimaryCyan)
            ) {
                Icon(Icons.Default.Camera, contentDescription = "Capture", tint = JarvisBackground)
                Spacer(modifier = Modifier.width(6.dp))
                Text("SCAN MODULE", color = JarvisBackground, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 6. TASKS / SCHEDULER PROTOCOLS SCREEN
@Composable
fun JarvisTasksScreen(viewModel: JarvisViewModel) {
    val tasks by viewModel.reminders.collectAsState()
    var titleInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "PRIORITY ASSIGNMENT GRID",
            color = JarvisPrimaryCyan,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Create form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.15f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "ANCHOR NEW SCHEDULER KEY",
                    color = JarvisAccentPurple,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = { Text("Task header name...", color = JarvisTextLow, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextHigh, fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JarvisPrimaryCyan, unfocusedBorderColor = JarvisBorderColor)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    placeholder = { Text("Task content description...", color = JarvisTextLow, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = JarvisTextHigh, fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JarvisPrimaryCyan, unfocusedBorderColor = JarvisBorderColor)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (titleInput.trim().isNotEmpty() && noteInput.trim().isNotEmpty()) {
                            viewModel.addNewReminder(titleInput, noteInput)
                            titleInput = ""
                            noteInput = ""
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("anchor_task_button"),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisPrimaryCyan)
                ) {
                    Text("+ ANCHOR CORE", color = JarvisBackground, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tasks flow list container
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.1f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg.copy(alpha = 0.4f))
        ) {
            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("NO SHIELDS ASSIGNED SIR. ALL GRIDS CLEAR.", color = JarvisTextLow, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks) { task ->
                        TaskRowItem(task = task, onChecked = { viewModel.toggleTask(task.id, it) }, onDelete = { viewModel.purgeTask(task.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRowItem(task: ReminderEntity, onChecked: (Boolean) -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, JarvisBorderColor), RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onChecked,
                colors = CheckboxDefaults.colors(
                    checkedColor = JarvisPrimaryCyan,
                    checkmarkColor = JarvisBackground,
                    uncheckedColor = JarvisTextLow
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) JarvisTextLow else JarvisTextHigh,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = task.content,
                    color = JarvisTextLow,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Purge Task", tint = JarvisAccentPurple, modifier = Modifier.size(16.dp))
        }
    }
}

// 7. DEVELOPER CONTROLS & SETTINGS PANEL
@Composable
fun JarvisSettingsScreen(viewModel: JarvisViewModel) {
    val isLiteMode by viewModel.isLiteMode.collectAsState()
    val context = LocalContext.current
    val currentOverride by JarvisThemeEngine.currentOverrideMode.collectAsState()
    val totalRamGb = JarvisThemeEngine.totalRamGb
    val cpuCores = JarvisThemeEngine.processorCores
    val score = JarvisThemeEngine.performanceScore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "HARDWARE SYSTEMS & LINK DIAGNOSTICS",
            color = JarvisPrimaryCyan,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.15f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "VISUAL PIPELINE OPTIMIZATION",
                    color = JarvisAccentPurple,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Render Hardware Specifications
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // CPU Gauge Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x221E293B), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("CPU CORES", color = JarvisTextLow, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            Text("$cpuCores Cores", color = JarvisTextHigh, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    // RAM Gauge Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x221E293B), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("TOTAL RAM", color = JarvisTextLow, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            Text(String.format(Locale.US, "%.2f GB", totalRamGb), color = JarvisTextHigh, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Score Box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0x221E293B), RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.1f)), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("SYS SCORE", color = JarvisTextLow, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                            Text("$score pts", color = JarvisTextHigh, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Current Active Visual Tier description line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isLiteMode) "ACTIVE PIPELINE: PERFORMANCE LITE" else "ACTIVE PIPELINE: FULL HOLOGRAPHIC",
                            color = if (isLiteMode) JarvisSuccessGreen else JarvisPrimaryCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLiteMode) "Holographic overlays disabled. Fast, low-impact visual rendering." else "Fluid glowing dynamic particles & complex crosshair rotations running on-grid.",
                            color = JarvisTextLow,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (currentOverride == ThemingMode.AUTO) JarvisPrimaryCyan.copy(alpha = 0.12f) else JarvisAccentPurple.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (currentOverride == ThemingMode.AUTO) JarvisPrimaryCyan.copy(alpha = 0.4f) else JarvisAccentPurple.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (currentOverride == ThemingMode.AUTO) "AUTO-LINKED" else "OVERRIDDEN",
                            color = if (currentOverride == ThemingMode.AUTO) JarvisPrimaryCyan else JarvisAccentPurple,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Interactive Manual Override options: Row of 3 modern cyberpunk selector tabs
                Text(
                    text = "MANUAL RESOLUTION OVERRIDE PROTOCOL",
                    color = JarvisTextLow.copy(alpha = 0.8f),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val overrideOptions = listOf(
                        ThemingMode.AUTO to "AUTO SENSE",
                        ThemingMode.FORCE_FULL to "FORCE FULL",
                        ThemingMode.FORCE_LITE to "FORCE LITE"
                    )

                    overrideOptions.forEach { (modeOption, label) ->
                        val isSelected = currentOverride == modeOption
                        val interactionSource = remember { MutableInteractionSource() }
                        val isFocused by interactionSource.collectIsFocusedAsState()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) JarvisPrimaryCyan.copy(alpha = 0.15f) else if (isFocused) JarvisPrimaryCyan.copy(alpha = 0.05f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) JarvisPrimaryCyan else if (isSelected) JarvisPrimaryCyan else JarvisPrimaryCyan.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .focusable(interactionSource = interactionSource)
                                .clickable(interactionSource = interactionSource, indication = androidx.compose.foundation.LocalIndication.current) {
                                    JarvisThemeEngine.setThemingMode(context, modeOption)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) JarvisPrimaryCyan else JarvisTextLow,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System matrix card info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(BorderStroke(1.dp, JarvisPrimaryCyan.copy(alpha = 0.1f)), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = JarvisCardBg.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MODULE STATUS PANEL",
                    color = JarvisAccentPurple,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                DiagnosticStatusItem(title = "Local database storage logic", active = true)
                DiagnosticStatusItem(title = "Holographic custom visual drawings", active = true)
                DiagnosticStatusItem(title = "Malayalam linguistics parsing logic", active = true)
                DiagnosticStatusItem(title = "Intelligent retrofitted Gemini REST bridge", active = true)
                DiagnosticStatusItem(title = "Optical scanning camera overlay decoder", active = true)
                DiagnosticStatusItem(title = "Microphone intent spectrum capture support", active = true)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System copyright info
        Text(
            text = "J.A.R.V.I.S. FIRMWARE BUILD INTEGRITY MARK III.\nDESIGNED BY COGNITIVE COMPOSERS 2026.",
            color = JarvisTextLow,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DiagnosticStatusItem(title: String, active: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Active Status Icon",
            tint = if (active) JarvisSuccessGreen else JarvisTextLow,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = JarvisTextHigh,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
