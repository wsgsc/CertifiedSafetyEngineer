package com.xiaogong.csestudy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_answers")
data class UserAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val userAnswer: String,
    val isCorrect: Boolean,
    val answeredAt: Long = System.currentTimeMillis(),
    val timeTakenMs: Long = 0,
    val quizMode: String = ""
)
