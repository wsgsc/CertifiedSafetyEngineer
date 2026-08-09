package com.xiaogong.csestudy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_questions")
data class FavoriteQuestionEntity(
    @PrimaryKey val questionId: Long,
    val favoritedAt: Long = System.currentTimeMillis()
)
