package com.xiaogong.csestudy.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.xiaogong.csestudy.data.model.ExamLevel
import com.xiaogong.csestudy.util.AvatarHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: ProfileViewModel, onLevelSelected: (ExamLevel) -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var showLevelDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingTimeSlot by remember { mutableIntStateOf(0) }

    if (editingTimeSlot > 0) {
        val slot = editingTimeSlot
        val initialHour = if (slot == 1) state.reminderHour1 else state.reminderHour2
        val initialMinute = if (slot == 1) state.reminderMinute1 else state.reminderMinute2
        ReminderTimePickerDialog(
            initialHour = initialHour,
            initialMinute = initialMinute,
            onConfirm = { hour, minute ->
                vm.setReminderTime(slot, hour, minute)
                editingTimeSlot = 0
            },
            onDismiss = { editingTimeSlot = 0 }
        )
    }

    if (showEditDialog) {
        EditProfileDialog(
            currentNickname = state.nickname,
            currentAvatarUri = state.avatarUri,
            onConfirm = { nickname, avatarUri ->
                vm.saveProfile(nickname, avatarUri)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showLevelDialog) {
        LevelSwitchDialog(
            current = state.examLevel,
            onConfirm = { level ->
                onLevelSelected(level)
                showLevelDialog = false
            },
            onDismiss = { showLevelDialog = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("我的") })
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── 用户信息卡片 ──────────────────────────────
            item {
                UserInfoCard(
                    nickname = state.nickname.ifBlank { "学习达人" },
                    avatarUri = state.avatarUri,
                    onClick = { showEditDialog = true }
                )
            }

            // ── 近7天正确率趋势 ──────────────────────────────
            if (state.weeklyStats.isNotEmpty()) {
                item {
                    WeeklyChartCard(stats = state.weeklyStats)
                }
            }

            // ── 学习统计 ──────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("学习统计", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround) {
                            StatColumn("${state.totalAnswered}", "总答题数")
                            StatColumn("${state.overallAccuracy}%", "总正确率")
                            StatColumn("${state.streakDays}天", "连续打卡")
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround) {
                            StatColumn("${state.totalCorrect}", "答对题数")
                            StatColumn("${state.totalAnswered - state.totalCorrect}", "答错题数")
                        }
                    }
                }
            }

            // ── 功能菜单 ──────────────────────────────────
            item {
                Text("功能", style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ProfileMenuItem(
                            icon = Icons.Default.Favorite,
                            title = "我的收藏",
                            subtitle = "${state.favoriteCount} 道已收藏"
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileMenuItem(
                            icon = Icons.Default.School,
                            title = "考试级别",
                            subtitle = state.examLevel?.displayName ?: "未设置",
                            onClick = { showLevelDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("每日提醒", style = MaterialTheme.typography.bodyLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        "${String.format("%02d:%02d", state.reminderHour1, state.reminderMinute1)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (state.reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable(enabled = state.reminderEnabled) { editingTimeSlot = 1 }
                                    )
                                    Text(
                                        "${String.format("%02d:%02d", state.reminderHour2, state.reminderMinute2)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (state.reminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.clickable(enabled = state.reminderEnabled) { editingTimeSlot = 2 }
                                    )
                                }
                            }
                            Switch(
                                checked = state.reminderEnabled,
                                onCheckedChange = { vm.setReminderEnabled(it) }
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileMenuItem(
                            icon = Icons.Default.Info,
                            title = "关于",
                            subtitle = "注册安全工程师刷题 v1.0"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelSwitchDialog(
    current: ExamLevel?,
    onConfirm: (ExamLevel) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("切换考试级别") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExamLevel.entries.forEach { level ->
                    val isSelected = level == current
                    OutlinedButton(
                        onClick = { onConfirm(level) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isSelected) ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(
                            text = level.displayName,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun StatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun UserInfoCard(nickname: String, avatarUri: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri.isNotEmpty()) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "头像",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "点击编辑个人资料",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentNickname: String,
    currentAvatarUri: String,
    onConfirm: (nickname: String, avatarUri: String) -> Unit,
    onDismiss: () -> Unit
) {
    var nickname by remember { mutableStateOf(currentNickname) }
    var avatarUri by remember { mutableStateOf(currentAvatarUri) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) {
                    AvatarHelper.copyToInternal(context, uri)
                }
                avatarUri = path
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑个人资料") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (avatarUri.isNotEmpty()) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "头像",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.size(28.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = "更换照片",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { if (it.length <= 12) nickname = it },
                    label = { Text("昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(nickname.ifBlank { "学习达人" }, avatarUri) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun WeeklyChartCard(stats: List<com.xiaogong.csestudy.data.local.entity.StudyRecordEntity>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val sorted = stats.sortedBy { it.dateKey }
    val accuracies = sorted.map {
        if (it.totalAnswered == 0) 0 else it.correctCount * 100 / it.totalAnswered
    }
    val labels = sorted.map { it.dateKey.takeLast(5) } // "01-15"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("近7天正确率", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (accuracies.all { it == 0 }) {
                Text("暂无答题记录", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                    val w = size.width
                    val h = size.height
                    val padLeft = 40f
                    val padBottom = 24f
                    val padTop = 28f
                    val padRight = 16f
                    val chartW = w - padLeft - padRight
                    val chartH = h - padBottom - padTop

                    // Y axis grid lines (0%, 50%, 100%)
                    val yPcts = listOf(0, 50, 100)
                    yPcts.forEach { pct ->
                        val y = padTop + chartH * (1 - pct / 100f)
                        drawLine(
                            color = surfaceVariant,
                            start = androidx.compose.ui.geometry.Offset(padLeft, y),
                            end = androidx.compose.ui.geometry.Offset(w - padRight, y),
                            strokeWidth = 1f
                        )
                    }

                    if (accuracies.size < 2) return@Canvas

                    // Calculate points
                    val stepX = chartW / (accuracies.size - 1)
                    val points = accuracies.mapIndexed { i, acc ->
                        androidx.compose.ui.geometry.Offset(
                            padLeft + i * stepX,
                            padTop + chartH * (1 - acc / 100f)
                        )
                    }

                    // Draw lines
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = primaryColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 2.5f
                        )
                    }

                    // Draw dots
                    points.forEach { p ->
                        drawCircle(color = primaryColor, radius = 4f, center = p)
                        drawCircle(color = androidx.compose.ui.graphics.Color.White, radius = 2f, center = p)
                    }
                }

                // Date labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    labels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置提醒时间") },
        text = {
            TimePicker(state = state)
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
