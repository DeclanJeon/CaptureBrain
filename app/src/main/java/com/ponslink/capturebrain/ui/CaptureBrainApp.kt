package com.ponslink.capturebrain.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ponslink.capturebrain.ui.model.CaptureBrainSampleData
import com.ponslink.capturebrain.ui.model.CaptureItemUi
import com.ponslink.capturebrain.ui.model.CaptureStatus
import com.ponslink.capturebrain.ui.model.QueueSummaryUi
import com.ponslink.capturebrain.ui.model.SettingsUi

/* ──────────────── Design tokens ──────────────── */

private object Cb {
    val Bg = Color(0xFFF7F8FC)
    val White = Color.White
    val Ink = Color(0xFF111827)
    val Muted = Color(0xFF6B7280)
    val Panel = Color.White
    val SoftPanel = Color(0xFFF0F4FF)
    val Border = Color(0xFFE3E8F2)
    val Blue = Color(0xFF2563EB)
    val BlueSoft = Color(0xFFEAF1FF)
    val Mint = Color(0xFF10B981)
    val MintSoft = Color(0xFFE7F8F2)
    val Amber = Color(0xFFF59E0B)
    val AmberSoft = Color(0xFFFFF7ED)
    val Red = Color(0xFFEF4444)
    val RedSoft = Color(0xFFFFE4E9)
}

private val CardR = RoundedCornerShape(20.dp)
private val PillR = RoundedCornerShape(12.dp)

/* ──────────────── Main ──────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureBrainApp(
    connectedDriveAccount: String? = null,
    driveConnectionError: String? = null,
    imagePermissionGranted: Boolean = false,
    imagePermissionDenied: Boolean = false,
    rootFolderName: String = CaptureBrainSampleData.settings.rootFolderName,
    summary: QueueSummaryUi = CaptureBrainSampleData.queueSummary,
    captures: List<CaptureItemUi> = CaptureBrainSampleData.captures,
    onRootFolderChange: (String) -> Unit = {},
    onRequestImagePermission: () -> Unit = {},
    onConnectDrive: () -> Unit = {},
    onDisconnectDrive: () -> Unit = {},
    onImportRecent: () -> Unit = {},
    onRetryFailed: () -> Unit = {}
) {
    val isReady = !connectedDriveAccount.isNullOrBlank() && imagePermissionGranted
    var rootFolder by rememberSaveable(rootFolderName) { mutableStateOf(rootFolderName) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showLibrary by rememberSaveable { mutableStateOf(false) }
    val folderName = rootFolder.ifBlank { "CaptureBrain" }

    val updateRootFolder: (String) -> Unit = {
        rootFolder = it
        onRootFolderChange(it)
    }

    Scaffold(
        containerColor = Cb.Bg,
        topBar = {
            TopAppBar(
                title = { Text("캡처브레인", fontWeight = FontWeight.Bold, color = Cb.Ink) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cb.Bg),
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Text("⚙", fontSize = 20.sp)
                    }
                }
            )
        }
    ) { padding ->
        if (isReady) {
            DashboardScreen(
                modifier = Modifier.padding(padding),
                folderName = folderName,
                summary = summary,
                captures = captures,
                onImportRecent = onImportRecent,
                onOpenLibrary = { showLibrary = true },
                onRetryFailed = onRetryFailed
            )
        } else {
            OnboardingScreen(
                modifier = Modifier.padding(padding),
                connectedDriveAccount = connectedDriveAccount,
                driveConnectionError = driveConnectionError,
                imagePermissionGranted = imagePermissionGranted,
                imagePermissionDenied = imagePermissionDenied,
                onConnectDrive = onConnectDrive,
                onRequestImagePermission = onRequestImagePermission,
                folderName = folderName
            )
        }
    }

    /* Settings bottom sheet */
    if (showSettings) {
        SettingsSheet(
            settings = CaptureBrainSampleData.settings,
            rootFolder = rootFolder,
            onRootFolderChange = updateRootFolder,
            connectedDriveAccount = connectedDriveAccount,
            driveConnectionError = driveConnectionError,
            imagePermissionGranted = imagePermissionGranted,
            imagePermissionDenied = imagePermissionDenied,
            onConnectDrive = onConnectDrive,
            onDisconnectDrive = onDisconnectDrive,
            onRequestImagePermission = onRequestImagePermission,
            onDismiss = { showSettings = false }
        )
    }

    /* Library bottom sheet */
    if (showLibrary) {
        LibrarySheet(
            captures = captures,
            folderName = folderName,
            onRetryFailed = onRetryFailed,
            onDismiss = { showLibrary = false }
        )
    }
}

/* ──────────────── Onboarding (not-ready) ──────────────── */

