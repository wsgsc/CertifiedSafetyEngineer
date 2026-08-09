package com.xiaogong.csestudy.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.xiaogong.csestudy.data.model.ExamLevel
import com.xiaogong.csestudy.ui.navigation.QuizMode
import com.xiaogong.csestudy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: HomeViewModel, onNavigate: (String) -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 用户问候卡片 ────────────────────────────────────
        item {
            UserGreetingCard(
                nickname = state.nickname.ifBlank { "学习达人" },
                avatarUri = state.avatarUri,
                examLevel = state.examLevel
            )
        }

        // ── 今日学习统计卡片 ──────────────────────────────
        item {
            TodayStudyCard(state = state, onContinue = {
                onNavigate(Screen.QuizPlay.createRoute(QuizMode.RANDOM))
            })
        }

        // ── 打卡天数 ──────────────────────────────────────
        item {
            CheckInCard(state = state)
        }

        // ── 总体进度卡片 ──────────────────────────────────
        item {
            OverallProgressCard(state = state)
        }

        // ── 每日鼓励语 ────────────────────────────────────
        if (state.encouragingQuote.isNotBlank()) {
            item { QuoteCard(quote = state.encouragingQuote, onClick = vm::refreshQuote) }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun QuoteCard(quote: String, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    var spins by remember { mutableIntStateOf(0) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "quoteCardScale"
    )
    val iconRotation by animateFloatAsState(
        targetValue = spins * 360f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "quoteIconRotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                spins++
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        QuoteCardContent(quote = quote, iconRotation = iconRotation)
    }
}

/** 旧句上浮淡出，新句自下方弹入 */
private fun AnimatedContentTransitionScope<String>.quoteTransition(): ContentTransform {
    val enter = fadeIn(tween(320, delayMillis = 90)) +
        slideInVertically(
            animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessLow)
        ) { it / 3 } +
        scaleIn(initialScale = 0.9f, animationSpec = tween(320, delayMillis = 90))

    val exit = fadeOut(tween(180)) +
        slideOutVertically(tween(220, easing = FastOutLinearInEasing)) { -it / 3 } +
        scaleOut(targetScale = 0.94f, animationSpec = tween(180))

    return enter togetherWith exit using SizeTransform(clip = false)
}

@Composable
private fun QuoteCardContent(quote: String, iconRotation: Float) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
            Spacer(Modifier.width(12.dp))
            AnimatedContent(
                targetState = quote,
                transitionSpec = { quoteTransition() },
                label = "quoteText",
                modifier = Modifier.weight(1f)
            ) { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 30.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Autorenew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f),
                modifier = Modifier
                    .size(14.dp)
                    .graphicsLayer { rotationZ = iconRotation }
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "点一下，换一句",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun OverallProgressCard(state: HomeUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("总体进度", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    "${state.overallProgressPercent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { state.overallProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "已刷 ${state.answeredQuestions} / ${state.totalQuestions} 题",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "累计答题 ${state.totalAnswerTimes} 次 · 正确率 ${state.overallAccuracy}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CheckInCard(state: HomeUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${state.checkInDays}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "天打卡",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
                Text(
                    text = if (state.streakDays > 1) "已连续学习 ${state.streakDays} 天，保持住！"
                           else "坚持每天打卡，习惯自然养成",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TodayStudyCard(state: HomeUiState, onContinue: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("今日学习", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(value = "${state.todayAnswered}", label = "已答题")
                StatItem(value = "${state.todayAccuracy}%", label = "正确率")
                StatItem(value = "${state.wrongCount}", label = "错题")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.todayAnswered == 0) "开始学习" else "继续练习")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun UserGreetingCard(
    nickname: String,
    avatarUri: String,
    examLevel: ExamLevel
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hi, $nickname",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = examLevel.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
