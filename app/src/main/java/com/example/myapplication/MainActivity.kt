package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

enum class Screen {
    Loading, Connect, Scanning, Connected, Indicators, Settings
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf(Screen.Loading) }
    var isScanningComplete by remember { mutableStateOf(false) }
    var showIndicators by remember { mutableStateOf(false) }
    var wearOfFilters by remember { mutableFloatStateOf(0.95f) }
    var atmosphericLiquidGenerator by remember { mutableFloatStateOf(1.0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            when (currentScreen) {
                Screen.Loading -> LoadingScreen { currentScreen = Screen.Connect }
                Screen.Connect -> ConnectScreen {
                    currentScreen = Screen.Scanning
                }

                Screen.Scanning -> {
                    LaunchedEffect(key1 = true) {
                        delay(3000) // Simulate 2-second QR scan
                        isScanningComplete = true
                        currentScreen = Screen.Connected
                    }
                    QrCodeScannerScreen()
                }

                Screen.Connected -> {
                    ConnectedScreen(
                        isScanningComplete,
                        showIndicators,
                        onShowIndicatorsChange = { showIndicators = it },
                        onSettingsClick = { currentScreen = Screen.Settings }
                    )
                }

                Screen.Indicators -> {
                    IndicatorsScreen()
                }

                Screen.Settings -> {
                    SettingsScreen(
                        onBackClick = { currentScreen = Screen.Connected },
                        wearOfFilters = wearOfFilters,
                        atmosphericLiquidGenerator = atmosphericLiquidGenerator,
                        onWearOfFiltersChange = { wearOfFilters = it },
                        onAtmosphericLiquidGeneratorChange = { atmosphericLiquidGenerator = it }
                    )
                }
            }
        }
    }
}


@Composable
fun LoadingScreen(onTimeout: () -> Unit) {
    var animationPlayed by remember { mutableStateOf(false) }
    val percentage = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        percentage.animateTo(
            targetValue = 100f,
            animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
        )
        animationPlayed = true
        onTimeout()
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to AERO Bottle", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        BottleLoadingAnimation(percentage = percentage.value)
    }
}

@Composable
fun BottleLoadingAnimation(percentage: Float) {
    Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val path = Path().apply {
                moveTo(width * 0.2f, height * 0.9f)
                lineTo(width * 0.8f, height * 0.9f)
                lineTo(width * 0.9f, height * 0.1f)
                lineTo(width * 0.1f, height * 0.1f)
                close()
            }
            drawPath(path, color = Color.Gray, style = Stroke(width = 5f))
            clipPath(path) {
                drawRect(
                    color = Color.Blue,
                    topLeft = Offset(0f, height * (1 - percentage / 100f)),
                    size = Size(width, height * (percentage / 100f))
                )
            }
        }
        Text(text = "${percentage.toInt()}%", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun ConnectScreen(onConnectClicked: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onConnectClicked) {
            Text(text = "Connect the new device")
        }
    }
}

@Composable
fun ConnectedScreen(
    isScanningComplete: Boolean,
    showIndicators: Boolean,
    onShowIndicatorsChange: (Boolean) -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = isScanningComplete && !showIndicators) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Successfully connected",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onShowIndicatorsChange(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text(text = "OK", color = Color.White)
                    }
                }
            }

            AnimatedVisibility(visible = showIndicators) {
                IndicatorsScreen()
            }
        }

        // Settings Button (Top-Left Corner)
        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
        }
    }
}

