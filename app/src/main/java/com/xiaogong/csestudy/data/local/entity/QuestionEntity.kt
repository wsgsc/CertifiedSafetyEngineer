package com.xiaogong.csestudy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: Long,
    val level: String,          // ExamLevel.name
    val subject: String,        // Subject.name
    val chapter: String,
    val knowledgePoint: String,
    val type: String,           // QuestionType.name
    val question: String,
    val options: List<String>,  // TypeConverter → JSON
    val answer: String,
    val analysis: String,
    val difficulty: Int = 2,
    val source: String = "",
    val year: Int = 0,
    val tags: List<String> = emptyList()
)
