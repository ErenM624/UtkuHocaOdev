package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MachineControlScreen()
            }
        }
    }
}

@Composable
fun MachineControlScreen() {
    val navyBlue = Color(0xFF000066)
    val startGreen = Color(0xFF228B22)
    val stopRed = Color(0xFFB22222)
    val cardBg = Color(0x14FFFFFF)
    val dividerColor = Color(0x1FFFFFFF)

    var packageCount by remember { mutableStateOf(0) }
    var confirmedCount by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var isRunning by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var showToast by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    val scope = rememberCoroutineScope()

    fun showToastMessage(msg: String) {
        toastMessage = msg
        showToast = true
        scope.launch {
            delay(2000)
            showToast = false
        }
    }

    fun startMachine() {
        if (confirmedCount == 0) {
            showToastMessage("Önce paket sayısını onaylayın!")
            return
        }
        isRunning = true
        progress = 0f
        scope.launch {
            while (isRunning && progress < 1f) {
                delay(200)
                progress = (progress + (1f / confirmedCount)).coerceAtMost(1f)
            }
            if (progress >= 1f) {
                isRunning = false
                showToastMessage("İşlem tamamlandı!")
            }
        }
    }

    fun stopMachine() {
        isRunning = false
        showToastMessage("Makine durduruldu.")
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = navyBlue
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .safeDrawingPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Header ──────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DEĞİRMENCİ MAKİNE",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Paketleme Otomasyon\nMakinesi",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                // ── Durum Kartı ──────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardBg)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isRunning) Color(0xFF4ADE80) else Color(0xFFF87171))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Durum",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = if (isRunning) "Çalışıyor" else "Beklemede",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── İlerleme ─────────────────────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "İlerleme",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                        Text(
                            text = "%${(animatedProgress * 100).toInt()}",
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF4ADE80))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = dividerColor, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // ── Paket Sayısı ──────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paket Sayısı",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 14.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Eksi butonu
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { if (packageCount > 0) packageCount-- },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("−", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Light)
                        }
                        // Sayı kutusu
                        Box(
                            modifier = Modifier
                                .size(52.dp, 32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.95f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = packageCount.toString(),
                                color = Color(0xFF000066),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        // Artı butonu
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .clickable { packageCount++ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Light)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Onayla Butonu ─────────────────────────────────────
                Button(
                    onClick = {
                        confirmedCount = packageCount
                        showToastMessage("$confirmedCount paket onaylandı.")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "ONAYLA",
                        color = Color(0xFF000066),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ── Başlat / Dur Butonları ────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { startMachine() },
                        enabled = !isRunning,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = startGreen,
                            disabledContainerColor = startGreen.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("▶  BAŞLAT", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = { stopMachine() },
                        enabled = isRunning,
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = stopRed,
                            disabledContainerColor = stopRed.copy(alpha = 0.35f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("■  DUR", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── Footer ────────────────────────────────────────────
                Text(
                    text = "24015221020 · Hüseyin Eren DEĞİRMENCİ",
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // ── Toast ─────────────────────────────────────────────────
            if (showToast) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.95f))
                            .padding(horizontal = 20.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = toastMessage,
                            color = Color(0xFF000066),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MachineControlPreview() {
    MyApplicationTheme {
        MachineControlScreen()
    }
}