@Composable
private fun OnboardingScreen(
    modifier: Modifier = Modifier,
    connectedDriveAccount: String?,
    driveConnectionError: String?,
    imagePermissionGranted: Boolean,
    imagePermissionDenied: Boolean,
    onConnectDrive: () -> Unit,
    onRequestImagePermission: () -> Unit,
    folderName: String
) {
    val driveDone = !connectedDriveAccount.isNullOrBlank()
    val permDone = imagePermissionGranted

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cb.Bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        /* Hero message */
        Card(
            shape = CardR,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, Color(0xFFD7E2F5))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        verticalGradientBrush(listOf(Cb.White, Cb.BlueSoft, Cb.MintSoft))
                    )
                    .padding(28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Pill(text = "2단계만 하면 끝", color = Cb.Blue)
                    Text(
                        "스크린샷, 이제 자동으로 정리해요",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Cb.Ink
                    )
                    Text(
                        "Drive 연결하고 권한만 허용하면, 스크린샷을 찍을 때마다 자동 분류해서 저장합니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Cb.Muted
                    )
                }
            }
        }

        /* Step indicators */
        StepRow(step = 1, title = "Google Drive 연결", done = driveDone)
        if (driveDone) {
            Card(shape = CardR, colors = CardDefaults.cardColors(containerColor = Cb.MintSoft)) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircleCheck()
                    Column(Modifier.weight(1f)) {
                        Text("연결됨", fontWeight = FontWeight.Bold, color = Cb.Mint)
                        Text(
                            connectedDriveAccount.orEmpty(),
                            color = Cb.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        } else {
            Card(shape = CardR, border = BorderStroke(1.dp, Cb.Border)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircleIcon("G", Cb.Blue)
                        Column(Modifier.weight(1f)) {
                            Text("Google Drive", fontWeight = FontWeight.Bold, color = Cb.Ink)
                            Text("스크린샷과 Markdown을 저장할 위치", color = Cb.Muted)
                        }
                    }
                    Button(
                        onClick = onConnectDrive,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("연결하기") }
                }
            }
            if (!driveConnectionError.isNullOrBlank()) {
                Text(driveConnectionError, color = Cb.Red, fontWeight = FontWeight.SemiBold)
            }
        }

        StepRow(step = 2, title = "스크린샷 접근 허용", done = permDone)
        if (permDone) {
            Card(shape = CardR, colors = CardDefaults.cardColors(containerColor = Cb.MintSoft)) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircleCheck()
                    Text("권한 허용됨", fontWeight = FontWeight.Bold, color = Cb.Mint)
                }
            }
        } else {
            Card(
                shape = CardR,
                border = BorderStroke(1.dp, Cb.Border),
                colors = CardDefaults.cardColors(containerColor = if (imagePermissionDenied) Cb.RedSoft else Cb.Panel)
            ) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircleIcon(if (imagePermissionDenied) "!" else "📷", if (imagePermissionDenied) Cb.Amber else Cb.Blue)
                        Column(Modifier.weight(1f)) {
                            Text("스크린샷 접근", fontWeight = FontWeight.Bold, color = Cb.Ink)
                            Text(
                                if (imagePermissionDenied) "설정에서 권한을 다시 허용해야 합니다" else "새 스크린샷을 감지하려면 필요합니다",
                                color = Cb.Muted
                            )
                        }
                    }
                    Button(
                        onClick = onRequestImagePermission,
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (imagePermissionDenied) ButtonDefaults.buttonColors(containerColor = Cb.Amber) else ButtonDefaults.buttonColors()
                    ) { Text(if (imagePermissionDenied) "다시 허용하기" else "허용하기") }
                }
            }
        }

        /* How it works preview */
        Spacer(Modifier.height(8.dp))
        Card(shape = CardR, colors = CardDefaults.cardColors(containerColor = Cb.SoftPanel)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("이렇게 저장돼요", fontWeight = FontWeight.Bold, color = Cb.Ink)
                FlowStep("1", "스크린샷 감지", "새 이미지 발견")
                FlowStep("2", "내용 분석", "카테고리 자동 선택")
                FlowStep("3", folderName, "이미지 + Markdown 저장")
            }
        }
    }
}

/* ──────────────── Dashboard (ready) ──────────────── */

