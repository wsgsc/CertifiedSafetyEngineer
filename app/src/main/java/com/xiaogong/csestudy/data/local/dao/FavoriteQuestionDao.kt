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

    // 收藏数/收藏 id 均限定在指定科目范围内（初级仅法规+管理）
    @Query("""
        SELECT COUNT(*) FROM favorite_questions f
        JOIN questions q ON q.id = f.questionId
        WHERE q.subject IN (:subjects)
    """)
    fun getCountInSubjects(subjects: List<String>): Flow<Int>

    @Query("""
        SELECT f.questionId FROM favorite_questions f
        JOIN questions q ON q.id = f.questionId
        WHERE q.subject IN (:subjects)
        ORDER BY f.favoritedAt DESC
    """)
    suspend fun getAllIdsInSubjects(subjects: List<String>): List<Long>

    // 收藏开关用，不受科目范围影响
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_questions WHERE questionId = :questionId)")
    suspend fun exists(questionId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_questions WHERE questionId = :questionId)")
    fun isFavorite(questionId: Long): Flow<Boolean>
}
