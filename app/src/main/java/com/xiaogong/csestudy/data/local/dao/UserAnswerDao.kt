package com.xiaogong.csestudy.data.local.dao

import androidx.room.*
import com.xiaogong.csestudy.data.local.entity.UserAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAnswerDao {
    @Insert
    suspend fun insert(answer: UserAnswerEntity)

    @Query("SELECT COUNT(*) FROM user_answers")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_answers WHERE isCorrect = 1")
    fun getCorrectCount(): Flow<Int>

    @Query("SELECT COUNT(DISTINCT questionId) FROM user_answers")
    fun getAnsweredQuestionCount(): Flow<Int>

    // 每题最近一次作答（用于恢复历史答题记录）
    @Query("""
        SELECT * FROM user_answers
        WHERE id IN (SELECT MAX(id) FROM user_answers GROUP BY questionId)
    """)
    suspend fun getLatestAnswers(): List<UserAnswerEntity>

    @Query("SELECT COUNT(*) FROM user_answers WHERE answeredAt >= :startOfDay")
    fun getTodayCount(startOfDay: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_answers WHERE answeredAt >= :startOfDay AND isCorrect = 1")
    fun getTodayCorrectCount(startOfDay: Long): Flow<Int>

    @Query("SELECT * FROM user_answers WHERE questionId = :questionId ORDER BY answeredAt DESC LIMIT 1")
    suspend fun getLatestForQuestion(questionId: Long): UserAnswerEntity?

    @Query("SELECT COUNT(*) FROM user_answers WHERE quizMode = :mode")
    fun getCountByMode(mode: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM user_answers WHERE quizMode = :mode AND isCorrect = 1")
    fun getCorrectCountByMode(mode: String): Flow<Int>
}