@Composable
private fun DashboardScreen(
    modifier: Modifier = Modifier,
    folderName: String,
    summary: QueueSummaryUi,
    captures: List<CaptureItemUi>,
    onImportRecent: () -> Unit,
    onOpenLibrary: () -> Unit,
    onRetryFailed: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cb.Bg),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /* Status banner */
        item {
            Card(
                shape = CardR,
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, Color(0xFFD7E2F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            verticalGradientBrush(listOf(Cb.White, Cb.MintSoft))
                        )
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Pill(text = "자동 저장 중", color = Cb.Mint)
                        Text(
                            "이제 스크린샷만 찍으세요",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Cb.Ink
                        )
                        Text(
                            "캡처하면 이미지와 Markdown을 자동 분류해 저장합니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Cb.Muted
                        )
                    }
                }
            }
        }

        /* Today summary */
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox(modifier = Modifier.weight(1f), value = "${summary.todayCompleted}", label = "오늘 완료", color = Cb.Mint)
                if (summary.queued > 0) {
                    StatBox(modifier = Modifier.weight(1f), value = "${summary.queued}", label = "처리 중", color = Cb.Blue)
                }
                if (summary.failed > 0) {
                    StatBox(modifier = Modifier.weight(1f), value = "${summary.failed}", label = "실패", color = Cb.Red)
                }
                if (summary.sensitivePending > 0) {
                    StatBox(modifier = Modifier.weight(1f), value = "${summary.sensitivePending}", label = "민감확인", color = Cb.Amber)
                }
                if (summary.queued == 0 && summary.failed == 0 && summary.sensitivePending == 0) {
                    StatBox(modifier = Modifier.weight(1f), value = "0", label = "대기 중", color = Cb.Muted)
                }
            }
        }

        /* Quick actions */
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = onImportRecent,
                    modifier = Modifier.weight(1f)
                ) { Text("최근 스크린샷 찾기") }
                OutlinedButton(
                    onClick = onOpenLibrary,
                    modifier = Modifier.weight(1f)
                ) { Text("보관함 보기") }
            }
        }

        /* Native ad — between quick actions and captures */
        item {
            NativeAdCard()
        }

        /* Recent captures */
        if (captures.isNotEmpty()) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("최근 저장", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Cb.Ink)
                    TextButton(onClick = onOpenLibrary) { Text("전체 보기") }
                }
            }
            items(captures.take(5), key = { it.id }) { capture ->
                CaptureRow(capture = capture, folderName = folderName, onClick = onOpenLibrary)
            }
        }

        /* Save structure */
        item {
            Spacer(Modifier.height(4.dp))
            Card(shape = CardR, colors = CardDefaults.cardColors(containerColor = Cb.SoftPanel)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("저장 구조", fontWeight = FontWeight.Bold, color = Cb.Ink)
                    Text("$folderName / 카테고리 / 스크린샷 · Markdown", color = Cb.Muted)
                }
            }
        }

        /* Bottom spacing */
        item { Spacer(Modifier.height(16.dp)) }
    }
}

