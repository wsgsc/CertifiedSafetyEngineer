package com.xiaogong.csestudy.ui.quiz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class QuizModeEntry(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val mode: com.xiaogong.csestudy.ui.navigation.QuizMode,
    val param: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizModeScreen(
    uiState: QuizModeUiState,
    onModeSelected: (com.xiaogong.csestudy.ui.navigation.QuizMode, String) -> Unit
) {
    val seqSubtitle = buildString {
        append("按题库顺序逐题练习")
        if (uiState.sequentialProgress > 0) {
            append("，上次到第 ${uiState.sequentialProgress + 1} 题")
        }
    }

    val entries = listOf(
        QuizModeEntry(Icons.Default.PlayArrow, "顺序练习", seqSubtitle,
            com.xiaogong.csestudy.ui.navigation.QuizMode.SEQUENTIAL),
        QuizModeEntry(Icons.Default.Shuffle, "随机练习", "随机抽取20道题练习",
            com.xiaogong.csestudy.ui.navigation.QuizMode.RANDOM),
        QuizModeEntry(Icons.Default.List, "章节练习", "选择章节进行专项练习",
            com.xiaogong.csestudy.ui.navigation.QuizMode.CHAPTER),
        QuizModeEntry(Icons.Default.ErrorOutline, "错题练习", "重做历史做错的题目",
            com.xiaogong.csestudy.ui.navigation.QuizMode.WRONG),
        QuizModeEntry(Icons.Default.Favorite, "收藏练习", "练习已收藏的题目",
            com.xiaogong.csestudy.ui.navigation.QuizMode.FAVORITE)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("刷题") })
        if (uiState.totalCount > 0) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "题库共 ${uiState.totalCount} 题",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (uiState.totalAnswered > 0) {
                        Text(
                            text = "累计正确率 ${uiState.accuracyPercent}%（${uiState.totalCorrect}/${uiState.totalAnswered}）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries) { entry ->
                QuizModeCard(entry = entry, onClick = { onModeSelected(entry.mode, entry.param) })
            }
        }
    }
}

@Composable
private fun QuizModeCard(entry: QuizModeEntry, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(entry.icon, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium)
                Text(entry.subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuizResultScreen(
    correctCount: Int,
    totalCount: Int,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    val accuracy = if (totalCount == 0) 0 else correctCount * 100 / totalCount
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.EmojiEvents, contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = if (accuracy >= 60) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(24.dp))
        Text("练习完成", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        ResultStatRow("正确题数", "$correctCount / $totalCount")
        ResultStatRow("正确率", "$accuracy%")
        Spacer(Modifier.height(32.dp))
        Button(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("返回首页") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("再来一组") }
    }
}

@Composable
private fun ResultStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}