@Composable
fun QrCodeScannerScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.camera_preview), // Use your camera_preview.png
            contentDescription = "QR Code Scanner",
            modifier = Modifier.fillMaxSize()
        )
        Text(text = "Scanning...", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun WaterQualityIndicator(
    label: String = "Water Quality",
    defaultPercentage: Float = 99f,
    color: Color = Color.Green,
    onAnalyzeComplete: (Float) -> Unit
) {
    var isAnalyzing by remember { mutableStateOf(false) }
    var currentPercentage by remember { mutableStateOf(defaultPercentage) }
    var analysisPercentage by remember { mutableStateOf(0f) }
    val animatedPercentage = remember { Animatable(currentPercentage) }

    val labelBelow = when {
        currentPercentage >= 80f -> "Allowed to drink"
        currentPercentage >= 60f -> "Be careful"
        else -> "Do not drink"
    }

    val indicatorColor = when {
        currentPercentage >= 80f -> Color.Green
        currentPercentage >= 60f -> Color.Yellow
        else -> Color.Red
    }

    LaunchedEffect(key1 = isAnalyzing) {
        if (isAnalyzing) {
            analysisPercentage = 0f
            animatedPercentage.snapTo(0f)
            val randomPercentage = Random.nextInt(50, 100).toFloat()
            animatedPercentage.animateTo(
                targetValue = randomPercentage,
                animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
            )
            delay(3000)
            onAnalyzeComplete(randomPercentage)
            currentPercentage = randomPercentage
            isAnalyzing = false
        } else {
            animatedPercentage.animateTo(
                targetValue = currentPercentage,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            //modifier = Modifier.clickable { // Remove clickable
            //    if (!isAnalyzing) {
            //        isAnalyzing = true
            //    }
            //}
        ) {
            CircularProgressIndicator(
                progress = if (isAnalyzing) animatedPercentage.value / 100f else currentPercentage / 100f,
                modifier = Modifier.size(150.dp),
                color = if (isAnalyzing) color else indicatorColor,
                strokeWidth = 10.dp
            )
            Text(
                text = if (isAnalyzing) "Analyzing..." else "${currentPercentage.toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        if (!isAnalyzing) {
            Text(
                text = labelBelow,
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    currentPercentage >= 80f -> Color.Green
                    currentPercentage >= 60f -> Color.Yellow
                    else -> Color.Red
                }
            )
        }
    }
}

@Composable
fun SettingsPanel(
    onWearOfFiltersChange: (Float) -> Unit,
    onAtmosphericLiquidGeneratorChange: (Float) -> Unit
) {
    var wearOfFiltersValue by remember { mutableFloatStateOf(0.95f) } // Default 95%
    var atmosphericLiquidGeneratorValue by remember { mutableFloatStateOf(1.0f) } // Default 100%

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface) // Add a background
    ) {
        Text(
            text = "Wear of Filters: ${(wearOfFiltersValue * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = wearOfFiltersValue,
            onValueChange = {
                wearOfFiltersValue = it
                onWearOfFiltersChange(it)
            },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Atmospheric Liquid Generator: ${(atmosphericLiquidGeneratorValue * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = atmosphericLiquidGeneratorValue,
            onValueChange = {
                atmosphericLiquidGeneratorValue = it
                onAtmosphericLiquidGeneratorChange(it)
            },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun IndicatorsScreen() {
    var waterQualityPercentage by remember { mutableStateOf(99f) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Indicator(
            label = "Battery",
            percentage = 67f,
            color = Color.Yellow
        )
        Indicator(
            label = "Bottle Fill",
            percentage = 80f,
            color = Color.Blue
        )
        WaterQualityIndicator(
            onAnalyzeComplete = { newPercentage ->
                waterQualityPercentage = newPercentage
            },
            defaultPercentage = waterQualityPercentage
        )
    }
}

@Composable
fun Indicator(
    label: String,
    percentage: Float,
    color: Color,
    labelBelow: String? = null
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000), label = ""
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = animatedPercentage / 100f,
                modifier = Modifier.size(150.dp),
                color = color,
                strokeWidth = 10.dp
            )
            Text(
                text = "${animatedPercentage.toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.titleMedium)
        if (labelBelow != null) {
            Text(
                text = labelBelow,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Green
            )
        }
    }
}

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    wearOfFilters: Float,
    atmosphericLiquidGenerator: Float,
    onWearOfFiltersChange: (Float) -> Unit,
    onAtmosphericLiquidGeneratorChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        // Back Button
        IconButton(onClick = onBackClick) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Content
        SettingsPanel(
            wearOfFilters = wearOfFilters,
            atmosphericLiquidGenerator = atmosphericLiquidGenerator,
            onWearOfFiltersChange = onWearOfFiltersChange,
            onAtmosphericLiquidGeneratorChange = onAtmosphericLiquidGeneratorChange,
            isEditable = false
        )

        // Buy More Consumables Button
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Button(onClick = { /* Handle buy more consumables action */ }) {
                Text("Buy more consumables here")
            }
        }
    }
}

@Composable
fun SettingsPanel(
    wearOfFilters: Float,
    atmosphericLiquidGenerator: Float,
    onWearOfFiltersChange: (Float) -> Unit,
    onAtmosphericLiquidGeneratorChange: (Float) -> Unit,
    isEditable: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        SettingRow(
            label = "Wear of Filters",
            value = wearOfFilters,
            onValueChange = onWearOfFiltersChange,
            isEditable = isEditable
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingRow(
            label = "Atmospheric Liquid Generator",
            value = atmosphericLiquidGenerator,
            onValueChange = onAtmosphericLiquidGeneratorChange,
            isEditable = isEditable
        )
    }
}

@Composable
fun SettingRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    isEditable: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        if (isEditable) {
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..1f,
                modifier = Modifier.width(150.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MyApplicationTheme {
        MainScreen()
    }
}