/* ──────────────── Settings Sheet ──────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    settings: SettingsUi,
    rootFolder: String,
    onRootFolderChange: (String) -> Unit,
    connectedDriveAccount: String?,
    driveConnectionError: String?,
    imagePermissionGranted: Boolean,
    imagePermissionDenied: Boolean,
    onConnectDrive: () -> Unit,
    onDisconnectDrive: () -> Unit,
    onRequestImagePermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var wifiOnly by rememberSaveable { mutableStateOf(settings.wifiOnlyUpload) }
    var confirmSensitive by rememberSaveable { mutableStateOf(settings.sensitiveConfirmationEnabled) }
    var folder by rememberSaveable(rootFolder) { mutableStateOf(rootFolder) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Cb.Panel
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("설정", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Cb.Ink)

            /* Drive */
            Card(shape = CardR, border = BorderStroke(1.dp, Cb.Border)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircleIcon("G", if (!connectedDriveAccount.isNullOrBlank()) Cb.Mint else Cb.Blue)
                        Column(Modifier.weight(1f)) {
                            Text("Google Drive", fontWeight = FontWeight.Bold, color = Cb.Ink)
                            Text(
                                if (!connectedDriveAccount.isNullOrBlank()) connectedDriveAccount else "연결 안 됨",
                                color = Cb.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!connectedDriveAccount.isNullOrBlank()) {
                            TextButton(onClick = onDisconnectDrive) { Text("변경", color = Cb.Blue) }
                        } else {
                            Button(onClick = onConnectDrive) { Text("연결") }
                        }
                    }
                    if (!driveConnectionError.isNullOrBlank()) {
                        Text(driveConnectionError, color = Cb.Red, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            /* Folder */
            Card(shape = CardR, border = BorderStroke(1.dp, Cb.Border)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("저장 폴더", fontWeight = FontWeight.Bold, color = Cb.Ink)
                    OutlinedTextField(
                        value = folder,
                        onValueChange = {
                            folder = it
                            onRootFolderChange(it)
                        },
                        placeholder = { Text("CaptureBrain") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("비워두면 CaptureBrain", color = Cb.Muted)
                }
            }

            /* Permission */
            Card(shape = CardR, border = BorderStroke(1.dp, Cb.Border)) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircleIcon(if (imagePermissionGranted) "✓" else "!", if (imagePermissionGranted) Cb.Mint else Cb.Amber)
                    Column(Modifier.weight(1f)) {
                        Text("스크린샷 접근", fontWeight = FontWeight.Bold, color = Cb.Ink)
                        Text(if (imagePermissionGranted) "허용됨" else "허용 필요", color = Cb.Muted)
                    }
                    if (!imagePermissionGranted) {
                        Button(onClick = onRequestImagePermission) { Text("허용") }
                    }
                }
            }

            /* Toggles */
            ToggleRow("Wi-Fi에서만 업로드", wifiOnly) { wifiOnly = it }
            ToggleRow("민감정보 확인", confirmSensitive) { confirmSensitive = it }

            /* Privacy note */
            Card(shape = CardR, colors = CardDefaults.cardColors(containerColor = Cb.SoftPanel)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("개인정보", fontWeight = FontWeight.Bold, color = Cb.Ink)
                    Text("파일은 내 Google Drive에만 저장됩니다. 외부 서버 업로드는 없습니다.", color = Cb.Muted)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ──────────────── Library Sheet ──────────────── */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibrarySheet(
    captures: List<CaptureItemUi>,
    folderName: String,
    onRetryFailed: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Cb.Panel
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("보관함", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Cb.Ink)
            Text("자동 분류된 스크린샷", color = Cb.Muted)

            if (captures.isEmpty()) {
                Card(shape = CardR, colors = CardDefaults.cardColors(containerColor = Cb.SoftPanel)) {
                    Column(
                        Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("아직 저장된 항목이 없습니다", fontWeight = FontWeight.Bold, color = Cb.Ink, textAlign = TextAlign.Center)
                        Text("스크린샷을 찍으면 여기에 표시됩니다", color = Cb.Muted, textAlign = TextAlign.Center)
                    }
                }
            } else {
                captures.forEach { capture ->
                    CaptureRow(capture = capture, folderName = folderName, onClick = {})
                }
            }

            /* Retry failed */
            val failed = captures.filter { it.status == CaptureStatus.FailedOcr || it.status == CaptureStatus.FailedUpload }
            if (failed.isNotEmpty()) {
                Button(
                    onClick = onRetryFailed,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("실패 항목 다시 시도 (${failed.size})") }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ──────────────── Shared components ──────────────── */

@Composable
private fun StepRow(step: Int, title: String, done: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (done) Cb.Mint else Cb.Blue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (done) "✓" else "$step",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp
            )
        }
        Text(title, fontWeight = FontWeight.Bold, color = if (done) Cb.Mint else Cb.Ink)
    }
}

@Composable
private fun FlowStep(step: String, title: String, body: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text(step, color = Cb.Blue, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Cb.Ink, fontWeight = FontWeight.SemiBold)
            Text(body, color = Cb.Muted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(PillR)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CircleIcon(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.13f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
private fun CircleCheck() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Cb.Mint),
        contentAlignment = Alignment.Center
    ) {
        Text("✓", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
    }
}

@Composable
private fun StatBox(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = CardR,
        border = BorderStroke(1.dp, Cb.Border),
        colors = CardDefaults.cardColors(containerColor = Cb.Panel)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Cb.Muted)
        }
    }
}

@Composable
private fun CaptureRow(capture: CaptureItemUi, folderName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = CardR,
        border = BorderStroke(1.dp, Cb.Border),
        colors = CardDefaults.cardColors(containerColor = Cb.Panel)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircleIcon(categoryIcon(capture.category), statusColor(capture.status))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(capture.title, color = Cb.Ink, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "$folderName/${capture.category}",
                    color = Cb.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Pill(text = capture.status.label, color = statusColor(capture.status))
        }
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(shape = CardR, border = BorderStroke(1.dp, Cb.Border)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Cb.Ink, fontWeight = FontWeight.Bold)
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

/* ──────────────── Helpers ──────────────── */

private fun verticalGradientBrush(colors: List<Color>): Brush =
    Brush.verticalGradient(colors)

private fun statusColor(status: CaptureStatus): Color = when (status) {
    CaptureStatus.Completed, CaptureStatus.MarkdownCreated -> Cb.Mint
    CaptureStatus.FailedOcr, CaptureStatus.FailedUpload -> Cb.Red
    CaptureStatus.SensitivePending -> Cb.Amber
    else -> Cb.Blue
}

private fun categoryIcon(category: String): String = when {
    category.contains("Learning", ignoreCase = true) -> "✦"
    category.contains("Business", ignoreCase = true) -> "₩"
    category.contains("Development", ignoreCase = true) -> "{ }"
    else -> "MD"
}
