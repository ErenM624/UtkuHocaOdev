package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.*

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

// ─── Renk Paleti ──────────────────────────────────────────────────────────────
private val BgDeep        = Color(0xFF0D0D0F)
private val BgCard        = Color(0xFF1A1A1E)
private val BgCardBorder  = Color(0xFF2C2C30)
private val AccentRed     = Color(0xFFE53935)
private val AccentRedDark = Color(0xFFB71C1C)
private val AccentGreen   = Color(0xFF00C853)
private val AccentGreenDk = Color(0xFF00701E)
private val AccentOrange  = Color(0xFFFF6D00)
private val TextPrimary   = Color(0xFFF0F0F0)
private val TextSecondary = Color(0xFF888890)
private val TextMuted     = Color(0xFF444450)
private val LogoStripe    = Color(0xFFE53935)

// ─── Durum Enum ───────────────────────────────────────────────────────────────
enum class MachineState { IDLE, RUNNING, STOPPED, DONE }

@Composable
fun MachineControlScreen() {
    val scope = rememberCoroutineScope()

    var machineState   by remember { mutableStateOf(MachineState.IDLE) }
    var progress       by remember { mutableStateOf(0) }
    var uretilen       by remember { mutableStateOf(0) }
    var hedef          by remember { mutableStateOf(0) }
    var inputValue     by remember { mutableStateOf("0") }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var logLines       by remember { mutableStateOf(listOf("Sistem hazır.", "Paket sayısı girin.")) }
    var job            by remember { mutableStateOf<Job?>(null) }

    // Pulsing animasyonu (durum lambası için)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseAlpha"
    )

    // İlerleme animasyonu
    val animatedProgress by animateIntAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "progress"
    )

    fun addLog(msg: String) {
        logLines = (listOf(msg) + logLines).take(4)
    }

    fun formatTime(s: Int): String =
        "%02d:%02d".format(s / 60, s % 60)

    fun stopMachine() {
        job?.cancel()
        machineState = MachineState.STOPPED
        addLog("Durduruldu: $uretilen/$hedef paket.")
    }

    fun startMachine() {
        if (hedef <= 0) { addLog("HATA: Paket sayısı 0 olamaz!"); return }
        if (machineState == MachineState.RUNNING) return
        uretilen = 0
        progress = 0
        elapsedSeconds = 0
        machineState = MachineState.RUNNING
        addLog("Makine başlatıldı. Hedef: $hedef paket.")

        job = scope.launch {
            val timerJob = launch {
                while (isActive) {
                    delay(1000)
                    elapsedSeconds++
                }
            }
            while (uretilen < hedef && isActive) {
                delay(250)
                uretilen++
                progress = ((uretilen.toFloat() / hedef) * 100).toInt()
            }
            timerJob.cancel()
            if (uretilen >= hedef) {
                machineState = MachineState.DONE
                addLog("✓ Tamamlandı! $hedef paket üretildi.")
                addLog("Süre: ${formatTime(elapsedSeconds)}")
            }
        }
    }

    fun onConfirm() {
        if (machineState == MachineState.RUNNING) return
        val v = inputValue.toIntOrNull() ?: 0
        hedef = v.coerceAtLeast(0)
        uretilen = 0; progress = 0; elapsedSeconds = 0
        addLog("Hedef ayarlandı: $hedef paket.")
    }

    // ─── UI ───────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(AccentRed, AccentRedDark)
                        )
                    )
                    .drawBehind {
                        // Alt şerit
                        drawRect(
                            color = AccentOrange.copy(alpha = 0.6f),
                            topLeft = Offset(0f, size.height - 3f),
                            size = size.copy(height = 3f)
                        )
                    }
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        text = "PAKETLEME OTOMASYON MAKİNESİ",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        letterSpacing = 3.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "DEĞİRMENCİ MAKİNE",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
                // Durum lambası (sağ üst)
                val dotColor = when (machineState) {
                    MachineState.RUNNING -> AccentGreen
                    MachineState.STOPPED -> AccentRed
                    MachineState.DONE    -> AccentGreen
                    MachineState.IDLE    -> TextMuted
                }
                val alpha = if (machineState == MachineState.RUNNING) pulseAlpha else 1f
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = when (machineState) {
                            MachineState.RUNNING -> "ÇALIŞIYOR"
                            MachineState.STOPPED -> "DURDU"
                            MachineState.DONE    -> "TAMAM"
                            MachineState.IDLE    -> "BEKLİYOR"
                        },
                        color = dotColor.copy(alpha = alpha),
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor.copy(alpha = alpha))
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Bilgi Kartları (3'lü) ──────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf(
                    Triple("HEDEF", if (hedef > 0) "$hedef" else "—", TextPrimary),
                    Triple("ÜRETİLEN", "$uretilen", AccentGreen),
                    Triple("SÜRE", formatTime(elapsedSeconds), AccentOrange)
                ).forEach { (label, value, color) ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = BorderStroke(0.5.dp, BgCardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(label, color = TextMuted, fontSize = 9.sp, letterSpacing = 2.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Yüzde Göstergesi ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(BgCard)
                    .border(2.dp, BgCardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Yay (ilerleme halkası)
                val sweepAngle = animatedProgress * 3.6f
                Canvas(modifier = Modifier.size(180.dp)) {
                    // Arkaplan halkası
                    drawArc(
                        color = BgCardBorder,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 12.dp.toPx()),
                        topLeft = Offset(18.dp.toPx(), 18.dp.toPx()),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - 36.dp.toPx(), size.height - 36.dp.toPx()
                        )
                    )
                    // İlerleme halkası
                    if (sweepAngle > 0f) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(AccentGreen, AccentGreen, AccentOrange)
                            ),
                            startAngle = -90f,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 12.dp.toPx(),
                                cap = StrokeCap.Round
                            ),
                            topLeft = Offset(18.dp.toPx(), 18.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                size.width - 36.dp.toPx(), size.height - 36.dp.toPx()
                            )
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%$animatedProgress",
                        color = if (animatedProgress == 100) AccentGreen else TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 44.sp
                    )
                    Text(
                        text = "TAMAMLANDI",
                        color = TextMuted,
                        fontSize = 9.sp,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Log Kutusu ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0F)),
                border = BorderStroke(0.5.dp, BgCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    logLines.forEachIndexed { i, line ->
                        Text(
                            text = "> $line",
                            color = if (i == 0) Color(0xFF33FF66) else TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Ana Butonlar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // BAŞLAT
                Button(
                    onClick = { startMachine() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        disabledContainerColor = AccentGreenDk
                    ),
                    enabled = machineState != MachineState.RUNNING,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "BAŞLAT",
                        color = Color(0xFF002B0E),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }
                // DUR
                Button(
                    onClick = { stopMachine() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed,
                        disabledContainerColor = AccentRedDark
                    ),
                    enabled = machineState == MachineState.RUNNING,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "DUR",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Paket Sayısı Girişi ───────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = BorderStroke(0.5.dp, BgCardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "PAKET SAYISI",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Azalt
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgDeep)
                                .border(0.5.dp, BgCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val v = (inputValue.toIntOrNull() ?: 0) - 1
                                    inputValue = v.coerceAtLeast(0).toString()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("−", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Light)
                        }
                        // Sayı göstergesi
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgDeep)
                                .border(0.5.dp, BgCardBorder, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = inputValue,
                                color = TextPrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                textAlign = TextAlign.Center
                            )
                        }
                        // Artır
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BgDeep)
                                .border(0.5.dp, BgCardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    val v = (inputValue.toIntOrNull() ?: 0) + 1
                                    inputValue = v.toString()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Light)
                        }
                        // Onayla
                        Button(
                            onClick = { onConfirm() },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                            modifier = Modifier.height(48.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(
                                "ONAYLA",
                                color = Color(0xFF1A0800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Alt Bilgi ─────────────────────────────────────────────────────
            Text(
                text = "24015221020 · Hüseyin Eren DEĞİRMENCİ",
                color = TextMuted,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0F, showSystemUi = true)
@Composable
fun MachineControlPreview() {
    MyApplicationTheme {
        MachineControlScreen()
    }
}