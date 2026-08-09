package com.xiaogong.csestudy.ui.study

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xiaogong.csestudy.data.model.ExamLevel
import com.xiaogong.csestudy.data.model.Subject

@Composable
private fun SubjectItem(subject: Subject, questionCount: Int, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(subject.displayName, style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium)
                Text("共 $questionCount 题", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSubjectSelectScreen(
    vm: StudyViewModel,
    examLevel: ExamLevel?,
    onSubjectClick: (Subject) -> Unit
) {
    val counts by vm.subjectCounts.collectAsStateWithLifecycle()
    val level = examLevel ?: ExamLevel.INTERMEDIATE
    val subjects = Subject.forLevel(level)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("章节练习 - ${level.displayName}") })
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(subjects) { subject ->
                SubjectItem(
                    subject = subject,
                    questionCount = counts[subject] ?: 0,
                    onClick = { onSubjectClick(subject) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectName: String,
    vm: StudyViewModel,
    onChapterClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val subject = Subject.valueOf(subjectName)
    val chapters by vm.chapters.collectAsStateWithLifecycle()

    LaunchedEffect(subjectName) { vm.loadChapters(subjectName) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(subject.displayName) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "返回",
                        modifier = Modifier.rotate(180f))
                }
            }
        )
        if (chapters.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无章节数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(chapters) { chapter ->
                    ChapterItem(chapter = chapter, onClick = { onChapterClick(chapter) })
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(chapter: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(chapter, style = MaterialTheme.typography.bodyLarge)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

