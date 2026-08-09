package com.xiaogong.csestudy.ui.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xiaogong.csestudy.data.model.Question
import com.xiaogong.csestudy.ui.navigation.QuizMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(vm: QuizViewModel, onFinish: (Int, Int) -> Unit, onBack: () -> Unit) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            onFinish(state.correctCount, state.questions.size)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(state.progress, style = MaterialTheme.typography.bodyLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "退出")
                }
            },
            actions = {
                FavoriteButton(
                    isFavorite = state.isCurrentFavorite,
                    enabled = state.currentQuestion != null,
                    onClick = { vm.toggleFavorite() }
                )
            }
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (state.questions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无题目", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onBack) { Text("返回") }
                }
            }
            return@Column
        }

        val q = state.currentQuestion ?: return@Column

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { QuestionHeader(question = q) }
            item { QuestionBody(question = q) }
            item {
                OptionsSection(
                    question = q,
                    selectedOptions = state.selectedOptions,
                    isAnswered = state.isAnswered,
                    onSelect = { vm.selectOption(it) }
                )
            }
            if (state.isAnswered) {
                item { AnswerResult(state = state, question = q) }
            }
        }

        // 底部按钮区
        Surface(shadowElevation = 4.dp) {
            Column {
                if (vm.quizMode == QuizMode.SEQUENTIAL) {
                    SequentialProgressBar(state = state)
                }
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { vm.prevQuestion() },
                        modifier = Modifier.weight(1f),
                        enabled = state.hasPrev
                    ) { Text("上一题") }
                    Button(
                        onClick = { vm.nextQuestion() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            when {
                                !state.isAnswered && state.selectedOptions.isNotEmpty() -> "提交答案"
                                state.currentIndex + 1 >= state.questions.size -> "查看结果"
                                else -> "下一题"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.18f else 1f,
        animationSpec = spring(dampingRatio = 0.34f, stiffness = Spring.StiffnessMedium),
        label = "favoriteScale"
    )
    val tint by animateColorAsState(
        targetValue = if (isFavorite) Color(0xFFE53935)
                      else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "favoriteTint"
    )

    IconButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "取消收藏" else "收藏",
            tint = tint,
            modifier = Modifier.scale(scale)
        )
    }
}

@Composable
private fun QuestionHeader(question: Question) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SuggestionChip(
            onClick = {},
            label = { Text(question.type.displayName, style = MaterialTheme.typography.labelSmall) }
        )
        Text(
            text = "难度: ${"★".repeat(question.difficulty)}${"☆".repeat(5 - question.difficulty)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuestionBody(question: Question) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = question.question,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp),
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

@Composable
private fun OptionsSection(
    question: Question,
    selectedOptions: Set<String>,
    isAnswered: Boolean,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        question.options.forEach { option ->
            val optionKey = option.take(1) // "A", "B", "C" ...
            val isSelected = optionKey in selectedOptions
            val isCorrect = question.answer.contains(optionKey)

            val borderColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primary
                isAnswered && isCorrect -> Color(0xFF4CAF50)
                isAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val bgColor = when {
                !isAnswered && isSelected -> MaterialTheme.colorScheme.primaryContainer
                isAnswered && isCorrect -> Color(0xFFE8F5E9)
                isAnswered && isSelected && !isCorrect -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable(enabled = !isAnswered) { onSelect(optionKey) },
                color = bgColor,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (isAnswered && isCorrect) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                    } else if (isAnswered && isSelected && !isCorrect) {
                        Icon(Icons.Default.Cancel, contentDescription = null,
                            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerResult(state: QuizUiState, question: Question) {
    val isCorrect = state.isCorrect
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 答题结果横幅
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isCorrect) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        if (isCorrect) "回答正确 ✓" else "回答错误 ✕",
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                    )
                    if (!isCorrect) {
                        Text(
                            "你的答案：${state.selectedOptions.sorted().joinToString("")}   正确答案：${question.answer}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text("正确答案：${question.answer}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // 答案解析
        AnalysisCard(title = "答案解析", content = question.analysis)

        // 知识点标签
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("知识点", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                Text(question.knowledgePoint, style = MaterialTheme.typography.bodyMedium)
                if (question.source.isNotBlank() || question.year > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        buildString {
                            if (question.year > 0) append("${question.year}年")
                            if (question.source.isNotBlank()) append(" · ${question.source}")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisCard(title: String, content: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(content, style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight)
        }
    }
}

@Composable
private fun SequentialProgressBar(state: QuizUiState) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "进度 ${state.currentIndex + 1} / ${state.questions.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "正确率 ${state.globalAccuracyPercent}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (state.globalAccuracyPercent >= 60)
                        Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                )
                Text(
                    text = "答对 ${state.globalCorrect} / 已答 ${state.globalAnswered}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { state.progressFraction },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
