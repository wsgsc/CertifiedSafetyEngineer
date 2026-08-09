package com.xiaogong.csestudy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// knowledgeId 格式: "{subject}_{chapter}_{knowledgePoint}"
@Entity(tableName = "knowledge_progress")
data class KnowledgeProgressEntity(
    @PrimaryKey val knowledgeId: String,
    val subject: String,
    val chapter: String,
    val knowledgePoint: String,
    val answerCount: Int = 0,
    val correctCount: Int = 0,
    val lastPracticeAt: Long = 0L
) {
    val accuracy: Float get() = if (answerCount == 0) 0f else correctCount.toFloat() / answerCount
    // 0=未练习 1=较弱(<60%) 2=一般(60-79%) 3=良好(80-89%) 4=优秀(>=90%)
    val masteryLevel: Int get() = when {
        answerCount == 0 -> 0
        accuracy < 0.6f -> 1
        accuracy < 0.8f -> 2
        accuracy < 0.9f -> 3
        else -> 4
    }
}
