package com.xiaogong.csestudy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wrong_questions")
data class WrongQuestionEntity(
    @PrimaryKey val questionId: Long,
    val wrongCount: Int = 1,
    val lastWrongAt: Long = System.currentTimeMillis(),
    val isMastered: Boolean = false  // 用户标记已掌握
)
