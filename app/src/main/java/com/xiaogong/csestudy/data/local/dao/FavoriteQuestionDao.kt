package com.xiaogong.csestudy.data.local.dao

import androidx.room.*
import com.xiaogong.csestudy.data.local.entity.FavoriteQuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteQuestionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavoriteQuestionEntity)

    @Query("DELETE FROM favorite_questions WHERE questionId = :questionId")
    suspend fun delete(questionId: Long)

    @Query("SELECT * FROM favorite_questions ORDER BY favoritedAt DESC")
    fun getAll(): Flow<List<FavoriteQuestionEntity>>

    @Query("SELECT COUNT(*) FROM favorite_questions")
    fun getCount(): Flow<Int>

    @Query("SELECT questionId FROM favorite_questions")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_questions WHERE questionId = :questionId)")
    fun isFavorite(questionId: Long): Flow<Boolean>
}
