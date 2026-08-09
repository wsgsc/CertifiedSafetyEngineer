package com.xiaogong.csestudy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// 每日学习汇总记录，dateKey 格式 "2024-01-15"
@Entity(tableName = "study_records")
data class StudyRecordEntity(
    @PrimaryKey val dateKey: String,
    val totalAnswered: Int = 0,
    val correctCount: Int = 0,
    val studyMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
